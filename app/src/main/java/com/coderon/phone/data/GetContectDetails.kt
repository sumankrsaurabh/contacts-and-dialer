package com.coderon.phone.data

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun readContactsFromLocalStorage(context: Context): List<Contact> {
    return withContext(Dispatchers.IO) {
        val contactsList = mutableListOf<Contact>()

        // Query the contacts content provider
        val cursor = context.contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.Contacts._ID)
            val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
            val hasPhoneNumberIndex = it.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

            while (it.moveToNext()) {
                val contactId = it.getString(idIndex)
                val contactName = it.getString(nameIndex)
                val hasPhoneNumber = it.getInt(hasPhoneNumberIndex)

                val phoneNumber = if (hasPhoneNumber > 0) {
                    getPhoneNumber(context, contactId)
                } else {
                    null
                }

                val email = getEmail(context, contactId)
                val address = getAddress(context, contactId)
                val companyAndJob = getCompanyAndJob(context, contactId)
                val profilePicture = getProfilePicture(context, contactId)
                val ringtone = getRingtone(context, contactId)
                val notes = getNotes(context, contactId)
                val birthday = getBirthday(context, contactId)

                // Adding the contact to the list
                phoneNumber?.let { it1 ->
                    Contact(
                        name = contactName ?: "Unknown",
                        phoneNumber = it1,
                        profilePicture = profilePicture,
                        email = email,
                        address = address,
                        company = companyAndJob?.first,
                        jobTitle = companyAndJob?.second,
                        notes = notes,
                        ringtone = ringtone,
                        isFavorite = false,  // Assuming default value; this can be updated based on your logic
                        birthday = birthday,
                        contactType = ContactType.PERSONAL // Adjust as per your logic
                    )
                }?.let { it2 ->
                    contactsList.add(
                        it2
                    )
                }
            }
        }
        contactsList
    }
}

// Function to retrieve the phone number(s) of a contact
@SuppressLint("Range")
private fun getPhoneNumber(context: Context, contactId: String): String? {
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
        arrayOf(contactId), null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
        }
    }
    return null
}

// Function to retrieve the email(s) of a contact
@SuppressLint("Range")
private fun getEmail(context: Context, contactId: String): String? {
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Email.CONTENT_URI,
        null,
        ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
        arrayOf(contactId), null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
        }
    }
    return null
}

// Function to retrieve the physical address of a contact
@SuppressLint("Range")
private fun getAddress(context: Context, contactId: String): String? {
    val cursor = context.contentResolver.query(
        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
        null,
        ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?",
        arrayOf(contactId), null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS))
        }
    }
    return null
}

// Function to retrieve company and job title of a contact
@SuppressLint("Range")
private fun getCompanyAndJob(context: Context, contactId: String): Pair<String?, String?>? {
    val cursor = context.contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Organization.COMPANY,
            ContactsContract.CommonDataKinds.Organization.TITLE
        ),
        "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
        arrayOf(contactId, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE),
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            val company = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.COMPANY))
            val jobTitle = it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE))
            return Pair(company, jobTitle)
        }
    }
    return null
}

// Function to retrieve profile picture of a contact
private fun getProfilePicture(context: Context, contactId: String): ImageBitmap? {
    val uri = ContactsContract.Contacts.CONTENT_URI.buildUpon().appendPath(contactId).build()
    val inputStream = ContactsContract.Contacts.openContactPhotoInputStream(context.contentResolver, uri)

    inputStream?.use {
        val bitmap = BitmapFactory.decodeStream(it)
        return bitmap.asImageBitmap()
    }
    return null
}

// Function to retrieve custom ringtone of a contact
@SuppressLint("Range")
private fun getRingtone(context: Context, contactId: String): String? {
    val cursor = context.contentResolver.query(
        ContactsContract.Contacts.CONTENT_URI,
        arrayOf(ContactsContract.Contacts.CUSTOM_RINGTONE),
        ContactsContract.Contacts._ID + " = ?",
        arrayOf(contactId), null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndex(ContactsContract.Contacts.CUSTOM_RINGTONE))
        }
    }
    return null
}

// Function to retrieve notes of a contact
@SuppressLint("Range")
private fun getNotes(context: Context, contactId: String): String? {
    val cursor = context.contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Note.NOTE),
        "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
        arrayOf(contactId, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE),
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE))
        }
    }
    return null
}

// Function to retrieve birthday of a contact
@SuppressLint("Range")
private fun getBirthday(context: Context, contactId: String): String? {
    val cursor = context.contentResolver.query(
        ContactsContract.Data.CONTENT_URI,
        arrayOf(ContactsContract.CommonDataKinds.Event.START_DATE),
        "${ContactsContract.Data.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ? AND ${ContactsContract.CommonDataKinds.Event.TYPE} = ?",
        arrayOf(contactId, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY.toString()),
        null
    )

    cursor?.use {
        if (it.moveToFirst()) {
            return it.getString(it.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE))
        }
    }
    return null
}
