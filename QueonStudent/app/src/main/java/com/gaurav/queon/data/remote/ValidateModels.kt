package com.gaurav.queon.data.remote

// Body sent to /validate-entry and /validate-exit
data class ValidateRequest(
    val examId: String,
    val token: String
)

// Response from both endpoints
data class ValidateResponse(
    val status: String,          // "allowed" | "denied" | "error"
    val examId: String? = null,
    val examName: String? = null,
    val durationMinutes: Int? = null,
    val message: String? = null,
    val reason: String? = null
)
