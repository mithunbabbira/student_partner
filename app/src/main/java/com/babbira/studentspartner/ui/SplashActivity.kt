package com.babbira.studentspartner.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.ktx.Firebase

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is signed in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is signed in, go to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            // No user is signed in, go to LoginActivity
            startActivity(Intent(this, LoginActivity::class.java))
        }
        
        // Close splash activity
        finish()
    }
} 