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
import com.babbira.studentspartner.utils.CommonFunctions
import com.babbira.studentspartner.utils.DialogUtils
import com.babbira.studentspartner.utils.ImageUtils
import com.babbira.studentspartner.utils.LoaderManager
import com.babbira.studentspartner.utils.UserDetails
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
    private val loaderManager = LoaderManager.getInstance()
    
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.profileImageView.setImageURI(it)
            loaderManager.showLoader(this)
            uploadProfileImage(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        
        // Initialize adapters first
        setupCollegeAdapter()
        setupCombinationAdapter()
        
        setupImagePicker()
        setupDropdownListeners()
        setupTextChangeListeners()
        setupUpdateButton()
        observeViewModel()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.edit_profile)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
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
            .setTitle(getString(R.string.dialog_title_profile_picture))
            .setItems(arrayOf(
                getString(R.string.dialog_option_choose_from_gallery),
                getString(R.string.dialog_option_remove_photo)
            )) { _, which ->
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
            loaderManager.hideLoader()
            Toast.makeText(this, getString(R.string.error_select_college_and_combination), Toast.LENGTH_SHORT).show()
            return
        }

        val compressedImage = ImageUtils.compressImage(imageUri, contentResolver)
        if (compressedImage == null) {
            loaderManager.hideLoader()
            Toast.makeText(this, getString(R.string.error_select_college_and_combination), Toast.LENGTH_SHORT).show()
            return
        }

        val imageRef = storageRef.child("profile_pics/$userCollege/$userCombination/$userId.jpg")

        imageRef.putBytes(compressedImage)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    updateProfileImageUrl(downloadUri.toString())
                    UserDetails.setProfileImageUrl(this, downloadUri.toString())
                }
            }
            .addOnFailureListener {
                loaderManager.hideLoader()
                Toast.makeText(this, getString(R.string.error_upload_image_failed, getString(R.string.label_failed_to_process_image)), Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userRef = FirebaseFirestore.getInstance().collection("users").document(userId)

        userRef.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                loaderManager.hideLoader()
                Toast.makeText(this, getString(R.string.success_profile_picture_updated), Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                loaderManager.hideLoader()
                Toast.makeText(this, getString(R.string.error_update_profile_failed, getString(R.string.label_failed_to_update_profile)), Toast.LENGTH_SHORT).show()
            }
    }

    private fun removeProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val userCollege = binding.collegeAutoComplete.text.toString()
        val userCombination = binding.combinationAutoComplete.text.toString()

        if (userCollege.isEmpty() || userCombination.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_college_or_combination_not_found), Toast.LENGTH_SHORT).show()
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
                        Toast.makeText(this, getString(R.string.success_profile_picture_removed), Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, getString(R.string.error_remove_profile_picture_failed), Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun loadProfileImage(imageUrl: String?) {
        if (imageUrl != null) {
            UserDetails.setProfileImageUrl(this,imageUrl)
        }else{
            UserDetails.setProfileImageUrl(this,"")
        }
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
                binding.collegeInputLayout.isEndIconVisible = false
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    binding.collegeInputLayout.error = null
                }
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.isEmpty()) {
                        viewModel.fetchColleges()
                        binding.collegeInputLayout.isEndIconVisible = false
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
                binding.combinationInputLayout.isEndIconVisible = false
            }

            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    binding.combinationInputLayout.error = null
                }
                
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                
                override fun afterTextChanged(s: Editable?) {
                    val text = s?.toString() ?: ""
                    if (text.isEmpty()) {
                        viewModel.fetchCombination(binding.collegeAutoComplete.text.toString())
                        binding.combinationInputLayout.isEndIconVisible = false
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
                // Clear error when valid item selected
                binding.semesterInputLayout.error = null
            }

            // Add text change listener to clear error
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    binding.semesterInputLayout.error = null
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
                // Clear error when valid item selected
                binding.sectionInputLayout.error = null
            }

            // Add text change listener to clear error
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    binding.sectionInputLayout.error = null
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

        // Add text change listener for college field
        binding.collegeAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Clear error and helper text when user starts typing
                binding.collegeInputLayout.error = null
                binding.collegeInputLayout.helperText = null
                
                val text = s?.toString() ?: ""
                if (text.isNotEmpty()) {
                    // Check if college exists in the list
                    val collegeList = viewModel.colleges.value ?: emptyList()
                    val collegeExists = collegeList.any { it.equals(text, ignoreCase = true) }
                    
                    if (!collegeExists) {
                        // Show plus icon for adding new college
                        binding.collegeInputLayout.isEndIconVisible = true
                        binding.collegeInputLayout.setEndIconDrawable(R.drawable.ic_add)
                    } else {
                        binding.collegeInputLayout.isEndIconVisible = false
                    }
                }
            }
        })

        // Add similar listener for combination field
        binding.combinationAutoComplete.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Clear error and helper text when user starts typing
                binding.combinationInputLayout.error = null
                binding.combinationInputLayout.helperText = null
                
                val text = s?.toString() ?: ""
                if (text.isNotEmpty()) {
                    // Check if combination exists in the list
                    val combinationList = viewModel.combination.value ?: emptyList()
                    val combinationExists = combinationList.any { it.equals(text, ignoreCase = true) }
                    
                    if (!combinationExists) {
                        // Show plus icon for adding new combination
                        binding.combinationInputLayout.isEndIconVisible = true
                        binding.combinationInputLayout.setEndIconDrawable(R.drawable.ic_add)
                    } else {
                        binding.combinationInputLayout.isEndIconVisible = false
                    }
                }
            }
        })
    }

    private fun setupUpdateButton() {
        binding.updateProfileButton.setOnClickListener {
            showUpdateProfileConfirmation()
        }
    }

    private fun showUpdateProfileConfirmation() {
        // Get the entered values
        val enteredCollege = binding.collegeAutoComplete.text.toString().trim()
        val enteredCombination = binding.combinationAutoComplete.text.toString().trim()
        val enteredSemester = binding.semesterAutoComplete.text.toString().trim()
        val enteredSection = binding.sectionAutoComplete.text.toString().trim()
        
        // Get the lists from ViewModel
        val collegeList = viewModel.colleges.value ?: emptyList()
        val combinationList = viewModel.combination.value ?: emptyList()
        
        // Define valid semesters and sections
        val validSemesters = (1..10).map { "${it}st" }
        val validSections = ('A'..'Z').map { it.toString() }

        // Validate existence in respective lists
        val isCollegeValid = collegeList.any { it.equals(enteredCollege, ignoreCase = true) }
        val isCombinationValid = combinationList.any { it.equals(enteredCombination, ignoreCase = true) }
        val isSemesterValid = validSemesters.any { it.equals(enteredSemester, ignoreCase = true) }
        val isSectionValid = validSections.any { it.equals(enteredSection, ignoreCase = true) }

        when {
            !isCollegeValid -> {
                // Replace error with helper text and show plus icon
                binding.collegeInputLayout.error = null
                binding.collegeInputLayout.helperText = "Please select a valid college from the list or add the college"
                binding.collegeInputLayout.isEndIconVisible = true
                binding.collegeInputLayout.setEndIconDrawable(R.drawable.ic_add)
                return
            }
            !isCombinationValid -> {
                // Replace error with helper text and show plus icon
                binding.combinationInputLayout.error = null
                binding.combinationInputLayout.helperText = "Please select a valid combination from the list or add the combination"
                binding.combinationInputLayout.isEndIconVisible = true
                binding.combinationInputLayout.setEndIconDrawable(R.drawable.ic_add)
                return
            }
            !isSemesterValid -> {
                // Show error for semester - must be from list
                binding.semesterInputLayout.error = "Please select a valid semester from the dropdown list"
                return
            }
            !isSectionValid -> {
                // Show error for section - must be from list
                binding.sectionInputLayout.error = "Please select a valid section from the dropdown list"
                return
            }
            else -> {
                // Clear any previous errors or helper texts
                binding.collegeInputLayout.error = null
                binding.collegeInputLayout.helperText = null
                binding.combinationInputLayout.error = null
                binding.combinationInputLayout.helperText = null
                binding.semesterInputLayout.error = null
                binding.sectionInputLayout.error = null

                // Show confirmation dialog
                DialogUtils.showConfirmationDialog(
                    context = this,
                    title = getString(R.string.dialog_title_update_profile),
                    message = getString(R.string.dialog_message_update_profile),
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
        }
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
            binding.collegeInputLayout.isEndIconVisible = show
        }

        viewModel.showAddCombinationButton.observe(this) { show ->
            binding.combinationInputLayout.isEndIconVisible = show
        }

        viewModel.showAddSemesterButton.observe(this) { show ->
            binding.semesterInputLayout.isEndIconVisible = show
        }

        viewModel.showAddSectionButton.observe(this) { show ->
            binding.sectionInputLayout.isEndIconVisible = show
        }

        viewModel.isUpdating.observe(this) { isUpdating ->
            binding.updateProfileButton.isEnabled = !isUpdating
            if (isUpdating) {
                loaderManager.showLoader(this)
            } else {
                loaderManager.hideLoader()
                CommonFunctions.clearAllLocalData(this)
                finish()
            }
            binding.updateProfileButton.text = if (isUpdating) "" else getString(R.string.button_update_profile)
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
        dialogBinding.titleText.text = getString(R.string.dialog_title_add_college)
        dialogBinding.itemInput.setText(collegeName)

        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.dialog_button_add), { dialog, _ ->
                val finalCollegeName = dialogBinding.itemInput.text.toString().trim()
                if (finalCollegeName.isNotEmpty()) {
                    viewModel.addNewCollege(finalCollegeName)
                    // Set the newly added college as selected
                    binding.collegeAutoComplete.setText(finalCollegeName, false)
                    binding.collegeInputLayout.isEndIconVisible = false
                    clearDependentFields()
                    viewModel.setSelectedCollege(finalCollegeName)
                    viewModel.fetchCombination(finalCollegeName)
                }
                dialog.dismiss()
            })
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
            .show()
    }

    private fun showAddCombinationDialog(combinationName: String) {
        val dialogBinding = DialogAddItemBinding.inflate(layoutInflater)
        dialogBinding.titleText.text = getString(R.string.dialog_title_add_combination)
        dialogBinding.itemInput.setText(combinationName)

        MaterialAlertDialogBuilder(this)
            .setView(dialogBinding.root)
            .setPositiveButton(getString(R.string.dialog_button_add), { dialog, _ ->
                val finalCombinationName = dialogBinding.itemInput.text.toString().trim()
                if (finalCombinationName.isNotEmpty()) {
                    viewModel.addNewCombination(finalCombinationName)
                    // Set the newly added combination as selected
                    binding.combinationAutoComplete.setText(finalCombinationName, false)
                    binding.combinationInputLayout.isEndIconVisible = false
                    clearSemesterAndSection()
                    viewModel.setSelectedCombination(finalCombinationName)
                    viewModel.fetchSemesters(
                        binding.collegeAutoComplete.text.toString(),
                        finalCombinationName
                    )
                }
                dialog.dismiss()
            })
            .setNegativeButton(getString(R.string.dialog_button_cancel), null)
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
            // Set background colors
            root.setBackgroundColor(getColor(R.color.background))
            contentLayout.setBackgroundColor(getColor(R.color.background))
            
            // Set text colors and backgrounds for input fields
            nameEditText.apply {
                setTextColor(getColor(R.color.text_primary))
                setHintTextColor(getColor(R.color.text_secondary))
                background = getDrawable(R.drawable.edit_text_background)
            }
            
            phoneEditText.apply {
                setTextColor(getColor(R.color.text_primary))
                setHintTextColor(getColor(R.color.text_secondary))
                background = getDrawable(R.drawable.edit_text_background)
            }
            
            collegeAutoComplete.apply {
                setTextColor(getColor(R.color.text_primary))
                setHintTextColor(getColor(R.color.text_secondary))
                background = getDrawable(R.drawable.edit_text_background)
            }
            
            combinationAutoComplete.apply {
                setTextColor(getColor(R.color.text_primary))
                setHintTextColor(getColor(R.color.text_secondary))
                background = getDrawable(R.drawable.edit_text_background)
            }
            
            semesterAutoComplete.apply {
                setTextColor(getColor(R.color.text_primary))
                setHintTextColor(getColor(R.color.text_secondary))
                background = getDrawable(R.drawable.edit_text_background)
            }
            
            sectionAutoComplete.apply {
                setTextColor(getColor(R.color.text_primary))
                setHintTextColor(getColor(R.color.text_secondary))
                background = getDrawable(R.drawable.edit_text_background)
            }
            
            // Set button colors
            updateProfileButton.apply {
                setBackgroundColor(getColor(R.color.primary))
                setTextColor(getColor(R.color.white))
            }

            // Populate the fields with data
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