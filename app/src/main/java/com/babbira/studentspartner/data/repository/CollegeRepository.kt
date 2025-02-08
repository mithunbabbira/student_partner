package com.babbira.studentspartner.data.repository

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import android.util.Log

import com.babbira.studentspartner.data.model.UserProfile
import com.google.firebase.ktx.Firebase


class CollegeRepository {
    private val db = Firebase.firestore
    private val TAG = "CollegeRepository"
    private val auth = FirebaseAuth.getInstance()

    suspend fun getColleges(): Result<List<String>> = try {
        val documents = db.collection("collegeList").get().await()
        Result.success(documents.map { it.id })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun combination(college: String): Result<List<String>> {
        return try {
            Log.d(TAG, "Getting combination for college: $college")
            
            // First check if the college document exists
            val collegeRef = db.collection("collegeList").document(college)
            val collegeDoc = collegeRef.get().await()
            
            Log.d(TAG, "College document exists: ${collegeDoc.exists()}")
            
            if (!collegeDoc.exists()) {
                Log.w(TAG, "College document doesn't exist: $college")
                Result.success(emptyList())
            } else {
                // Then get the combination collection
                val combinationsRef = collegeRef.collection("combination")
                Log.d(TAG, "combination collection path: ${combinationsRef.path}")
                
                val combination = combinationsRef.get().await()
                Log.d(TAG, "Found ${combination.documents.size} combination")
                Log.d(TAG, "combination IDs: ${combination.documents.map { it.id }}")
                
                Result.success(combination.documents.map { it.id })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting combination", e)
            Result.failure(e)
        }
    }

    suspend fun getSemesters(college: String, combination: String): Result<List<String>> {
        return try {
            Log.d(TAG, "Getting semesters for college: $college, combination: $combination")
            
            // First check if the combination document exists
            val combinationRef = db.collection("collegeList")
                .document(college)
                .collection("combinations")
                .document(combination)
            
            val combinationDoc = combinationRef.get().await()
            Log.d(TAG, "combination document exists: ${combinationDoc.exists()}")
            
            if (!combinationDoc.exists()) {
                Log.w(TAG, "combination document doesn't exist: $combination")
                Result.success(emptyList())
            } else {
                // Then get the semesters collection
                val semestersRef = combinationRef.collection("semesters")
                Log.d(TAG, "Semesters collection path: ${semestersRef.path}")
                
                val semesters = semestersRef.get().await()
                Log.d(TAG, "Found ${semesters.documents.size} semesters")
                Log.d(TAG, "Semester IDs: ${semesters.documents.map { it.id }}")
                
                Result.success(semesters.documents.map { it.id })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting semesters", e)
            Result.failure(e)
        }
    }

    suspend fun addCollege(collegeName: String): Result<Unit> = try {
        val data = hashMapOf<String, Any>()
        db.collection("collegeList")
            .document(collegeName)
            .set(data)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun addCombination(college: String, combination: String): Result<Unit> = try {
        Log.d(TAG, "Adding combination: $combination to college: $college")
        
        // First ensure college document exists
        val collegeRef = db.collection("collegeList").document(college)
        val collegeExists = collegeRef.get().await().exists()
        Log.d(TAG, "College exists: $collegeExists")
        
        if (!collegeExists) {
            Log.d(TAG, "Creating college document: $college")
            collegeRef.set(hashMapOf<String, Any>()).await()
        }

        // Then add the combination document
        val combinationRef = collegeRef.collection("combination").document(combination)
        Log.d(TAG, "Adding combination at path: ${combinationRef.path}")

        combinationRef.set(hashMapOf<String, Any>()).await()
        Log.d(TAG, "Successfully added combination: $combination")

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error adding combination", e)
        Result.failure(e)
    }

    suspend fun updateUserProfile(userProfile: HashMap<String, Any>): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not logged in"))
            }

            Log.d(TAG, "Updating profile for user: ${currentUser.uid}")
            
            db.collection("users")
                .document(currentUser.uid)
                .set(userProfile)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating profile", e)
            Result.failure(e)
        }
    }

    suspend fun getUserProfile(): Result<UserProfile?> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return Result.failure(Exception("User not logged in"))
            }

            Log.d(TAG, "Fetching profile for user: ${currentUser.uid}")
            
            val document = db.collection("users")
                .document(currentUser.uid)
                .get()
                .await()

            if (document.exists()) {
                val profile = document.toObject(UserProfile::class.java)
                Log.d(TAG, "Found profile: $profile")
                Result.success(profile)
            } else {
                Log.d(TAG, "No profile found for user")
                Result.success(null)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching profile", e)
            Result.failure(e)
        }
    }
} 