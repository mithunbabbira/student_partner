package com.babbira.studentspartner.auth

import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.babbira.studentspartner.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
// ... other imports ...

class AuthRepository(private val auth: FirebaseAuth) {
    fun signInWithGoogle(credential: AuthCredential): Flow<Resource<FirebaseUser>> = flow {
        println("AuthRepository: Starting Google sign-in flow")
        try {
            println("AuthRepository: Emitting Loading state")
            emit(Resource.Loading())
            
            println("AuthRepository: Attempting to sign in with credential")
            val result = auth.signInWithCredential(credential).await()
            
            result.user?.let {
                println("AuthRepository: Sign-in successful. User ID: ${it.uid}")
                emit(Resource.Success(it))
            } ?: run {
                println("AuthRepository: Sign-in failed - No user returned")
                throw Exception("Sign in failed - No user found")
            }
        } catch (e: Exception) {
            println("AuthRepository: Error during sign-in: ${e.message}")
            println("AuthRepository: Stack trace: ${e.stackTraceToString()}")
            emit(Resource.Error(e.message ?: "An unknown error occurred"))
        }
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun signOut() = auth.signOut()

    // ... rest of the Repository code ...
} 