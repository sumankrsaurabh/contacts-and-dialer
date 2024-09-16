package com.coderon.phone.domain

class GetContactsUseCase(private val repository: ContactRepository) {
    suspend operator fun invoke() = repository.getContacts()
}
