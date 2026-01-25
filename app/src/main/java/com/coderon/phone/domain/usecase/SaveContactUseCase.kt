package com.coderon.phone.domain.usecase

import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.domain.repository.ContactRepository

class SaveContactUseCase(private val contactRepository: ContactRepository) {
    suspend operator fun invoke(
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        profilePictureUri: String?
    ) = contactRepository.addContact(displayName, phoneNumbers, profilePictureUri)
}
