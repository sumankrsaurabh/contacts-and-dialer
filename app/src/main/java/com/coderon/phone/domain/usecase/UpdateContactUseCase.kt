package com.coderon.phone.domain.usecase

import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.domain.repository.ContactRepository

class UpdateContactUseCase(private val contactRepository: ContactRepository) {
    suspend operator fun invoke(
        contactId: String,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        profilePictureUri: String?
    ) = contactRepository.updateContact(contactId, displayName, phoneNumbers, profilePictureUri)
}
