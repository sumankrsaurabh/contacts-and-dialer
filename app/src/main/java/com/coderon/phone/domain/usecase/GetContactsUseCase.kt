package com.coderon.phone.domain.usecase

import com.coderon.phone.data.model.Contact
import com.coderon.phone.domain.repository.ContactRepository

class GetContactsUseCase(private val contactRepository: ContactRepository) {
    suspend operator fun invoke(): List<Contact> = contactRepository.getContacts()
}
