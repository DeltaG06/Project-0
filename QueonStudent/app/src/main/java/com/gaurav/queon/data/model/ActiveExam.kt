package com.gaurav.queon.data.model

data class ActiveExam(
    val examId: String,
    val examName: String,
    val durationMinutes: Int,
    val startedAtMillis: Long
)
