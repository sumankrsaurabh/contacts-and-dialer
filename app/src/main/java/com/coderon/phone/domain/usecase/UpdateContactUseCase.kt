package com.coderon.phone.domain.usecase

import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.domain.repository.ContactRepository

class UpdateContactUseCase(private val contactRepository: ContactRepository) {
    suspend operator fun invoke(
        contactId: String,
        firstName: String?,
        lastName: String?,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        emailAddresses: List<String>,
        profilePictureUri: String?,
        isFavorite: Boolean
    ) = contactRepository.updateContact(
        contactId = contactId,
        firstName = firstName,
        lastName = lastName,
        displayName = displayName,
        phoneNumbers = phoneNumbers,
        emailAddresses = emailAddresses,
        profilePictureUri = profilePictureUri,
        isFavorite = isFavorite
    )
}
