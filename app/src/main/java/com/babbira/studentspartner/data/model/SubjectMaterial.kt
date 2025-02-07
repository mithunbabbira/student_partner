package com.babbira.studentspartner.data.model

data class SubjectMaterial(
    val id: String = "",
    val title: String = "",
    val pdfUrl: String = "",
    val addedBy: String = "",
    val section: String = "",
    val semester: String = "",
    val description: String = "",
    val chapterNumber: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) 