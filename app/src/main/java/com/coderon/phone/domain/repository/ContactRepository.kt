package com.coderon.phone.domain.repository

import com.coderon.phone.data.modal.Contact

interface ContactRepository {
    suspend fun getContacts(): List<Contact>
    suspend fun addContact(name: String, phoneNumber: String)
    suspend fun deleteContact(contactId: String)
    suspend fun updateContact(contactId: String, name: String, phoneNumber: String)
}