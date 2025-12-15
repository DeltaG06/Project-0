package com.gaurav.queon.data.remote

import com.google.gson.Gson

data class QrPayload(
    val examId: String,
    val type: String,  // "ENTRY" or "EXIT"
    val token: String
)

private val gson = Gson()

fun parseQrPayload(raw: String): QrPayload? {
    return try {
        gson.fromJson(raw, QrPayload::class.java)
    } catch (e: Exception) {
        null
    }
}
