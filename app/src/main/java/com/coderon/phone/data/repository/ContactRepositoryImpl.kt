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
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            CONTACT_PROJECTION,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIndex =
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex =
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val numberIndex =
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIndex =
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE)
            val photoIndex =
                it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Photo.PHOTO_URI)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex) ?: "Unknown"
                val number = it.getString(numberIndex) ?: continue
                val type = it.getInt(typeIndex)
                val photoUri = it.getString(photoIndex)

                val builder = contactMap.getOrPut(id) {
                    MutableContactBuilder(
                        id = id,
                        displayName = name,
                        photoUri = photoUri
                    )
                }

                builder.phoneNumbers.add(
                    PhoneNumber(
                        number = number,
                        type = mapPhoneType(type),
                        isPrimary = builder.phoneNumbers.isEmpty()
                    )
                )
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

    // ------------------------------------------------
    // ADD CONTACT (MULTI NUMBER)
    // ------------------------------------------------
    override suspend fun addContact(
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        profilePictureUri: String?
    ): Unit = withContext(Dispatchers.IO) {

        val ops = ArrayList<ContentProviderOperation>()

        // Raw contact
        ops += ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
            .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
            .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
            .build()

        // Name
        ops += ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
            .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
            .withValue(
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
            )
            .withValue(
                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                displayName
            )
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
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        profilePictureUri: String?
    ): Unit = withContext(Dispatchers.IO) {

        val ops = ArrayList<ContentProviderOperation>()

        // Update name
        ops += ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
            .withSelection(
                "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                arrayOf(
                    contactId,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
            )
            .withValue(
                ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                displayName
            )
            .build()

        // Remove old numbers
        ops += ContentProviderOperation.newDelete(ContactsContract.Data.CONTENT_URI)
            .withSelection(
                "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                arrayOf(
                    contactId,
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
                .withValue(ContactsContract.Data.CONTACT_ID, contactId)
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.number)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    mapPhoneTypeToSystem(phone.type)
                )
                .build()
        }

        // Update photo
        profilePictureUri?.let {
            getPhotoBytes(it.toUri())?.let { bytes ->
                ops += ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI)
                    .withSelection(
                        "${ContactsContract.Data.CONTACT_ID}=? AND ${ContactsContract.Data.MIMETYPE}=?",
                        arrayOf(
                            contactId,
                            ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                        )
                    )
                    .withValue(
                        ContactsContract.CommonDataKinds.Photo.PHOTO,
                        bytes
                    )
                    .build()
            }
        }

        contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
        Unit
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

    companion object {
        private val CONTACT_PROJECTION = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Photo.PHOTO_URI
        )
    }
}

/* ------------------------------------------------
   INTERNAL BUILDER
------------------------------------------------ */

private class MutableContactBuilder(
    val id: String,
    val displayName: String,
    val photoUri: String?
) {
    val phoneNumbers = mutableListOf<PhoneNumber>()

    fun build(): Contact = Contact(
        id = id,
        displayName = displayName,
        profilePictureUrl = photoUri,
        phoneNumbers = phoneNumbers
    )
}
