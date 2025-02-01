package com.coderon.phone.domain

import com.coderon.phone.domain.repository.ContactRepository

class GetContactsUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke() = repository.getContacts()
}
