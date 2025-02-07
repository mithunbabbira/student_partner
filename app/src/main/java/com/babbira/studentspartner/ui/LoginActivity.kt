package com.babbira.studentspartner.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.babbira.studentspartner.R
import com.babbira.studentspartner.auth.AuthRepository
import com.babbira.studentspartner.auth.AuthViewModel
import com.babbira.studentspartner.auth.AuthViewModelFactory
import com.babbira.studentspartner.utils.Resource
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.common.SignInButton
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var authViewModel: AuthViewModel
    private lateinit var progressBar: View
    private lateinit var googleSignInButton: SignInButton

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        println("LoginActivity: Sign-in result received with code: ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            println("LoginActivity: Sign-in result OK")
            try {
                println("LoginActivity: Attempting to get credential from intent")
                val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
                println("LoginActivity: Successfully got credential")
                
                val idToken = credential.googleIdToken
                println("LoginActivity: ID Token: ${idToken?.take(10)}... (truncated)")
                
                if (idToken != null) {
                    println("LoginActivity: Creating Firebase credential")
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    println("LoginActivity: Calling ViewModel with Firebase credential")
                    authViewModel.signInWithGoogle(firebaseCredential)
                } else {
                    println("LoginActivity: No ID token found in credential")
                    Toast.makeText(this, "No ID token found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                println("LoginActivity: Error processing sign-in result: ${e.message}")
                println("LoginActivity: Stack trace: ${e.stackTraceToString()}")
                Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            println("LoginActivity: Sign-in result not OK. Result code: ${result.resultCode}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        progressBar = findViewById(R.id.progressBar)
        googleSignInButton = findViewById(R.id.google_sign_in_button)

        setupGoogleSignIn()
        setupViewModel()
        setupClickListeners()
        observeAuthState()
    }

    private fun setupGoogleSignIn() {
        println("LoginActivity: Setting up Google Sign-In")
        oneTapClient = Identity.getSignInClient(this)
        println("LoginActivity: Created OneTapClient")
        
        signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.default_android_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
        println("LoginActivity: Created SignInRequest with Android client ID")
    }

    private fun setupViewModel() {
        val repository = AuthRepository(Firebase.auth)
        val factory = AuthViewModelFactory(repository)
        authViewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]
    }

    private fun setupClickListeners() {
        googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
    }

    private fun signInWithGoogle() {
        println("LoginActivity: Starting Google Sign-In process")
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                println("LoginActivity: Successfully got sign-in intent")
                try {
                    println("LoginActivity: Launching sign-in intent")
                    signInLauncher.launch(
                        IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    )
                    println("LoginActivity: Sign-in intent launched")
                } catch (e: Exception) {
                    println("LoginActivity: Error launching sign-in: ${e.message}")
                    println("LoginActivity: Stack trace: ${e.stackTraceToString()}")
                    Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                println("LoginActivity: Failed to begin sign-in: ${e.message}")
                println("LoginActivity: Stack trace: ${e.stackTraceToString()}")
                Toast.makeText(this, "Sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun observeAuthState() {
        println("LoginActivity: Starting to observe auth state")
        lifecycleScope.launch {
            authViewModel.signInState.collect { resource ->
                println("LoginActivity: Received auth state update: $resource")
                when (resource) {
                    is Resource.Loading -> {
                        println("LoginActivity: Show loading state")
                        progressBar.visibility = View.VISIBLE
                    }
                    is Resource.Success -> {
                        println("LoginActivity: Success state - User ID: ${resource.data?.uid}")
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                        println("LoginActivity: Starting MainActivity")
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                    is Resource.Error -> {
                        println("LoginActivity: Error state - ${resource.message}")
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@LoginActivity, resource.message, Toast.LENGTH_SHORT).show()
                    }
                    null -> {
                        println("LoginActivity: Null state")
                        progressBar.visibility = View.GONE
                    }
                }
            }
        }
    }

    companion object {
        private const val RC_GOOGLE_SIGN_IN = 1001
    }
} 