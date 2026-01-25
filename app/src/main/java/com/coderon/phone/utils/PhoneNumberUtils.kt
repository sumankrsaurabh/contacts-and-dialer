package com.coderon.phone.utils

/**
 * Normalizes a phone number by removing non-numeric characters (except '+')
 * and stripping the '91' or '+91' prefix.
 */
fun normalizePhoneNumber(number: String): String {
    val clean = number.replace(Regex("[^0-9+]"), "")
    return when {
        clean.startsWith("+91") -> clean.substring(3)
        clean.startsWith("91") -> clean.substring(2)
        else -> clean
    }
}
