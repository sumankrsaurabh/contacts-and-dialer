package com.coderon.phone.data

import androidx.compose.ui.graphics.ImageBitmap

data class Contact(
    val name: String,
    val phoneNumber: String,
    val profilePicture: ImageBitmap?,  // Can be null if no profile picture is set
    val email: String?,                // Email address (nullable)
    val address: String?,              // Physical address (nullable)
    val company: String?,              // Company/Organization (nullable)
    val jobTitle: String?,             // Job title (nullable)
    val notes: String?,                // Notes related to the contact (nullable)
    val ringtone: String?,             // Custom ringtone (nullable)
    val isFavorite: Boolean = false,   // Marks if the contact is a favorite
    val birthday: String?,             // Birthday of the contact (nullable)
    val contactType: ContactType       // Personal, Work, etc.
)

// Enum for contact type (Personal, Work, etc.)
enum class ContactType {
    PERSONAL, WORK, OTHER
}
