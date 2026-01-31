package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.model.Contact
import com.coderon.phone.data.model.PhoneNumber
import com.coderon.phone.data.repository.SettingsRepository
import com.coderon.phone.domain.repository.ContactRepository
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
import java.util.SortedMap

class ContactViewModel(
    private val getContactsUseCase: GetContactsUseCase,
    private val saveContactUseCase: SaveContactUseCase,
    private val updateContactUseCase: UpdateContactUseCase,
    private val contactRepository: ContactRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _allContacts = MutableStateFlow<List<Contact>>(emptyList())
    val allContacts: StateFlow<List<Contact>> = _allContacts.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredContacts: StateFlow<List<Contact>> =
        combine(_allContacts, _searchQuery) { contacts, query ->
            if (query.isBlank()) emptyList()
            else contacts.filter { contact ->
                contact.displayName.contains(query, ignoreCase = true) ||
                        contact.phoneNumbers.any { it.number.contains(query) }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val groupedContacts: StateFlow<SortedMap<Char, List<Contact>>> =
        combine(_allContacts, _searchQuery, settingsRepository.contactSortOrder) { contacts, query, sortOrder ->
            val filtered = if (query.isBlank()) {
                contacts
            } else {
                contacts.filter { contact ->
                    contact.displayName.contains(query, ignoreCase = true) ||
                            contact.phoneNumbers.any { it.number.contains(query) }
                }
            }

            // sortOrder: 0 for First Name, 1 for Last Name
            val sorted = if (sortOrder == 0) {
                filtered.sortedBy { it.firstName?.lowercase() ?: it.displayName.lowercase() }
            } else {
                filtered.sortedBy { it.lastName?.lowercase() ?: it.displayName.lowercase() }
            }

            sorted.groupBy { contact ->
                val nameForGrouping = if (sortOrder == 0) {
                    contact.firstName ?: contact.displayName
                } else {
                    contact.lastName ?: contact.displayName
                }
                nameForGrouping.firstOrNull()?.uppercaseChar() ?: '#'
            }.toSortedMap()
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyMap<Char, List<Contact>>().toSortedMap()
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

    fun deleteContact(contactId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            contactRepository.deleteContact(contactId)
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

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
