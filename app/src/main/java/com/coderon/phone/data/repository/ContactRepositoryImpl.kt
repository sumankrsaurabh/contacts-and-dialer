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
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ), null, null, ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
            )
            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getString(0)
                    val name = it.getString(1)
                    val number = it.getString(2)
                    contacts.add(Contact(id, name, number))
                }
            }
            contacts
        }
    }

    override suspend fun addContact(name: String, phoneNumber: String) {
        withContext(Dispatchers.IO) {
            val operations = ArrayList<ContentProviderOperation>()
            val rawContactUri = ContactsContract.RawContacts.CONTENT_URI
            val dataUri = ContactsContract.Data.CONTENT_URI

            operations.add(
                ContentProviderOperation.newInsert(rawContactUri)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null).build()
            )

            operations.add(
                ContentProviderOperation.newInsert(dataUri)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                    ).withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                    .build()
            )

            operations.add(
                ContentProviderOperation.newInsert(dataUri)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0).withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                    ).withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phoneNumber)
                    .withValue(
                        ContactsContract.CommonDataKinds.Phone.TYPE,
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                    ).build()
            )

            contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
        }
    }

    override suspend fun deleteContact(contactId: String) {
        withContext(Dispatchers.IO) {
            val uri = ContactsContract.RawContacts.CONTENT_URI
            contentResolver.delete(
                uri, "${ContactsContract.RawContacts.CONTACT_ID}=?", arrayOf(contactId)
            )
        }
    }

    override suspend fun updateContact(contactId: String, name: String, phoneNumber: String) {
        deleteContact(contactId)
        addContact(name, phoneNumber)
    }
}
