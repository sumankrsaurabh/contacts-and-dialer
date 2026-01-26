package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.domain.usecase.GetContactsUseCase
import com.coderon.phone.domain.usecase.SaveContactUseCase
import com.coderon.phone.domain.usecase.UpdateContactUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ContactViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val saveContactUseCase: SaveContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase
) : ViewModel() {

    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())
    val allContacts: StateFlow<List<Contact>> = _allContacts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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

    init {
        refreshContacts()
    }

    fun refreshContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = getContactsUseCase()
            _allContacts.value = contacts
        }
    }

    fun saveContact(
        firstName: String?,
        lastName: String?,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        emailAddresses: List<String>,
        profilePictureUri: String?,
        isFavorite: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            saveContactUseCase(
                firstName = firstName,
                lastName = lastName,
                displayName = displayName,
                phoneNumbers = phoneNumbers,
                emailAddresses = emailAddresses,
                profilePictureUri = profilePictureUri,
                isFavorite = isFavorite
            )
            refreshContacts()
        }
    }

    fun updateContact(
        contactId: String,
        firstName: String?,
        lastName: String?,
        displayName: String,
        phoneNumbers: List<PhoneNumber>,
        emailAddresses: List<String>,
        profilePictureUri: String?,
        isFavorite: Boolean
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            updateContactUseCase(
                contactId = contactId,
                firstName = firstName,
                lastName = lastName,
                displayName = displayName,
                phoneNumbers = phoneNumbers,
                emailAddresses = emailAddresses,
                profilePictureUri = profilePictureUri,
                isFavorite = isFavorite
            )
            refreshContacts()
        }
    }

    fun toggleFavorite(contact: Contact) {
        updateContact(
            contactId = contact.id,
            firstName = contact.firstName,
            lastName = contact.lastName,
            displayName = contact.displayName,
            phoneNumbers = contact.phoneNumbers,
            emailAddresses = contact.emailAddresses,
            profilePictureUri = contact.profilePictureUrl,
            isFavorite = !contact.isFavorite
        )
    }

    fun getContactByPhoneNumber(phoneNumber: String): Contact? {
        return _allContacts.value.firstOrNull { contact ->
            contact.phoneNumbers.any { it.number == phoneNumber }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
