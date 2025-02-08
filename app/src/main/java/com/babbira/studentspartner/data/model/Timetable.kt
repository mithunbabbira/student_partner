package com.babbira.studentspartner.data.model

data class Timetable(
    val id: String = "",
    val title: String = "",
    val filename: String = "",
    val pdfUrl: String = "",
    val addedBy: String = "",
    val userId: String = "",
    val createdAt: Long = System.currentTimeMillis()
) 