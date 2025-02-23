package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.model.Contact
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class ContactViewModel(private val contactRepository: ContactRepository) : ViewModel() {

    private val _contacts = MutableStateFlow<Map<Char, List<Contact>>>(emptyMap())
    val contacts: StateFlow<Map<Char, List<Contact>>> = _contacts.asStateFlow()

    init {
        fetchContacts()
    }

    private fun fetchContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = contactRepository.getContacts()
                .groupBy { it.name.firstOrNull()?.uppercaseChar() ?: '#' }
            _contacts.value = contacts
        }
    }

    fun saveContact(name: String, phoneNumber: String, profilePictureUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.addContact(name, phoneNumber, profilePictureUri)
            fetchContacts() // Refresh contacts after adding
        }
    }

    fun getContact(phoneNumber: String): Contact? {
        return _contacts.value.values.flatten().find { it.phoneNumber == phoneNumber }
    }

    fun filteredContacts(query: String): Flow<Map<Char, List<Contact>>> {
        return _contacts.map { contactMap ->
            contactMap.mapValues { (_, contacts) ->
                contacts.filter { contact ->
                    contact.name.contains(query, ignoreCase = true) ||
                            contact.phoneNumber.contains(query)
                }
            }.filterValues { it.isNotEmpty() } // Remove empty groups
        }
    }
}
