package com.babbira.studentspartner.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babbira.studentspartner.data.model.UserProfile
import com.babbira.studentspartner.data.repository.CollegeRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FieldValue

class ViewProfileViewModel(
    private val repository: CollegeRepository = CollegeRepository()
) : ViewModel() {
    private val TAG = "ViewProfileViewModel"

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _colleges = MutableLiveData<List<String>>()
    val colleges: LiveData<List<String>> = _colleges

    private val _combination = MutableLiveData<List<String>>()
    val combination: LiveData<List<String>> = _combination

    private val _semesters = MutableLiveData<List<String>>()
    val semesters: LiveData<List<String>> = _semesters

    private val _sections = MutableLiveData<List<String>>()
    val sections: LiveData<List<String>> = _sections

    private val _showAddCollegeButton = MutableLiveData<Boolean>()
    val showAddCollegeButton: LiveData<Boolean> = _showAddCollegeButton

    private val _showAddCombinationButton = MutableLiveData<Boolean>()
    val showAddCombinationButton: LiveData<Boolean> = _showAddCombinationButton

    private val _showAddSemesterButton = MutableLiveData<Boolean>()
    val showAddSemesterButton: LiveData<Boolean> = _showAddSemesterButton

    private val _showAddSectionButton = MutableLiveData<Boolean>()
    val showAddSectionButton: LiveData<Boolean> = _showAddSectionButton

    private var typedCollegeName: String = ""
    private var typedCombinationName: String = ""
    private var typedSemesterName: String = ""
    private var typedSectionName: String = ""
    private var selectedCollege: String = ""
    private var selectedCombination: String = ""
    private var selectedSemester: String = ""

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isProfileValid = MutableLiveData<Boolean>()
    val isProfileValid: LiveData<Boolean> = _isProfileValid

    private var userName: String = ""
    private var phoneNumber: String = ""
    private var selectedSection: String = ""

    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()

    private val _isUpdating = MutableLiveData<Boolean>()
    val isUpdating: LiveData<Boolean> = _isUpdating

    init {
        fetchUserProfile()
    }

    private fun fetchUserProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            repository.getUserProfile()
                .onSuccess { profile ->
                    _userProfile.value = profile
                    profile?.let {
                        // Update selected values
                        selectedCollege = it.college ?: ""
                        selectedCombination = it.combination ?: ""
                        selectedSemester = it.semester ?: ""
                        selectedSection = it.section ?: ""
                        userName = it.name ?: ""
                        phoneNumber = it.phone ?: ""
                        validateProfile()
                    }
                    _isLoading.value = false
                }
                .onFailure { error ->
                    _error.value = error.message
                    _isLoading.value = false
                }
        }
    }

    fun updateProfile() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userId = auth.currentUser?.uid ?: return@launch
                _userProfile.value?.let { profile ->
                    db.collection("Users").document(userId).set(profile).await()
                    _errorMessage.value = "Profile updated successfully"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchColleges() {
        viewModelScope.launch {
            repository.getColleges()
                .onSuccess { 
                    _colleges.value = it
                    _showAddCollegeButton.value = false
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun checkIfCollegeExists(typedText: String) {
        val currentColleges = _colleges.value ?: emptyList()
        val collegeExists = currentColleges.any { 
            it.equals(typedText.trim(), ignoreCase = true) 
        }
        
        typedCollegeName = typedText.trim()
        _showAddCollegeButton.value = !collegeExists && typedText.isNotEmpty()
        
        // Update the filtered list
        _colleges.value = currentColleges.filter { 
            it.contains(typedText, ignoreCase = true) 
        }
    }

    fun addNewCollege(collegeName: String) {
        viewModelScope.launch {
            repository.addCollege(collegeName)
                .onSuccess { 
                    fetchColleges() // Refresh the list
                    _error.value = "College added successfully"
                }
                .onFailure { _error.value = it.message }
        }
    }

    fun fetchCombination(college: String) {
        Log.d(TAG, "Fetching combination for college: $college")
        viewModelScope.launch {
            repository.combination(college)
                .onSuccess { combination ->
                    Log.d(TAG, "Successfully fetched ${combination.size} combination")
                    _combination.value = combination
                }
                .onFailure { error ->
                    Log.e(TAG, "Error fetching combination", error)
                    _error.value = error.message
                }
        }
    }

    fun fetchSemesters(college: String, combination: String) {
        viewModelScope.launch {
            // Instead of fetching from repository, we'll use local list
            val semesters = (1..10).map { "${it}st" }
            _semesters.value = semesters
        }
    }

    fun fetchSections(college: String, combination: String, semester: String) {
        viewModelScope.launch {
            // Instead of fetching from repository, we'll use local list
            val sections = ('A'..'Z').map { it.toString() }
            _sections.value = sections
        }
    }

    fun getTypedCollegeName(): String = typedCollegeName

    fun setSelectedCollege(college: String) {
        Log.d(TAG, "Setting selected college: $college")
        selectedCollege = college
        validateProfile()
    }

    fun checkIfCombinationExists(typedText: String) {
        val currentCombination = _combination.value ?: emptyList()
        val combinationExists = currentCombination.any {
            it.equals(typedText.trim(), ignoreCase = true) 
        }
        
        typedCombinationName = typedText.trim()
        _showAddCombinationButton.value = !combinationExists && typedText.isNotEmpty()
        
        // Update the filtered list
        _combination.value = currentCombination.filter {
            it.contains(typedText, ignoreCase = true) 
        }
    }

    fun addNewCombination(combinationName: String) {
        Log.d(TAG, "Adding new combination: $combinationName to college: $selectedCollege")
        viewModelScope.launch {
            repository.addCombination(selectedCollege, combinationName)
                .onSuccess { 
                    Log.d(TAG, "Successfully added combination: $combinationName")
                    fetchCombination(selectedCollege)
                    _error.value = "combination added successfully"
                }
                .onFailure { error ->
                    Log.e(TAG, "Error adding combination", error)
                    _error.value = error.message
                }
        }
    }

    fun setSelectedCombination(combination: String) {
        Log.d(TAG, "Setting selected combination: $combination")
        selectedCombination = combination
        validateProfile()
    }

    fun setSelectedSemester(semester: String) {
        selectedSemester = semester
        validateProfile()
    }

    fun checkIfSemesterExists(typedText: String) {
        val currentSemesters = _semesters.value ?: emptyList()
        val semesterExists = currentSemesters.any { 
            it.equals(typedText.trim(), ignoreCase = true) 
        }
        
        typedSemesterName = typedText.trim()
        _showAddSemesterButton.value = !semesterExists && typedText.isNotEmpty()
        
        _semesters.value = currentSemesters.filter { 
            it.contains(typedText, ignoreCase = true) 
        }
    }

    fun checkIfSectionExists(typedText: String) {
        val currentSections = _sections.value ?: emptyList()
        val sectionExists = currentSections.any { 
            it.equals(typedText.trim(), ignoreCase = true) 
        }
        
        typedSectionName = typedText.trim()
        _showAddSectionButton.value = !sectionExists && typedText.isNotEmpty()
        
        _sections.value = currentSections.filter { 
            it.contains(typedText, ignoreCase = true) 
        }
    }

    fun validateProfile() {
        val isValid = userName.isNotEmpty() &&
                phoneNumber.isNotEmpty() &&
                selectedCollege.isNotEmpty() &&
                selectedCombination.isNotEmpty() &&
                selectedSemester.isNotEmpty() &&
                selectedSection.isNotEmpty()

        _isProfileValid.value = isValid
    }

    fun setUserName(name: String) {
        userName = name.trim()
        validateProfile()
    }

    fun setPhoneNumber(phone: String) {
        phoneNumber = phone.trim()
        validateProfile()
    }

    fun setSelectedSection(section: String) {
        selectedSection = section
        validateProfile()
    }

    fun updateProfile(name: String, phone: String) {
        if (!isProfileValid.value!!) return

        _isUpdating.value = true
        viewModelScope.launch {
            val userProfile = hashMapOf(
                "name" to name,
                "phone" to phone,
                "college" to selectedCollege,
                "combination" to selectedCombination,
                "semester" to selectedSemester,
                "section" to selectedSection,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            repository.updateUserProfile(userProfile)
                .onSuccess { 
                    _error.value = "Profile updated successfully"
                    _isUpdating.value = false
                }
                .onFailure { error ->
                    _error.value = error.message ?: "Failed to update profile"
                    _isUpdating.value = false
                }
        }
    }
} 