package com.babbira.studentspartner.ui

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.babbira.studentspartner.R
import com.babbira.studentspartner.data.model.UserProfile
import com.babbira.studentspartner.databinding.ActivityViewProfileBinding
import com.babbira.studentspartner.databinding.DialogAddItemBinding
import com.babbira.studentspartner.utils.DialogUtils
import com.babbira.studentspartner.utils.ImageUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.bumptech.glide.Glide

class ViewProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewProfileBinding
    private val viewModel: ViewProfileViewModel by viewModels()
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference
    private var selectedImageUri: Uri? = null
    
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.profileImageView.setImageURI(it)
            uploadProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize adapters first
        setupCollegeAdapter()
        setupCombinationAdapter()
        
        setupImagePicker()
        setupDropdownListeners()
        setupTextChangeListeners()
        setupUpdateButton()
        observeViewModel()
    }

    private fun setupCollegeAdapter() {
        // Create and set adapter immediately with empty list
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )
        binding.collegeAutoComplete.setAdapter(adapter)
        binding.collegeAutoComplete.threshold = 1

        // Observe colleges and update adapter data
        viewModel.colleges.observe(this) { colleges ->
            adapter.clear()
            adapter.addAll(colleges)
            adapter.notifyDataSetChanged()
        }

        // Fetch colleges after setting up observation
        viewModel.fetchColleges()

        binding.collegeAutoComplete.setOnItemClickListener { _, _, _, _ ->
            binding.combinationAutoComplete.text?.clear()
            val selectedCollege = binding.collegeAutoComplete.text.toString()
            viewModel.fetchCombination(selectedCollege)
        }
    }

    private fun setupCombinationAdapter() {
        // Create and set adapter immediately with empty list
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            mutableListOf<String>()
        )
        binding.combinationAutoComplete.setAdapter(adapter)
        binding.combinationAutoComplete.threshold = 1

        // Observe combinations and update adapter data
        viewModel.combination.observe(this) { combinations ->
            adapter.clear()
            adapter.addAll(combinations)
            adapter.notifyDataSetChanged()
        }
    }

    private fun setupImagePicker() {
        binding.editProfileImageButton.setOnClickListener {
            showImagePickerDialog()
        }
    }

    private fun showImagePickerDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Profile Picture")
            .setItems(arrayOf("Choose from Gallery", "Remove Photo")) { _, which ->
                when (which) {
                    0 -> getContent.launch("image/*")
                    1 -> removeProfileImage()
                }
            }
            .show()
    }

    private fun uploadProfileImage(imageUri: Uri) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userCollege = binding.collegeAutoComplete.text.toString()
        val userCombination = binding.combinationAutoComplete.text.toString()

        if (userCollege.isEmpty() || userCombination.isEmpty()) {
            Toast.makeText(this, "Please select college and combination first", Toast.LENGTH_SHORT).show()
            return
        }

        // Compress and resize image using utility
        val compressedImage = ImageUtils.compressImage(imageUri, contentResolver)
        if (compressedImage == null) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
            return
        }

        val imageRef = storageRef.child("profile_pics/$userCollege/$userCombination/$userId.jpg")
        binding.progressBar.visibility = View.VISIBLE

        // Upload compressed image
        imageRef.putBytes(compressedImage)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateProfileImageUrl(downloadUri.toString())
                }
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to upload image: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to update profile: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userCollege = binding.collegeAutoComplete.text.toString()
        val userCombination = binding.combinationAutoComplete.text.toString()

        if (userCollege.isEmpty() || userCombination.isEmpty()) {
            Toast.makeText(this, "College or combination not found", Toast.LENGTH_SHORT).show()
            return
        }

        val imageRef = storageRef.child("profile_pics/$userCollege/$userCombination/$userId.jpg")
        
        binding.progressBar.visibility = View.VISIBLE

        imageRef.delete().addOnCompleteListener { storageTask ->
            val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)
            userRef.update("profileImageUrl", null)
                .addOnCompleteListener { firestoreTask ->
                    binding.progressBar.visibility = View.GONE
                    if (storageTask.isSuccessful && firestoreTask.isSuccessful) {
                        binding.profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
                        Toast.makeText(this, "Profile picture removed", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to remove profile picture", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun loadProfileImage(imageUrl: String?) {
        if (imageUrl != null) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(binding.profileImageView)
        } else {
            binding.profileImageView.setImageResource(R.drawable.ic_profile_placeholder)
        }
    }

    private fun setupDropdownListeners() {
        // College setup
        binding.collegeInputLayout.apply {
            setEndIconOnClickListener {
                val newCollegeName = binding.collegeAutoComplete.text.toString().trim()
                if (newCollegeName.isNotEmpty()) {
                    showAddCollegeDialog(newCollegeName)
                }
            }
        }

        binding.collegeAutoComplete.apply {
            setOnItemClickListener { _, _, _, _ ->
                val selectedCollege = text.toString()
                clearDependentFields()
                viewModel.setSelectedCollege(selectedCollege)
                viewModel.fetchCombination(selectedCollege)
                binding.collegeInputLayout.setEndIconVisible(false)
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.isEmpty()) {
                        viewModel.fetchColleges()
                        binding.collegeInputLayout.setEndIconVisible(false)
                    } else {
                        viewModel.checkIfCollegeExists(text)
                    }
                }
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showDropDown()
                }
            }

            setOnClickListener {
                showDropDown()
            }
        }

        // Combination setup
        binding.combinationInputLayout.apply {
            setEndIconOnClickListener {
                val newCombinationName = binding.combinationAutoComplete.text.toString().trim()
                if (newCombinationName.isNotEmpty()) {
                    showAddCombinationDialog(newCombinationName)
                }
            }
        }

        binding.combinationAutoComplete.apply {
            setOnItemClickListener { _, _, _, _ ->
                val selectedCombination = text.toString()
                clearSemesterAndSection()
                viewModel.setSelectedCombination(selectedCombination)
                viewModel.fetchSemesters(
                    binding.collegeAutoComplete.text.toString(),
                    selectedCombination
                )
                binding.combinationInputLayout.setEndIconVisible(false)
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.isEmpty()) {
                        viewModel.fetchCombination(binding.collegeAutoComplete.text.toString())
                        binding.combinationInputLayout.setEndIconVisible(false)
                    } else {
                        viewModel.checkIfCombinationExists(text)
                    }
                }
            })

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showDropDown()
                }
            }

            setOnClickListener {
                showDropDown()
            }
        }

        // Update semester setup with fixed values
        binding.semesterInputLayout.apply {
            endIconMode = TextInputLayout.END_ICON_NONE // Remove the add button
        }

        binding.semesterAutoComplete.apply {
            val semesters = (1..10).map { "${it}st" }
            val adapter = ArrayAdapter(
                this@ViewProfileActivity,
                android.R.layout.simple_dropdown_item_1line,
                semesters
            )
            setAdapter(adapter)

            setOnItemClickListener { _, _, _, _ ->
                val selectedSemester = text.toString()
                viewModel.setSelectedSemester(selectedSemester)
                clearSection()
                viewModel.fetchSections(
                    binding.collegeAutoComplete.text.toString(),
                    binding.combinationAutoComplete.text.toString(),
                    selectedSemester
                )
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showDropDown()
                }
            }

            setOnClickListener {
                showDropDown()
            }
        }

        // Update section setup with fixed values
        binding.sectionInputLayout.apply {
            endIconMode = TextInputLayout.END_ICON_NONE // Remove the add button
        }

        binding.sectionAutoComplete.apply {
            val sections = ('A'..'Z').map { it.toString() }
            val adapter = ArrayAdapter(
                this@ViewProfileActivity,
                android.R.layout.simple_dropdown_item_1line,
                sections
            )
            setAdapter(adapter)

            setOnItemClickListener { _, _, _, _ ->
                val selectedSection = text.toString()
                viewModel.setSelectedSection(selectedSection)
            }

            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    showDropDown()
                }
            }

            setOnClickListener {
                showDropDown()
            }
        }
    }

    private fun setupTextChangeListeners() {
        binding.nameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setUserName(s?.toString() ?: "")
            }
        })

        binding.phoneEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                viewModel.setPhoneNumber(s?.toString() ?: "")
            }
        })
    }

    private fun setupUpdateButton() {
        binding.updateProfileButton.setOnClickListener {
            showUpdateProfileConfirmation()
        }
    }

    private fun showUpdateProfileConfirmation() {
        DialogUtils.showConfirmationDialog(
            context = this,
            title = "Update Profile",
            message = "Are you sure you want to update your profile?",
            onPositiveClick = {
                // Proceed with profile update
                val name = binding.nameEditText.text.toString()
                val phone = binding.phoneEditText.text.toString()
                viewModel.updateProfile(name, phone)
            },
            onNegativeClick = {
                // Optional: Handle negative click if needed
            }
        )
    }

    private fun observeViewModel() {
        viewModel.colleges.observe(this) { colleges ->
            setupSearchableDropdown(binding.collegeAutoComplete, colleges)
        }

        viewModel.combination.observe(this) { combinations ->
            setupSearchableDropdown(binding.combinationAutoComplete, combinations)
        }

        viewModel.semesters.observe(this) { semesters ->
            setupSearchableDropdown(binding.semesterAutoComplete, semesters)
        }

        viewModel.sections.observe(this) { sections ->
            setupSearchableDropdown(binding.sectionAutoComplete, sections)
        }

        viewModel.showAddCollegeButton.observe(this) { show ->
            binding.collegeInputLayout.setEndIconVisible(show)
        }

        viewModel.showAddCombinationButton.observe(this) { show ->
            binding.combinationInputLayout.setEndIconVisible(show)
        }

        viewModel.showAddSemesterButton.observe(this) { show ->
            binding.semesterInputLayout.setEndIconVisible(show)
        }

        viewModel.showAddSectionButton.observe(this) { show ->
            binding.sectionInputLayout.setEndIconVisible(show)
        }

        viewModel.isUpdating.observe(this) { isUpdating ->
            binding.updateProfileButton.isEnabled = !isUpdating
            binding.progressBar.isVisible = isUpdating
            binding.updateProfileButton.text = if (isUpdating) "" else "Update Profile"
            
            if (!isUpdating) {
                finish()
            }
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isProfileValid.observe(this) { isValid ->
            binding.updateProfileButton.isEnabled = isValid
            binding.updateProfileButton.alpha = if (isValid) 1.0f else 0.5f
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.contentLayout.isVisible = !isLoading
        }

        viewModel.userProfile.observe(this) { profile ->
            profile?.let { populateFields(it) }
        }
    }

    private fun setupSearchableDropdown(
        autoCompleteTextView: AutoCompleteTextView,
        items: List<String>
    ) {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            items
        )
        autoCompleteTextView.setAdapter(adapter)
    }

    private fun showAddCollegeDialog(collegeName: String) {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        dialogBinding.titleText.text = "Add New College"
        dialogBinding.itemInput.setText(collegeName)

        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { dialog, _ ->
                val finalCollegeName = dialogBinding.itemInput.text.toString().trim()
                if (finalCollegeName.isNotEmpty()) {
                    viewModel.addNewCollege(finalCollegeName)
                    // Set the newly added college as selected
                    binding.collegeAutoComplete.setText(finalCollegeName, false)
                    binding.collegeInputLayout.setEndIconVisible(false)
                    clearDependentFields()
                    viewModel.setSelectedCollege(finalCollegeName)
                    viewModel.fetchCombination(finalCollegeName)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddCombinationDialog(combinationName: String) {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        dialogBinding.titleText.text = "Add New Combination"
        dialogBinding.itemInput.setText(combinationName)

        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { dialog, _ ->
                val finalCombinationName = dialogBinding.itemInput.text.toString().trim()
                if (finalCombinationName.isNotEmpty()) {
                    viewModel.addNewCombination(finalCombinationName)
                    // Set the newly added combination as selected
                    binding.combinationAutoComplete.setText(finalCombinationName, false)
                    binding.combinationInputLayout.setEndIconVisible(false)
                    clearSemesterAndSection()
                    viewModel.setSelectedCombination(finalCombinationName)
                    viewModel.fetchSemesters(
                        binding.collegeAutoComplete.text.toString(),
                        finalCombinationName
                    )
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearDependentFields() {
        binding.combinationAutoComplete.apply {
            text.clear()
            dismissDropDown()
        }
        clearSemesterAndSection()
        // Reset validation
        viewModel.setSelectedCombination("")
        viewModel.setSelectedSemester("")
        viewModel.setSelectedSection("")
    }

    private fun clearSemesterAndSection() {
        binding.semesterAutoComplete.apply {
            text.clear()
            dismissDropDown()
        }
        binding.sectionAutoComplete.apply {
            text.clear()
            dismissDropDown()
        }
        // Reset validation
        viewModel.setSelectedSemester("")
        viewModel.setSelectedSection("")
    }

    private fun clearSection() {
        binding.sectionAutoComplete.apply {
            text.clear()
            dismissDropDown()
        }
        // Reset validation
        viewModel.setSelectedSection("")
    }

    private fun populateFields(profile: UserProfile) {
        binding.apply {
            nameEditText.setText(profile.name)
            phoneEditText.setText(profile.phone)
            collegeAutoComplete.setText(profile.college)
            combinationAutoComplete.setText(profile.combination)
            semesterAutoComplete.setText(profile.semester)
            sectionAutoComplete.setText(profile.section)

            // Fetch dependent data if college exists
            profile.college?.let {
                viewModel.setSelectedCollege(it)
                viewModel.fetchCombination(it)
            }

            // Fetch sections if combination exists
            if (!profile.college.isNullOrEmpty() && !profile.combination.isNullOrEmpty()) {
                viewModel.setSelectedCombination(profile.combination)
                viewModel.fetchSections(profile.college, profile.combination, profile.semester ?: "")
            }

            loadProfileImage(profile.profileImageUrl)
        }
    }
}