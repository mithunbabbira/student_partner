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

class TimetableActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTimetableBinding
    private var pdfRenderer: PdfRenderer? = null
    private var currentPage: PdfRenderer.Page? = null
    private var isFullScreen = false
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
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val timetableType = intent.getStringExtra(EXTRA_TIMETABLE_TYPE)
        setupToolbarTitle(timetableType)
        
        fetchTimetable(timetableType)
    }

    private fun setupToolbarTitle(type: String?) {
        binding.toolbar.title = when (type) {
            TYPE_CLASS -> "Class Timetable"
            TYPE_EXAM -> "Exam Timetable"
            else -> "Timetable"
        }
    }

    private fun fetchTimetable(type: String?) {
        if (type == null) {
            showError("Invalid timetable type")
            return
        }

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
            openPdfRenderer(file)
            showPage(0)
            setupPdfControls()
        } else {
            downloadPdf(timetable.pdfUrl, fileName)
        }
    }

    private fun downloadPdf(pdfUrl: String, fileName: String) {
        val file = File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
        
        val storageRef = Firebase.storage.getReferenceFromUrl(pdfUrl)
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            try {
                file.writeBytes(bytes)
                openPdfRenderer(file)
                showPage(0)
                setupPdfControls()
            } catch (e: Exception) {
                showError("Failed to save PDF: ${e.message}")
            }
        }.addOnFailureListener {
            showError("Failed to download PDF: ${it.message}")
        }
    }

    private fun setupPdfControls() {
        binding.previousButton.setOnClickListener {
            if (currentPageNumber > 0) {
                showPage(currentPageNumber - 1)
            }
        }
        
        binding.nextButton.setOnClickListener {
            if (currentPageNumber < totalPages - 1) {
                showPage(currentPageNumber + 1)
            }
        }

        binding.pdfImageView.setOnClickListener {
            toggleFullScreen()
        }
    }

    private fun openPdfRenderer(file: File) {
        try {
            val fileDescriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            pdfRenderer = PdfRenderer(fileDescriptor)
            totalPages = pdfRenderer?.pageCount ?: 0
            binding.pageCountText.text = "Page 1 of $totalPages"
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun showPage(pageNumber: Int) {
        currentPage?.close()
        
        pdfRenderer?.let { renderer ->
            currentPage = renderer.openPage(pageNumber)
            currentPageNumber = pageNumber
            
            val bitmap = Bitmap.createBitmap(
                currentPage!!.width * 2,
                currentPage!!.height * 2,
                Bitmap.Config.ARGB_8888
            )
            
            currentPage?.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            binding.pdfImageView.setImageBitmap(bitmap)
            binding.pageCountText.text = "Page ${pageNumber + 1} of $totalPages"
        }
    }

    private fun toggleFullScreen() {
        isFullScreen = !isFullScreen
        if (isFullScreen) {
            val params = binding.pdfImageView.layoutParams
            params.height = ViewGroup.LayoutParams.MATCH_PARENT
            binding.pdfImageView.layoutParams = params
            supportActionBar?.hide()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        } else {
            val params = binding.pdfImageView.layoutParams
            params.height = (resources.displayMetrics.heightPixels * 0.5).toInt()
            binding.pdfImageView.layoutParams = params
            supportActionBar?.show()
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        currentPage?.close()
        pdfRenderer?.close()
    }
} 