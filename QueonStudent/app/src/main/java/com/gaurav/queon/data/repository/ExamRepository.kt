package com.gaurav.queon.data.repository

import com.gaurav.queon.data.remote.ApiClient
import com.gaurav.queon.data.remote.ValidateRequest
import com.gaurav.queon.data.remote.parseQrPayload
import com.gaurav.queon.data.model.ActiveExam

class ExamRepository {

    suspend fun validateEntry(rawQr: String): Result<Pair<String, ActiveExam?>> {
        val payload = parseQrPayload(rawQr)
            ?: return Result.failure(Exception("Invalid QR format"))
            
        return try {
            val res = ApiClient.api.validateEntry(
                ValidateRequest(
                    examId = payload.examId,
                    token = payload.token
                )
            )
            
            if (res.status == "allowed") {
                val exam = ActiveExam(
                    examId = payload.examId,
                    examName = res.examName ?: "Exam",
                    durationMinutes = res.durationMinutes ?: 60,
                    startedAtMillis = System.currentTimeMillis()
                )
                val message = res.message ?: "Entry allowed. Exam started."
                Result.success(message to exam)
            } else {
                val message = res.reason ?: res.message ?: "Entry denied"
                // We return success here because the API call succeeded, but the *logic* was a denial.
                // Or we can return failure. Let's stick closer to the original logic: return message + null exam implies denial.
                // Actually Result<Pair> covers it.
                // Let's throw exception for denial to make specific handling easier? 
                // No, existing logic used Pair response.
                // Let's wrap it nicely.
                 Result.success(message to null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun validateExit(rawQr: String): Result<String> {
        val payload = parseQrPayload(rawQr)
             ?: return Result.failure(Exception("Invalid QR format"))

        return try {
            val res = ApiClient.api.validateExit(
                ValidateRequest(
                    examId = payload.examId,
                    token = payload.token
                )
            )
            Result.success(res.message ?: res.reason ?: "Exit validation complete")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun reportIncident(examId: String, type: String, details: String) {
        try {
            ApiClient.api.reportIncident(
                com.gaurav.queon.data.remote.IncidentRequest(
                    examId = examId,
                    type = type,
                    details = details,
                    timestamp = System.currentTimeMillis()
                )
            )
        } catch (e: Exception) {
            // Best effort logging - don't crash if reporting fails
            e.printStackTrace()
        }
    }
}
