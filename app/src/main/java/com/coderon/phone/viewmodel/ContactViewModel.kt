package com.coderon.phone.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.modal.Contact
import com.coderon.phone.domain.GetContactsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ContactViewModel(private val getContactsUseCase: GetContactsUseCase) : ViewModel() {
    // Using StateFlow for better state management
    private val _contacts = MutableStateFlow<List<Contact>>(emptyList())
    val contacts: StateFlow<List<Contact>> = _contacts

    init {
        fetchContacts()
    }

    private fun fetchContacts() {
        // Ensure data fetching is done asynchronously
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = getContactsUseCase()
            withContext(Dispatchers.Main) {
                _contacts.value = contacts
            }
        }

    }
}
