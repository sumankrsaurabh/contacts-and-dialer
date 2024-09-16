package com.coderon.phone.domain

import com.coderon.phone.data.modal.Contact

interface ContactRepository {
    suspend fun getContacts(): List<Contact>
    suspend fun getContactById(id: Long): Contact?
}

