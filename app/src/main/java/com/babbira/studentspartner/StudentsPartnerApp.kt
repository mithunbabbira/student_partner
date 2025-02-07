package com.babbira.studentspartner

import androidx.multidex.MultiDexApplication
import com.google.firebase.FirebaseApp


class StudentsPartnerApp : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
} 