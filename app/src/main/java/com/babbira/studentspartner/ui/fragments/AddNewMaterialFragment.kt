package com.babbira.studentspartner.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.babbira.studentspartner.R
import com.babbira.studentspartner.data.model.SubjectMaterial
import com.babbira.studentspartner.databinding.FragmentAddNewMaterialBinding
import com.babbira.studentspartner.ui.ViewMaterialActivity.Companion.EXTRA_SUBJECT_NAME
import com.babbira.studentspartner.utils.UserDetails
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class AddNewMaterialFragment : Fragment() {
    companion object {
        private const val ARG_SUBJECT_NAME = "subject_name"

        fun newInstance(subjectName: String): AddNewMaterialFragment {
            return AddNewMaterialFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SUBJECT_NAME, subjectName)
                }
            }
        }
    }

    private var _binding: FragmentAddNewMaterialBinding? = null
    private val binding get() = _binding!!
    
    private val storage = FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private var selectedPdfUri: Uri? = null
    private val chapters = (1..15).map { it.toString() }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedPdfUri = it
            binding.selectPdfButton.text = "PDF Selected"
            binding.selectPdfButton.setIconResource(R.drawable.ic_check)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            openPdfPicker()
        } else {
            Toast.makeText(
                context,
                "Storage permission is required to select PDF files",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddNewMaterialBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupChapterDropdown()
        setupButtons()
    }

    private fun setupChapterDropdown() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            chapters
        )
        binding.chapterDropdown.setAdapter(adapter)
    }

    private fun setupButtons() {
        binding.selectPdfButton.setOnClickListener {
            checkAndRequestPermission()
        }

        binding.uploadButton.setOnClickListener {
            if (validateInputs()) {
                uploadMaterial()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val title = binding.titleEditText.text.toString().trim()
        val description = binding.descriptionEditText.text.toString().trim()
        val chapter = binding.chapterDropdown.text.toString()

        when {
            title.isEmpty() -> {
                binding.titleEditText.error = "Title is required"
                return false
            }
            description.isEmpty() -> {
                binding.descriptionEditText.error = "Description is required"
                return false
            }
            chapter.isEmpty() -> {
                binding.chapterInputLayout.error = "Chapter is required"
                return false
            }
            selectedPdfUri == null -> {
                Toast.makeText(context, "Please select a PDF file", Toast.LENGTH_SHORT).show()
                return false
            }
        }
        return true
    }

    private fun uploadMaterial() {
        val college = UserDetails.getUserCollege(requireContext())
        val combination = UserDetails.getUserCombination(requireContext())
        val semester = UserDetails.getUserSemester(requireContext())
        val section = UserDetails.getUserSection(requireContext())

        if (college.isEmpty() || combination.isEmpty() || semester.isEmpty()) {
            Toast.makeText(context, "Missing user details", Toast.LENGTH_SHORT).show()
            return
        }

        binding.uploadProgress.isVisible = true
        binding.uploadButton.isEnabled = false

        // First upload PDF to Storage
        val pdfRef = storage.reference
            .child("pdfData")
            .child(college)
            .child(combination)
            .child(section)
            .child("${System.currentTimeMillis()}.pdf")

        selectedPdfUri?.let { uri ->
            pdfRef.putFile(uri)
                .addOnProgressListener { taskSnapshot ->
                    val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                    binding.uploadProgress.progress = progress
                }
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let { throw it }
                    }
                    pdfRef.downloadUrl
                }
                .addOnSuccessListener { downloadUrl ->
                    // After PDF upload, save material details to Firestore
                    saveMaterialToFirestore(downloadUrl.toString())
                }
                .addOnFailureListener { e ->
                    binding.uploadProgress.isVisible = false
                    binding.uploadButton.isEnabled = true
                    Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveMaterialToFirestore(pdfUrl: String) {
        val material = SubjectMaterial(
            title = binding.titleEditText.text.toString().trim(),
            description = binding.descriptionEditText.text.toString().trim(),
            pdfUrl = pdfUrl,
            addedBy = auth.currentUser?.uid ?: "",
            section = UserDetails.getUserSection(requireContext()),
            semester = UserDetails.getUserSemester(requireContext()),
            chapterNumber = binding.chapterDropdown.text.toString().toInt(),
            createdAt = System.currentTimeMillis()
        )

        val college = UserDetails.getUserCollege(requireContext())
        val combination = UserDetails.getUserCombination(requireContext())
        val semester = UserDetails.getUserSemester(requireContext())
        val subjectName = arguments?.getString(ARG_SUBJECT_NAME)

        // Add logging
        Log.d("AddNewMaterial", "Saving material with details:")
        Log.d("AddNewMaterial", "College: $college")
        Log.d("AddNewMaterial", "Combination: $combination")
        Log.d("AddNewMaterial", "Semester: $semester")
        Log.d("AddNewMaterial", "Subject Name: $subjectName")
        Log.d("AddNewMaterial", "Material: $material")

        if (subjectName == null) {
            Log.e("AddNewMaterial", "Subject name is null, cannot save material")
            Toast.makeText(context, "Error: Subject name is missing", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("collegeList")
            .document(college)
            .collection("combination")
            .document(combination)
            .collection("semesters")
            .document(semester)
            .collection("subjectList")
            .document(subjectName)
            .collection("materials")
            .add(material)
            .addOnSuccessListener {
                Log.d("AddNewMaterial", "Material saved successfully with ID: ${it.id}")
                binding.uploadProgress.isVisible = false
                binding.uploadButton.isEnabled = true
                Toast.makeText(context, "Material uploaded successfully", Toast.LENGTH_SHORT).show()
                clearForm()
            }
            .addOnFailureListener { e ->
                Log.e("AddNewMaterial", "Failed to save material", e)
                binding.uploadProgress.isVisible = false
                binding.uploadButton.isEnabled = true
                Toast.makeText(context, "Failed to save material: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun clearForm() {
        binding.titleEditText.text?.clear()
        binding.descriptionEditText.text?.clear()
        binding.chapterDropdown.text?.clear()
        selectedPdfUri = null
        binding.selectPdfButton.text = "Select PDF"
        binding.selectPdfButton.setIconResource(R.drawable.ic_pdf)
    }

    private fun checkAndRequestPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            ) == PackageManager.PERMISSION_GRANTED -> {
                openPdfPicker()
            }
            shouldShowRequestPermissionRationale(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Manifest.permission.READ_MEDIA_IMAGES
                } else {
                    Manifest.permission.READ_EXTERNAL_STORAGE
                }
            ) -> {
                showPermissionRationaleDialog()
            }
            else -> {
                requestPermissionLauncher.launch(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                )
            }
        }
    }

    private fun showPermissionRationaleDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Permission Required")
            .setMessage("Storage permission is required to select PDF files")
            .setPositiveButton("Grant") { _, _ ->
                requestPermissionLauncher.launch(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        Manifest.permission.READ_MEDIA_IMAGES
                    } else {
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun openPdfPicker() {
        getContent.launch("application/pdf")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 