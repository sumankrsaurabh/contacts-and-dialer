package com.coderon.phone.data.repository

import android.annotation.SuppressLint
import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.net.toUri
import com.coderon.phone.data.model.Contact
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class ContactRepositoryImpl(private val contentResolver: ContentResolver) : ContactRepository {

    override suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableListOf<Contact>()
        val seenContacts = mutableSetOf<String>()

        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            CONTACT_PROJECTION,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val photoUriIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                if (id in seenContacts) continue
                seenContacts.add(id)

                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex) ?: ""
                val profilePictureUri = it.getString(photoUriIndex)

                contacts.add(
                    Contact(
                        id = id,
                        name = name,
                        phoneNumber = number,
                        profilePictureUrl = profilePictureUri?.takeIf { profilePicture -> profilePicture.isNotEmpty() }
                    )
                )
            }
        }

        contacts
    }

    @SuppressLint("Range")
    override suspend fun getContact(contactId: String): Contact? = withContext(Dispatchers.IO) {
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            CONTACT_PROJECTION,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId),
            null
        )

        var contact: Contact? = null
        cursor?.use {
            if (it.moveToFirst()) {
                val name =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
                        ?: "Unknown"
                val number =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        ?: ""
                val profilePictureUri =
                    it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Photo.PHOTO_URI))

                contact = Contact(
                    id = contactId,
                    name = name,
                    phoneNumber = number,
                    profilePictureUrl = profilePictureUri?.takeIf { profilePicture -> profilePicture.isNotEmpty() }
                )
            }
        }

        contact
    }

    override suspend fun addContact(name: String, phoneNumber: String, profilePictureUri: String?) {
        withContext(Dispatchers.IO) {
            try {
                val operations = ArrayList<ContentProviderOperation>()

                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build()
                )

                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            name
                        )
                        .build()
                )

                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .withValue(
                            ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                        )
                        .build()
                )

                // Insert photo if provided
                profilePictureUri?.takeIf { it.isNotEmpty() }?.let { uriString ->
                    val photoBytes = getPhotoByteArray(uriString.toUri())
                    if (photoBytes != null) {
                        operations.add(
                            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                                .withValue(
                                    ContactsContract.Data.MIMETYPE,
                                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                                )
                                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                                .build()
                        )
                    }
                }

                contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            } catch (e: Exception) {
                e.printStackTrace() // Replace with logging if needed
            }
        }
    }

    override suspend fun updateContact(
        contactId: String,
        name: String,
        phoneNumber: String,
        profilePictureUri: String?
    ) {
        withContext(Dispatchers.IO) {
            try {
                val operations = ArrayList<ContentProviderOperation>()

                operations.add(
                    ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                            "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                            arrayOf(
                                contactId,
                                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                            )
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            name
                        )
                        .build()
                )

                operations.add(
                    ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                        .withSelection(
                            "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                            arrayOf(
                                contactId,
                                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                            )
                        )
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                        .build()
                )

                // Update profile picture if provided
                profilePictureUri?.takeIf { it.isNotEmpty() }?.let { uriString ->
                    val photoBytes = getPhotoByteArray(uriString.toUri())
                    if (photoBytes != null) {
                        operations.add(
                            ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                                .withSelection(
                                    "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                                    arrayOf(
                                        contactId,
                                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                                    )
                                )
                                .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, photoBytes)
                                .build()
                        )
                    }
                }

                contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
            } catch (e: Exception) {
                e.printStackTrace() // Replace with logging
            }
        }
    }

    override suspend fun deleteContact(contactId: String) {
        withContext(Dispatchers.IO) {
            try {
                contentResolver.delete(
                    ContactsContract.RawContacts.CONTENT_URI,
                    "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                    arrayOf(contactId)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Helper to read photo file into byte array
    private fun getPhotoByteArray(uri: Uri): ByteArray? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.use { it.readBytes() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private val CONTACT_PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI
        )
    }
}
