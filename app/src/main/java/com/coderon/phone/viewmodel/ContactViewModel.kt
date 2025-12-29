package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.domain.repository.ContactRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactViewModel(
    private val contactRepository: ContactRepository
) : ViewModel() {

    // ----------------------------------
    // RAW CONTACTS
    // ----------------------------------
    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())
    val allContacts: StateFlow<List<Contact>> = _allContacts.asStateFlow()

    // ----------------------------------
    // SEARCH QUERY
    // ----------------------------------
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // ----------------------------------
    // GROUPED CONTACTS (A–Z)
    // ----------------------------------
    val groupedContacts: StateFlow<Map<Char, List<Contact>>> =
        combine(_allContacts, _searchQuery) { contacts, query ->
            val filtered = if (query.isBlank()) {
                contacts
            } else {
                contacts.filter { contact ->
                    contact.displayName.contains(query, ignoreCase = true) ||
                            contact.phoneNumbers.any { it.number.contains(query) }
                }
            }

            filtered.groupBy { contact ->
                contact.displayName
                    .firstOrNull()
                    ?.takeIf { it.isLetter() }
                    ?.uppercaseChar()
                    ?: '#'
            }.toSortedMap()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap()
        )

    // ----------------------------------
    // INIT
    // ----------------------------------
    init {
        refreshContacts()
    }

    // ----------------------------------
    // LOAD CONTACTS
    // ----------------------------------
    fun refreshContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = contactRepository.getContacts()
            _allContacts.value = contacts
        }
    }

    // ----------------------------------
    // ADD CONTACT (MULTI NUMBER READY)
    // ----------------------------------
    fun saveContact(
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        profilePictureUri: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.addContact(
                displayName = displayName,
                phoneNumbers = phoneNumbers,
                profilePictureUri = profilePictureUri
            )
            refreshContacts()
        }
    }

    // ----------------------------------
    // UPDATE CONTACT
    // ----------------------------------
    fun updateContact(
        contactId: String,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        profilePictureUri: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.updateContact(
                contactId = contactId,
                displayName = displayName,
                phoneNumbers = phoneNumbers,
                profilePictureUri = profilePictureUri
            )
            refreshContacts()
        }
    }

    // ----------------------------------
    // FIND CONTACT BY NUMBER
    // ----------------------------------
    fun getContactByPhoneNumber(phoneNumber: String): Contact? {
        return _allContacts.value.firstOrNull { contact ->
            contact.phoneNumbers.any { it.number == phoneNumber }
        }
    }

    // ----------------------------------
    // SEARCH
    // ----------------------------------
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
