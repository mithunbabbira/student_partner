package com.babbira.studentspartner.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await
import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.babbira.studentspartner.data.model.UserProfile



class CollegeRepository {
    private val db = Firebase.firestore
    private val TAG = "CollegeRepository"
    private val auth = Firebase.auth

    suspend fun getColleges(): Result<List<String>> = try {
        val documents = db.collection("collegeList").get().await()
        Result.success(documents.map { it.id })
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getSubjects(college: String): Result<List<String>> {
        return try {
            Log.d(TAG, "Getting subjects for college: $college")
            
            // First check if the college document exists
            val collegeRef = db.collection("collegeList").document(college)
            val collegeDoc = collegeRef.get().await()
            
            Log.d(TAG, "College document exists: ${collegeDoc.exists()}")
            
            if (!collegeDoc.exists()) {
                Log.w(TAG, "College document doesn't exist: $college")
                Result.success(emptyList())
            } else {
                // Then get the subjects collection
                val subjectsRef = collegeRef.collection("subjects")
                Log.d(TAG, "Subjects collection path: ${subjectsRef.path}")
                
                val subjects = subjectsRef.get().await()
                Log.d(TAG, "Found ${subjects.documents.size} subjects")
                Log.d(TAG, "Subject IDs: ${subjects.documents.map { it.id }}")
                
                Result.success(subjects.documents.map { it.id })
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting subjects", e)
            Result.failure(e)
        }
    }

    suspend fun getSemesters(college: String, subject: String): Result<List<String>> {
        return try {
            Log.d(TAG, "Getting semesters for college: $college, subject: $subject")
            
            // First check if the subject document exists
            val subjectRef = db.collection("collegeList")
                .document(college)
                .collection("subjects")
                .document(subject)
            
            val subjectDoc = subjectRef.get().await()
            Log.d(TAG, "Subject document exists: ${subjectDoc.exists()}")
            
            if (!subjectDoc.exists()) {
                Log.w(TAG, "Subject document doesn't exist: $subject")
                Result.success(emptyList())
            } else {
                // Then get the semesters collection
                val semestersRef = subjectRef.collection("semesters")
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

    suspend fun addSubject(college: String, subject: String): Result<Unit> = try {
        Log.d(TAG, "Adding subject: $subject to college: $college")
        
        // First ensure college document exists
        val collegeRef = db.collection("collegeList").document(college)
        val collegeExists = collegeRef.get().await().exists()
        Log.d(TAG, "College exists: $collegeExists")
        
        if (!collegeExists) {
            Log.d(TAG, "Creating college document: $college")
            collegeRef.set(hashMapOf<String, Any>()).await()
        }

        // Then add the subject document
        val subjectRef = collegeRef.collection("subjects").document(subject)
        Log.d(TAG, "Adding subject at path: ${subjectRef.path}")
        
        subjectRef.set(hashMapOf<String, Any>()).await()
        Log.d(TAG, "Successfully added subject: $subject")

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e(TAG, "Error adding subject", e)
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