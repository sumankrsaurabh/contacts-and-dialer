package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.model.Contact
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactViewModel(private val contactRepository: ContactRepository) : ViewModel() {

    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())
    private val _groupedContacts = MutableStateFlow<Map<Char, List<Contact>>>(emptyMap())
    val groupedContacts: StateFlow<Map<Char, List<Contact>>> = _groupedContacts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredContacts: StateFlow<Map<Char, List<Contact>>> = combine(
        _groupedContacts,
        _searchQuery
    ) { grouped, query ->
        if (query.isBlank()) return@combine grouped
        grouped.mapValues { (_, contacts) ->
            contacts.filter {
                it.name.contains(query, ignoreCase = true) ||
                        it.phoneNumber.contains(query)
            }
        }.filterValues { it.isNotEmpty() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    init {
        fetchContacts()
    }

    private fun fetchContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = contactRepository.getContacts()
            _allContacts.value = contacts
            _groupedContacts.value = groupContacts(contacts)
        }
    }

    private fun groupContacts(contacts: List<Contact>): Map<Char, List<Contact>> {
        return contacts.groupBy { contact ->
            contact.name.firstOrNull()?.takeIf { it.isLetter() }?.uppercaseChar() ?: '#'
        }.toSortedMap()
    }

    fun saveContact(name: String, phoneNumber: String, profilePictureUri: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.addContact(name, phoneNumber, profilePictureUri)
            fetchContacts()
        }
    }

    fun getContact(phoneNumber: String): Contact? {
        return _allContacts.value.find { it.phoneNumber == phoneNumber }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
