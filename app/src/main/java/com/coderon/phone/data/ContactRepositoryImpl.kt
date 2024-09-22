package com.coderon.phone.data

import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import com.coderon.phone.data.modal.Contact
import com.coderon.phone.domain.ContactRepository

class ContactRepositoryImpl(private val contentResolver: ContentResolver) : ContactRepository {

    companion object {
        private const val TAG = "ContactRepositoryImpl"
        private const val CACHE_EXPIRATION_MILLIS = 5 * 60 * 1000 // 5 minutes

        private val CONTACT_PROJECTION = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME,
            ContactsContract.Contacts.HAS_PHONE_NUMBER
        )

        private val PHONE_PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
    }

    private data class ContactCache(val contacts: List<Contact>, val timestamp: Long)
    private var contactCache: ContactCache? = null

    override suspend fun getContacts(): List<Contact> {
        val currentTime = System.currentTimeMillis()

        if (contactCache != null && (currentTime - contactCache!!.timestamp < CACHE_EXPIRATION_MILLIS)) {
            Log.d(TAG, "Returning cached contacts")
            return contactCache!!.contacts
        }

        Log.d(TAG, "Fetching contacts from content resolver")
        val contactList = mutableListOf<Contact>()

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            CONTACT_PROJECTION,
            null,
            null,
            ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            val hasPhoneNumberIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getLong(idIndex)
                val contactName = it.getString(nameIndex)

                if (contactName.isNullOrBlank() || it.getInt(hasPhoneNumberIndex) == 0) {
                    continue // Skip contacts without name or phone number
                }

                // Fetch phone numbers for the contact
                val phoneNumbers = getContactPhoneNumbers(contactId)
                val primaryPhoneNumber = phoneNumbers.firstOrNull() ?: "N/A"

                // Add to the contact list
                contactList.add(Contact(id = contactId, name = contactName, phoneNumber = primaryPhoneNumber))
            }
        }

        contactCache = ContactCache(contactList, currentTime)
        Log.d(TAG, "Caching contacts and returning fresh data. Total contacts: ${contactList.size}")
        return contactList
    }

    override suspend fun getContactById(id: Long): Contact? {
        val currentTime = System.currentTimeMillis()
        if (contactCache != null && (currentTime - contactCache!!.timestamp < CACHE_EXPIRATION_MILLIS)) {
            val contact = contactCache!!.contacts.find { it.id == id }
            if (contact != null) {
                Log.d(TAG, "Returning cached contact with ID: $id")
                return contact
            }
        }

        Log.d(TAG, "Fetching contact by ID: $id from content resolver")

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            CONTACT_PROJECTION,
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(id.toString()),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val contactName = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val hasPhoneNumber = it.getInt(it.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                if (hasPhoneNumber == 0) {
                    return null
                }

                val phoneNumbers = getContactPhoneNumbers(id)
                val primaryPhoneNumber = phoneNumbers.firstOrNull() ?: "N/A"

                return Contact(id = id, name = contactName, phoneNumber = primaryPhoneNumber)
            }
        }

        Log.d(TAG, "Contact with ID: $id not found")
        return null
    }

    // Helper function to get phone numbers for a given contact ID
    private fun getContactPhoneNumbers(contactId: Long): List<String> {
        val phoneNumbers = mutableListOf<String>()

        val phonesProjection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val phonesCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            phonesProjection,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )

        phonesCursor?.use {
            val numberIndex = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val phoneNumber = it.getString(numberIndex)
                phoneNumbers.add(phoneNumber)
            }
        }

        return phoneNumbers
    }
}
