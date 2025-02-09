package com.babbira.studentspartner.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.babbira.studentspartner.R
import com.babbira.studentspartner.databinding.ActivityTimetableBinding
import com.babbira.studentspartner.data.model.Timetable
import com.babbira.studentspartner.utils.UserDetails
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.File
import java.io.IOException
import android.os.Environment
import android.view.ScaleGestureDetector
import android.view.MotionEvent

class TimetableActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimetableBinding
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var totalPages = 0
    private var currentPageNumber = 0
    private val db = FirebaseFirestore.getInstance()
    private val startForResult = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Refresh timetable
            intent.getStringExtra(EXTRA_TIMETABLE_TYPE)?.let { fetchTimetable(it) }
        }
    }

    companion object {
        const val EXTRA_TIMETABLE_TYPE = "timetable_type"
        const val TYPE_CLASS = "class"
        const val TYPE_EXAM = "exam"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimetableBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupToolbar()
        fetchTimetable(intent.getStringExtra(EXTRA_TIMETABLE_TYPE))
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = when(intent.getStringExtra(EXTRA_TIMETABLE_TYPE)) {
                TYPE_CLASS -> getString(R.string.class_timetable)
                TYPE_EXAM -> getString(R.string.exam_timetable)
                else -> getString(R.string.timetable)
            }
        }
    }

    private fun fetchTimetable(type: String?) {
        if (type == null) {
            showError("Invalid timetable type")
            return
        }

        binding.progressBar.visibility = View.VISIBLE
        binding.titleText.text = if (type == TYPE_CLASS) "Class Timetable" else "Exam Timetable"

        val college = UserDetails.getUserCollege(this)
        val combination = UserDetails.getUserCombination(this)
        val semester = UserDetails.getUserSemester(this)

        db.collection("collegeList")
            .document(college)
            .collection("combination")
            .document(combination)
            .collection("semesters")
            .document(semester)
            .collection("timetable")
            .document(if (type == TYPE_CLASS) "classtimetable" else "examtimetable")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val timetable = document.toObject(Timetable::class.java)
                    timetable?.let {
                        binding.uploadedByText.text = "Uploaded by: ${it.addedBy}"
                        downloadAndViewPdf(it)
                    }
                } else {
                    showError("No timetable found")
                }
            }
            .addOnFailureListener { e ->
                showError("Failed to fetch timetable: ${e.message}")
            }
    }

    private fun downloadAndViewPdf(timetable: Timetable) {
        val fileName = "${timetable.filename}.pdf"
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        if (file.exists() && file.length() > 0) {
            binding.progressBar.visibility = View.GONE  // Ensure loader is hidden
            openPdfRenderer(file)
            showPage(0)
        } else {
            downloadPdf(timetable.pdfUrl, fileName)
        }
    }

    private fun downloadPdf(pdfUrl: String, fileName: String) {
        binding.progressBar.visibility = View.VISIBLE
        
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        val storageRef = Firebase.storage.getReferenceFromUrl(pdfUrl)
        storageRef.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->
                try {
                    file.writeBytes(bytes)
                    openPdfRenderer(file)
                    showPage(0)
                } catch (e: Exception) {
                    showError("Failed to save PDF: ${e.message}")
                }
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                showError("Failed to download PDF: ${it.message}")
                binding.progressBar.visibility = View.GONE
            }
    }

    private fun openPdfRenderer(file: File) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            totalPages = pdfRenderer?.pageCount ?: 0
        } catch (e: IOException) {
            binding.progressBar.visibility = View.GONE  // Hide loader on error
            e.printStackTrace()
        }
    }

    private fun showPage(pageNumber: Int) {
        currentPage?.close()
        pdfRenderer?.let { renderer ->
            currentPage = renderer.openPage(pageNumber)
            val bitmap = Bitmap.createBitmap(
                currentPage!!.width * 2,
                currentPage!!.height * 2,
                Bitmap.Config.ARGB_8888
            )
            currentPage?.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            binding.pdfImageView.setImageBitmap(bitmap)
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        currentPage?.close()
        pdfRenderer?.close()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_timetable, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_upload -> {
                startForResult.launch(
                    Intent(this, UploadTimetableActivity::class.java).apply {
                        putExtra(EXTRA_TIMETABLE_TYPE, intent.getStringExtra(EXTRA_TIMETABLE_TYPE))
                    }
                )
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
} 