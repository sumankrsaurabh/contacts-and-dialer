package com.coderon.phone.domain.repository

import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber

interface ContactRepository {

    suspend fun getContacts(): List<Contact>

    suspend fun getContact(contactId: String): Contact?

    suspend fun addContact(
        firstName: String?,
        lastName: String?,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        emailAddresses: List<String>,
        profilePictureUri: String?,
        isFavorite: Boolean
    )

    suspend fun updateContact(
        contactId: String,
        firstName: String?,
        lastName: String?,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        emailAddresses: List<String>,
        profilePictureUri: String?,
        isFavorite: Boolean
    )

    suspend fun deleteContact(contactId: String)
}
