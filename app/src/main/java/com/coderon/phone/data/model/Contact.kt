package com.coderon.phone.data.model

data class Contact(
    val id: String = "",
    val displayName: String = "",
    val firstName: String? = null,
    val lastName: String? = null,

    // A contact can have multiple numbers
    val phoneNumbers: List<PhoneNumber> = emptyList(),

    val emailAddresses: List<String> = emptyList(),
    val profilePictureUrl: String? = null,

    val isFavorite: Boolean = false,
    val isBlocked: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class PhoneNumber(
    val number: String,
    val type: PhoneNumberType = PhoneNumberType.MOBILE,
    val isPrimary: Boolean = false
)

enum class PhoneNumberType {
    MOBILE, HOME, WORK, OTHER
}
