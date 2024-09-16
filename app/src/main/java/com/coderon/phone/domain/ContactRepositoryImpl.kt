package com.coderon.phone.domain

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.provider.ContactsContract
import android.util.Log
import com.coderon.phone.data.modal.Contact

class ContactRepositoryImpl(private val contentResolver: ContentResolver) : ContactRepository {

    private val TAG = "ContactRepositoryImpl"

    // Fetch all contacts from the device and Google accounts
    override suspend fun getContacts(): List<Contact> {
        Log.d(TAG, "Starting to fetch all contacts")
        val contactList = mutableListOf<Contact>()

        // Define a projection to limit the columns we query (improves performance)
        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            null, // No selection clause
            null, // No selection args
            ContactsContract.Contacts.DISPLAY_NAME + " ASC" // Sort by display name
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)

            while (it.moveToNext()) {
                val contactId = it.getLong(idIndex)
                val contactName = it.getString(nameIndex)

                if (contactName.isNullOrBlank()) {
                    Log.w(TAG, "Skipping contact with ID: $contactId because the name is null or blank")
                    continue // Skip the contact if the name is null or empty
                }

                Log.d(TAG, "Fetched contact: ID = $contactId, Name = $contactName")

                // Fetch phone numbers for this contact
                val phoneNumbers = getContactPhoneNumbers(contactId)
                val primaryPhoneNumber = phoneNumbers.firstOrNull() ?: "N/A"

                // Add the contact to the list
                contactList.add(Contact(id = contactId, name = contactName, phoneNumber = primaryPhoneNumber))
                Log.d(TAG, "Added contact: $contactName with primary number: $primaryPhoneNumber")
            }
        } ?: Log.w(TAG, "Cursor returned null when querying contacts")

        Log.d(TAG, "Completed fetching contacts. Total: ${contactList.size}")
        return contactList
    }

    // Fetch a single contact by its ID
    @SuppressLint("Range")
    override suspend fun getContactById(id: Long): Contact? {
        Log.d(TAG, "Fetching contact by ID: $id")

        val projection = arrayOf(
            ContactsContract.Contacts._ID,
            ContactsContract.Contacts.DISPLAY_NAME
        )

        val cursor = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            projection,
            "${ContactsContract.Contacts._ID} = ?",
            arrayOf(id.toString()),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val contactName = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                val phoneNumbers = getContactPhoneNumbers(id)
                val primaryPhoneNumber = phoneNumbers.firstOrNull() ?: "N/A"

                Log.d(TAG, "Fetched contact: Name = $contactName, Phone Number = $primaryPhoneNumber")

                return Contact(id = id, name = contactName ?: "Unknown", phoneNumber = primaryPhoneNumber)
            }
        } ?: Log.w(TAG, "Cursor returned null when querying contact by ID: $id")

        Log.e(TAG, "Failed to find contact with ID: $id")
        return null
    }

    // Helper function to get phone numbers for a given contact ID
    private fun getContactPhoneNumbers(contactId: Long): List<String> {
        Log.d(TAG, "Fetching phone numbers for contact ID: $contactId")
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
                Log.d(TAG, "Fetched phone number: $phoneNumber")
            }
        } ?: Log.w(TAG, "Cursor returned null when querying phone numbers for contact ID: $contactId")

        Log.d(TAG, "Completed fetching phone numbers for contact ID: $contactId")
        return phoneNumbers
    }
}
