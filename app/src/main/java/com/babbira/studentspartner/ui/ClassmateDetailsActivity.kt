package com.babbira.studentspartner.ui



import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.babbira.studentspartner.R
import com.babbira.studentspartner.adapters.ClassmatesAdapter
import com.babbira.studentspartner.databinding.ActivityClassmateDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClassmateDetailsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityClassmateDetailsBinding
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityClassmateDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        fetchClassmates()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = getString(R.string.my_classmates)
        }
    }

    private fun fetchClassmates() {
        binding.progressBar.visibility = View.VISIBLE

        // First get current user's details
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val myCollege = document.getString("college") ?: ""
                        val myCombination = document.getString("combination") ?: ""
                        val mySection = document.getString("section") ?: ""
                        val mySemester = document.getString("semester") ?: ""

                        // Now fetch all classmates with matching criteria
                        db.collection("users")
                            .whereEqualTo("college", myCollege)
                            .whereEqualTo("combination", myCombination)
                            .whereEqualTo("section", mySection)
                            .whereEqualTo("semester", mySemester)
                            .get()
                            .addOnSuccessListener { documents ->
                                binding.progressBar.visibility = View.GONE
                                val classmatesList = documents.mapNotNull { doc ->
                                    // Skip current user from the list
                                    if (doc.id != user.uid) {
                                        ClassmateModel(
                                            id = doc.id,
                                            name = doc.getString("name") ?: "",
                                            phone = doc.getString("phone") ?: "",
                                            college = doc.getString("college") ?: "",
                                            combination = doc.getString("combination") ?: "",
                                            section = doc.getString("section") ?: "",
                                            semester = doc.getString("semester") ?: "",
                                            profileImageUrl = doc.getString("profileImageUrl")
                                        )
                                    } else null
                                }
                                setupRecyclerView(classmatesList)
                            }
                            .addOnFailureListener { e ->
                                binding.progressBar.visibility = View.GONE
                                Toast.makeText(this, "Error fetching classmates: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener { e ->
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun setupRecyclerView(classmates: List<ClassmateModel>) {
        binding.classmatesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ClassmateDetailsActivity)
            adapter = ClassmatesAdapter(
                classmates = classmates,
                onWhatsAppClick = { phone -> openWhatsApp(phone) },
                onCallClick = { phone -> dialPhoneNumber(phone) }
            )
        }
    }

    private fun openWhatsApp(phone: String) {
        try {
            val phoneNumber = if (phone.startsWith("+")) phone else "+91$phone"
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$phoneNumber")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun dialPhoneNumber(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$phone")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to make call", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

data class ClassmateModel(
    val id: String,
    val name: String,
    val phone: String,
    val college: String,
    val combination: String,
    val section: String,
    val semester: String,
    val profileImageUrl: String? = null
) 