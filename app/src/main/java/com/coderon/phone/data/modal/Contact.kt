package com.coderon.phone.data.modal

data class Contact(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val profilePictureUrl: String? = null
)
