package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.modal.Contact
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(private val contactRepository: ContactRepository) : ViewModel() {
    // Using StateFlow for better state management
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    init {
        fetchContacts()
    }

    private fun fetchContacts() {
        // Ensure data fetching is done asynchronously
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = contactRepository.getContacts()
            withContext(Dispatchers.Main) {
                _contacts.value = contacts
            }
        }

    }

    fun saveContact(name: String, phoneNumber: String, profilePictureUri: String?) {
        viewModelScope.launch {
            contactRepository.addContact(
                name = name,
                phoneNumber = phoneNumber,
                profilePictureUri = profilePictureUri
            )
        }
    }

    fun getContact(phoneNumber: String): Contact? {
        return _contacts.value.firstOrNull { it.phoneNumber == phoneNumber }
//       return contactId?.let { contactRepository.getContact(it) }
    }
}
