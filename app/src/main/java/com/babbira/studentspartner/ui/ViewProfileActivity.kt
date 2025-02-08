package com.babbira.studentspartner.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.babbira.studentspartner.data.model.UserProfile
import com.babbira.studentspartner.databinding.ActivityViewProfileBinding
import com.babbira.studentspartner.databinding.DialogAddItemBinding
import com.babbira.studentspartner.utils.DialogUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout

class ViewProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewProfileBinding
    private val viewModel: ViewProfileViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDropdownListeners()
        setupTextChangeListeners()
        setupUpdateButton()
        observeViewModel()
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

        viewModel.combination.observe(this) { combination ->
            setupSearchableDropdown(binding.combinationAutoComplete, combination)
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
        }
    }
}