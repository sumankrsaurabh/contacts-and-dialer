package com.coderon.phone.ui.navigation

sealed class Screen(val route: String) {
    object Keypad : Screen("keypad")
    object Recent : Screen("recent")
    object Contacts : Screen("contacts")
    object Search : Screen("search")
    object AddContact : Screen("add_contact?number={number}&contactId={contactId}") {
        fun createRoute(number: String? = null, contactId: String? = null): String {
            val numParam = if (number != null) "number=$number" else null
            val idParam = if (contactId != null) "contactId=$contactId" else null
            val params = listOfNotNull(numParam, idParam).joinToString("&")
            return if (params.isNotEmpty()) "add_contact?$params" else "add_contact"
        }
    }
    object CallDetails : Screen("contact_details/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "contact_details/$phoneNumber"
    }
    object CallScreen : Screen("call_screen")
    object Settings : Screen("settings")
}
