package com.babbira.studentspartner.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.babbira.studentspartner.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private val auth = Firebase.auth
    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkUserProfile()
    }

    private fun checkUserProfile() {
        println("MainActivity: Checking user profile")
        val currentUser = auth.currentUser
        if (currentUser == null) {
            println("MainActivity: No user found, redirecting to login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        println("MainActivity: Checking Firestore for user: ${currentUser.uid}")
        db.collection("Users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    println("MainActivity: User profile found")
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    // User exists, stay on MainActivity
                } else {
                    println("MainActivity: No user profile found, redirecting to profile creation")
                    // User doesn't exist, redirect to profile creation
                    startActivity(Intent(this, ViewProfileActivity::class.java))
                    finish()
                }
            }
            .addOnFailureListener { e ->
                println("MainActivity: Error checking user profile: ${e.message}")
                Toast.makeText(this, "Error checking profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
} 