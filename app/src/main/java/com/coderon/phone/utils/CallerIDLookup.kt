package com.coderon.phone.utils

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object CallerIDLookup {
    suspend fun lookupPhoneNumber(context: Context, phoneNumber: String): String {
        return withContext(Dispatchers.IO) {
            try {
                val apiUrl = "https://api.numverify.com/v2/validate?access_key=YOUR_API_KEY&number=$phoneNumber"
                val url = URL(apiUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"

                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonObject = JSONObject(response)

                if (jsonObject.getBoolean("valid")) {
                    jsonObject.getString("carrier") ?: "Unknown Caller"
                } else {
                    "Spam / Unknown"
                }
            } catch (e: Exception) {
                "Unknown Caller"
            }
        }
    }
}
