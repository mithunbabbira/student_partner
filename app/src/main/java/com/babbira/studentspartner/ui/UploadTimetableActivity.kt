package com.babbira.studentspartner.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.babbira.studentspartner.data.model.Timetable
import com.babbira.studentspartner.databinding.ActivityUploadTimetableBinding
import com.babbira.studentspartner.utils.UserDetails
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

class UploadTimetableActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUploadTimetableBinding
    private val storage = FirebaseStorage.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var selectedPdfUri: Uri? = null

    companion object {
        const val EXTRA_TIMETABLE_TYPE = "timetable_type"
    }

    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedPdfUri = it
            binding.selectPdfButton.text = "PDF Selected"
            binding.uploadButton.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setupViews()
    }

    private fun setupViews() {
        binding.selectPdfButton.setOnClickListener {
            getContent.launch("application/pdf")
        }

        binding.uploadButton.setOnClickListener {
            uploadPdf()
        }
    }

    private fun uploadPdf() {
        val uri = selectedPdfUri ?: return
        binding.uploadButton.isEnabled = false
        binding.progressBar.isVisible = true

        val timetableType = intent.getStringExtra(EXTRA_TIMETABLE_TYPE) ?: return
        val filename = "timetable_${System.currentTimeMillis()}.pdf"
        
        val college = UserDetails.getUserCollege(this)
        val combination = UserDetails.getUserCombination(this)
        val section = UserDetails.getUserSection(this)
        
        val pdfRef = storage.reference
            .child("pdfData")
            .child(college)
            .child(combination)
            .child(section)
            .child(if (timetableType == TimetableActivity.TYPE_CLASS) "classtimetable" else "examtimetable")
            .child(filename)

        pdfRef.putFile(uri)
            .addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                binding.progressBar.progress = progress
            }
            .continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                pdfRef.downloadUrl
            }
            .addOnSuccessListener { downloadUrl ->
                saveTimetableToFirestore(downloadUrl.toString(), timetableType, filename)
            }
            .addOnFailureListener { e ->
                showError("Upload failed: ${e.message}")
                binding.uploadButton.isEnabled = true
                binding.progressBar.isVisible = false
            }
    }

    private fun saveTimetableToFirestore(pdfUrl: String, type: String, filename: String) {
        val college = UserDetails.getUserCollege(this)
        val combination = UserDetails.getUserCombination(this)
        val semester = UserDetails.getUserSemester(this)

        val timetable = Timetable(
            id = UUID.randomUUID().toString(),
            title = if (type == TimetableActivity.TYPE_CLASS) "Class Timetable" else "Exam Timetable",
            filename = filename,
            pdfUrl = pdfUrl,
            addedBy = UserDetails.getUserName(this),
            userId = UserDetails.getUserId(this),
            createdAt = System.currentTimeMillis()
        )

        db.collection("collegeList")
            .document(college)
            .collection("combination")
            .document(combination)
            .collection("semesters")
            .document(semester)
            .collection("timetable")
            .document(if (type == TimetableActivity.TYPE_CLASS) "classtimetable" else "examtimetable")
            .set(timetable)
            .addOnSuccessListener {
                setResult(Activity.RESULT_OK)
                finish()
            }
            .addOnFailureListener { e ->
                showError("Failed to save: ${e.message}")
                binding.uploadButton.isEnabled = true
                binding.progressBar.isVisible = false
            }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
} 