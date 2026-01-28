package com.coderon.phone.data.repository

import android.content.ContentProviderOperation
import android.content.ContentResolver
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.net.toUri
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.data.model.PhoneNumberType
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream

class ContactRepositoryImpl(
    private val contentResolver: ContentResolver
) : ContactRepository {

    // ------------------------------------------------
    // GET ALL CONTACTS (MULTI NUMBER SUPPORT)
    // ------------------------------------------------
    override suspend fun getContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contactMap = linkedMapOf<String, MutableContactBuilder>()

        val cursor = contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)
            val nameIndex = it.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)
            val mimeTypeIndex = it.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)
            val photoIndex = it.getColumnIndexOrThrow(ContactsContract.Data.PHOTO_URI)
            val starredIndex = it.getColumnIndexOrThrow(ContactsContract.Data.STARRED)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val mimeType = it.getString(mimeTypeIndex)
                val photoUri = it.getString(photoIndex)
                val isFavorite = it.getInt(starredIndex) == 1

                val builder = contactMap.getOrPut(id) {
                    MutableContactBuilder(
                        id = id,
                        displayName = name,
                        photoUri = photoUri,
                        isFavorite = isFavorite
                    )
                }

                when (mimeType) {
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                        val number = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                        val type = it.getInt(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
                        if (number != null) {
                            builder.phoneNumbers.add(
                                PhoneNumber(
                                    number = number,
                                    type = mapPhoneType(type),
                                    isPrimary = builder.phoneNumbers.isEmpty()
                                )
                            )
                        }
                    }
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                        val email = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                        if (email != null) {
                            builder.emailAddresses.add(email)
                        }
                    }
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE -> {
                        builder.firstName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME))
                        builder.lastName = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME))
                    }
                }
            }
        }

        contactMap.values.map { it.build() }
    }

    // ------------------------------------------------
    // GET SINGLE CONTACT
    // ------------------------------------------------
    override suspend fun getContact(contactId: String): Contact? =
        withContext(Dispatchers.IO) {
            getContacts().firstOrNull { it.id == contactId }
        }

    override suspend fun getContactByNumber(phoneNumber: String): Contact? = withContext(Dispatchers.IO) {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            Uri.encode(phoneNumber)
        )
        val projection = arrayOf(
            ContactsContract.PhoneLookup.CONTACT_ID,
            ContactsContract.PhoneLookup.DISPLAY_NAME
        )

        contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val contactId = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup.CONTACT_ID))
                return@withContext getContact(contactId)
            }
        }
        null
    }

    // ------------------------------------------------
    // ADD CONTACT (MULTI NUMBER)
    // ------------------------------------------------
    override suspend fun addContact(
        firstName: String?,
        lastName: String?,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        emailAddresses: List<String>,
        profilePictureUri: String?,
        isFavorite: Boolean
    ): Unit = withContext(Dispatchers.IO) {

        val ops = ArrayList<ContentProviderOperation>()

        // Raw contact
        ops += ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .withValue(ContactsContract.RawContacts.STARRED, if (isFavorite) 1 else 0)
            .build()

        // Name
        ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
            .build()

        // Phone numbers
        phoneNumbers.forEach { phone ->
            ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.number)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    mapPhoneTypeToSystem(phone.type)
                )
                .build()
        }

        // Email addresses
        emailAddresses.forEach { email ->
            ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_MOBILE)
                .build()
        }

        // Photo
        profilePictureUri?.let {
            getPhotoBytes(it.toUri())?.let { bytes ->
                ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, bytes)
                    .build()
            }
        }

        contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        Unit
    }

    // ------------------------------------------------
    // UPDATE CONTACT (MULTI NUMBER)
    // ------------------------------------------------
    override suspend fun updateContact(
        contactId: String,
        firstName: String?,
        lastName: String?,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        emailAddresses: List<String>,
        profilePictureUri: String?,
        isFavorite: Boolean
    ): Unit = withContext(Dispatchers.IO) {

        val rawContactId = getRawContactId(contactId) ?: return@withContext
        val ops = ArrayList<ContentProviderOperation>()

        // Update starred status
        ops += ContentProviderOperation.newUpdate(ContactsContract.Contacts.CONTENT_URI)
            .withSelection("${ContactsContract.Contacts._ID}=?", arrayOf(contactId))
            .withValue(ContactsContract.Contacts.STARRED, if (isFavorite) 1 else 0)
            .build()

        // Update name
        ops += ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
            .withSelection(
                "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                arrayOf(
                    rawContactId,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
            )
            .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
            .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
            .build()

        // Remove old numbers
        ops += ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
            .withSelection(
                "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                arrayOf(
                    rawContactId,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
            )
            .build()

        // Insert new numbers
        phoneNumbers.forEach { phone ->
            ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.number)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    mapPhoneTypeToSystem(phone.type)
                )
                .build()
        }

        // Remove old emails
        ops += ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
            .withSelection(
                "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                arrayOf(
                    rawContactId,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                )
            )
            .build()

        // Insert new emails
        emailAddresses.forEach { email ->
            ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email)
                .build()
        }

        // Update photo
        profilePictureUri?.let {
            getPhotoBytes(it.toUri())?.let { bytes ->
                // Try to delete existing photo first to simplify (instead of conditional update/insert)
                ops += ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.Data.RAW_CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                        arrayOf(
                            rawContactId,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                        )
                    )
                    .build()

                ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValue(ContactsContract.Data.RAW_CONTACT_ID, rawContactId)
                    .withValue(
                        ContactsContract.Data.MIMETYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                    )
                    .withValue(ContactsContract.CommonDataKinds.Photo.PHOTO, bytes)
                    .build()
            }
        }

        contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        Unit
    }

    private fun getRawContactId(contactId: String): String? {
        val projection = arrayOf(ContactsContract.RawContacts._ID)
        val selection = "${ContactsContract.RawContacts.CONTACT_ID}=?"
        val selectionArgs = arrayOf(contactId)
        val cursor = contentResolver.query(
            ContactsContract.RawContacts.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        return cursor?.use {
            if (it.moveToFirst()) {
                it.getString(it.getColumnIndexOrThrow(ContactsContract.RawContacts._ID))
            } else null
        }
    }

    // ------------------------------------------------
    // DELETE CONTACT
    // ------------------------------------------------
    override suspend fun deleteContact(contactId: String): Unit =
        withContext(Dispatchers.IO) {
            contentResolver.delete(
                ContactsContract.RawContacts.CONTENT_URI,
                "${ContactsContract.RawContacts.CONTACT_ID}=?",
                arrayOf(contactId)
            )
            Unit
        }

    // ------------------------------------------------
    // HELPERS
    // ------------------------------------------------
    private fun mapPhoneType(type: Int): PhoneNumberType = when (type) {
        ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> PhoneNumberType.HOME
        ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> PhoneNumberType.WORK
        else -> PhoneNumberType.MOBILE
    }

    private fun mapPhoneTypeToSystem(type: PhoneNumberType): Int = when (type) {
        PhoneNumberType.HOME -> ContactsContract.CommonDataKinds.Phone.TYPE_HOME
        PhoneNumberType.WORK -> ContactsContract.CommonDataKinds.Phone.TYPE_WORK
        else -> ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
    }

    private fun getPhotoBytes(uri: Uri): ByteArray? =
        try {
            contentResolver.openInputStream(uri)?.use(InputStream::readBytes)
        } catch (e: Exception) {
            null
        }
}

/* ------------------------------------------------
   INTERNAL BUILDER
------------------------------------------------ */

private class MutableContactBuilder(
    val id: String,
    val displayName: String,
    val photoUri: String?,
    val isFavorite: Boolean
) {
    var firstName: String? = null
    var lastName: String? = null
    val phoneNumbers = mutableListOf<PhoneNumber>()
    val emailAddresses = mutableListOf<String>()

    fun build(): Contact = Contact(
        id = id,
        displayName = displayName,
        firstName = firstName,
        lastName = lastName,
        profilePictureUrl = photoUri,
        phoneNumbers = phoneNumbers,
        emailAddresses = emailAddresses,
        isFavorite = isFavorite
    )
}
