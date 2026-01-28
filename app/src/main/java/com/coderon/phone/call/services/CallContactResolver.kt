package com.coderon.phone.call.services

import android.util.Log
import com.coderon.phone.data.model.Contact
import com.coderon.phone.domain.repository.ContactRepository

class CallContactResolver(private val contactRepository: ContactRepository) {
    suspend fun resolveContact(number: String): Contact? {
        return try {
            contactRepository.getContactByNumber(number)
        } catch (e: Exception) {
            Log.e("CallContactResolver", "Failed to resolve contact", e)
            null
        }
    }
}
