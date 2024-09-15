package com.coderon.phone.viewmodal

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coderon.phone.data.Contact
import com.coderon.phone.data.RecentCall
import com.coderon.phone.data.readContactsFromLocalStorage
import com.coderon.phone.data.readRecentCalls
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ContactViewModal : ViewModel() {

    private val _contactList = MutableStateFlow(listOf<Contact>())
    val contactList = _contactList.asStateFlow()

    private val _recentCallList = MutableStateFlow(listOf<RecentCall>())
    val recentCallList = _recentCallList.asStateFlow()

    fun getRecentCalls(context: Context) {
        viewModelScope.launch {
            _recentCallList.update {
                readRecentCalls(context)
            }
        }
    }

    fun getContactList(context: Context) {
        viewModelScope.launch {
            _contactList.update {
                readContactsFromLocalStorage(context)
            }
        }
    }

}
