package com.babbira.studentspartner.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SubjectMaterial(
    val id: String = "",
    val title: String = "",
    val filename: String = "",
    val pdfUrl: String = "",
    val addedBy: String = "",
    val userId: String = "",
    val section: String = "",
    val semester: String = "",
    val description: String = "",
    val chapterNumber: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable 