package com.coderon.phone.data.repository

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.provider.ContactsContract
import com.coderon.phone.data.modal.Contact
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactRepositoryImpl(private val contentResolver: ContentResolver) : ContactRepository {

    override suspend fun getContacts(): List<Contact> {
        return withContext(Dispatchers.IO) {
            val contacts = mutableListOf<Contact>()
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Photo.PHOTO_URI
                ),
                null, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )

            val seenContacts = mutableSetOf<String>() // Prevent duplicate contacts

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    if (id in seenContacts) continue // Skip duplicates
                    seenContacts.add(id)

                    val name = it.getString(1)
                    val number = it.getString(2)
                    val profilePictureUri = it.getString(3) ?: "" // Handle null safely

                    contacts.add(Contact(id, name, number, profilePictureUri.ifEmpty { null }))
                }
            }
            contacts
        }
    }

    override suspend fun getContact(contactId: String): Contact? {
        return withContext(Dispatchers.IO) {
            val cursor = contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Photo.PHOTO_URI
                ),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )

            var contact: Contact? = null
            cursor?.use {
                if (it.moveToFirst()) {
                    val name = it.getString(0)
                    val phoneNumber = it.getString(1)
                    val profilePictureUri = it.getString(2) ?: "" // Handle null safely

                    contact = Contact(
                        id = contactId,
                        name = name,
                        phoneNumber = phoneNumber,
                        profilePictureUrl = profilePictureUri.ifEmpty { null }
                    )
                }
            }
            contact
        }
    }

    override suspend fun addContact(name: String, phoneNumber: String, profilePictureUri: String?) {
        withContext(Dispatchers.IO) {
            try {
                val operations = ArrayList<ContentProviderOperation>()
                val rawContactUri = ContactsContract.RawContacts.CONTENT_URI
                val dataUri = ContactsContract.Data.CONTENT_URI

                // Create a new raw contact
                operations.add(
                    ContentProviderOperation.newInsert(rawContactUri)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build()
                )

                // Insert Name
                operations.add(
                    ContentProviderOperation.newInsert(dataUri)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build()
                )

                // Insert Phone Number
                operations.add(
                    ContentProviderOperation.newInsert(dataUri)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                        .build()
                )

                // Insert Profile Picture (only if available)
                profilePictureUri?.takeIf { it.isNotEmpty() }?.let {
                    operations.add(
                        ContentProviderOperation.newInsert(dataUri)
                            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                            .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO_URI, it)
                            .build()
                    )
                }

                // Apply batch operation
                contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun updateContact(contactId: String, name: String, phoneNumber: String, profilePictureUri: String?) {
        withContext(Dispatchers.IO) {
            try {
                val operations = ArrayList<ContentProviderOperation>()
                val dataUri = ContactsContract.Data.CONTENT_URI

                // Update Name
                operations.add(
                    ContentProviderOperation.newUpdate(dataUri)
                        .withSelection(
                            "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                            arrayOf(contactId, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                        )
                        .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                        .build()
                )

                // Update Phone Number
                operations.add(
                    ContentProviderOperation.newUpdate(dataUri)
                        .withSelection(
                            "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                            arrayOf(contactId, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .build()
                )

                // Update Profile Picture (if available)
                profilePictureUri?.takeIf { it.isNotEmpty() }?.let {
                    operations.add(
                        ContentProviderOperation.newUpdate(dataUri)
                            .withSelection(
                                "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                                arrayOf(contactId, ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE)
                            )
                            .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO_URI, it)
                            .build()
                    )
                }

                // Apply batch update
                contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun deleteContact(contactId: String) {
        withContext(Dispatchers.IO) {
            val uri = ContactsContract.RawContacts.CONTENT_URI
            contentResolver.delete(uri, "${ContactsContract.RawContacts.CONTACT_ID}=?", arrayOf(contactId))
        }
    }
}
