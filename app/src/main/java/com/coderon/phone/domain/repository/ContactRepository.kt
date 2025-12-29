package com.coderon.phone.domain.repository

import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber

interface ContactRepository {

    suspend fun getContacts(): List<Contact>

    suspend fun getContact(contactId: String): Contact?

    suspend fun addContact(
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        profilePictureUri: String?
    )

    suspend fun updateContact(
        contactId: String,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        profilePictureUri: String?
    )

    suspend fun deleteContact(contactId: String)
}
