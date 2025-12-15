package com.gaurav.queon.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface QueonApi {

    // FULL PATH: http://10.0.2.2:4000/api/exams/validate-entry
    @POST("api/exams/validate-entry")
    suspend fun validateEntry(
        @Body body: ValidateRequest
    ): ValidateResponse

    // FULL PATH: http://192.168.0.103:4000/api/exams/validate-exit
    @POST("api/exams/validate-exit")
    suspend fun validateExit(
        @Body body: ValidateRequest
    ): ValidateResponse

    // FULL PATH: http://192.168.0.103:4000/api/exams/incident
    @POST("api/exams/incident")
    suspend fun reportIncident(
        @Body body: IncidentRequest
    ): IncidentResponse
}

data class IncidentRequest(
    val examId: String,
    val type: String,   // FOCUS_LOST, UNPIN_ATTEMPT, etc.
    val details: String,
    val timestamp: Long
)

data class IncidentResponse(
    val status: String
)
