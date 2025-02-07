package com.babbira.studentspartner.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babbira.studentspartner.utils.Resource
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _signInState = MutableStateFlow<Resource<FirebaseUser>?>(null)
    val signInState: StateFlow<Resource<FirebaseUser>?> = _signInState

    fun signInWithGoogle(credential: AuthCredential) {
        println("AuthViewModel: Starting sign-in process with Google credential")
        viewModelScope.launch {
            println("AuthViewModel: Launching coroutine for sign-in")
            repository.signInWithGoogle(credential).collect { result ->
                println("AuthViewModel: Received result from repository: $result")
                when(result) {
                    is Resource.Loading -> println("AuthViewModel: Loading state")
                    is Resource.Success -> println("AuthViewModel: Success state with user: ${result.data?.uid}")
                    is Resource.Error -> println("AuthViewModel: Error state with message: ${result.message}")
                }
                _signInState.value = result
            }
        }
    }

    fun getCurrentUser(): FirebaseUser? = repository.getCurrentUser()
    
    fun signOut() = repository.signOut()
} 