package com.babbira.studentspartner.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.babbira.studentspartner.R
import com.babbira.studentspartner.utils.UserDetails

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.babbira.studentspartner.databinding.ActivityMainBinding
import com.babbira.studentspartner.adapter.SubjectListAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.widget.Toolbar
import android.view.MenuItem
import android.view.Gravity
import android.widget.TextView
import androidx.core.view.GravityCompat


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var subjectListAdapter: SubjectListAdapter
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = Firebase.firestore

        // Initialize views
        drawerLayout = binding.drawerLayout
        navigationView = binding.navigationView
        toolbar = binding.toolbar

        // Setup toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.ic_menu) // Hamburger icon
        }

        // Setup navigation drawer
        setupNavigationDrawer()

        // Update navigation header with user info
        updateNavigationHeader()

        setupRecyclerView()
        setupAddSubjectButton()
        checkUserProfile()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        subjectListAdapter = SubjectListAdapter { subject ->
            // Launch ViewMaterialActivity when subject is clicked
            val intent = Intent(this, ViewMaterialActivity::class.java).apply {
                putExtra(ViewMaterialActivity.EXTRA_SUBJECT_NAME, subject)
            }
            startActivity(intent)
        }

        binding.subjectsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = subjectListAdapter
        }
    }

    private fun setupAddSubjectButton() {
        binding.addNewSubjectButton.setOnClickListener {
            showAddSubjectDialog()
        }
    }

    private fun showAddSubjectDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_subject, null)
        val subjectNameEditText = dialogView.findViewById<TextInputEditText>(R.id.subjectNameEditText)

        MaterialAlertDialogBuilder(this)
            .setTitle("Add New Subject")
            .setView(dialogView)
            .setPositiveButton("Add Subject") { dialog, _ ->
                val subjectName = subjectNameEditText.text?.toString()?.trim() ?: ""
                if (subjectName.isNotEmpty()) {
                    addNewSubject(subjectName)
                } else {
                    Toast.makeText(this, "Please enter subject name", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun addNewSubject(subjectName: String) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.e("MainActivity", "User not logged in")
            return
        }

        val college = UserDetails.getUserCollege(this)
        val combination = UserDetails.getUserCombination(this)
        val semester = UserDetails.getUserSemester(this)

        if (college.isEmpty() || combination.isEmpty() || semester.isEmpty()) {
            Log.e("MainActivity", "Missing user details")
            return
        }

        val subjectRef = db.collection("collegeList")
            .document(college)
            .collection("combination")
            .document(combination)
            .collection("semesters")
            .document(semester)
            .collection("subjectList")
            .document(subjectName)

        // Create initial subject document
        val subjectData = hashMapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "addedBy" to currentUser.uid
        )

        subjectRef
            .set(subjectData)
            .addOnSuccessListener {
                Log.d("MainActivity", "Subject added successfully")
                Toast.makeText(this, "Subject added successfully", Toast.LENGTH_SHORT).show()
                fetchSubjectList()
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error adding subject", e)
                Toast.makeText(this, "Error adding subject", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkUserProfile() {
        println("MainActivity: Checking user profile")
        val currentUser = auth.currentUser
        if (currentUser == null) {
            println("MainActivity: No user found, redirecting to login")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        println("MainActivity: Checking Firestore for user: ${currentUser.uid}")
        db.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    println("MainActivity: User profile found")
                    saveUserDetailsToPreferences(document)
                    fetchSubjectList()
                    Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                    // User exists, stay on MainActivity
                } else {
                    println("MainActivity: No user profile found, redirecting to profile creation")
                    // User doesn't exist, redirect to profile creation
                    startActivity(Intent(this, ViewProfileActivity::class.java))
                    
                }
            }
            .addOnFailureListener { e ->
                println("MainActivity: Error checking user profile: ${e.message}")
                Toast.makeText(this, "Error checking profile: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun fetchSubjectList() {
        val college = UserDetails.getUserCollege(this)
        val combination = UserDetails.getUserCombination(this)
        val semester = UserDetails.getUserSemester(this)

        if (college.isEmpty() || combination.isEmpty() || semester.isEmpty()) {
            Log.e("MainActivity", "Missing user details")
            return
        }

        db.collection("collegeList")
            .document(college)
            .collection("combination")
            .document(combination)
            .collection("semesters")
            .document(semester)
            .collection("subjectList")
            .get()
            .addOnSuccessListener { documents ->
                val subjects = documents.documents.map { it.id }
                subjectListAdapter.updateSubjects(subjects)
                Log.d("MainActivity", "Fetched ${subjects.size} subjects")
            }
            .addOnFailureListener { e ->
                Log.e("MainActivity", "Error fetching subject list", e)
                Toast.makeText(this, "Error loading subjects", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveUserDetailsToPreferences(document: DocumentSnapshot) {
        try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.e("MainActivity", "User not logged in")
                return
            }

            // Save user details to SharedPreferences
            UserDetails.apply {
                setUserId(this@MainActivity, currentUser.uid)  // Save Firebase UID
                setUserName(this@MainActivity, document.getString("name") ?: "")
                setUserEmail(this@MainActivity, document.getString("email") ?: "")
                setUserPhone(this@MainActivity, document.getString("phone") ?: "")
                setUserCollege(this@MainActivity, document.getString("college") ?: "")
                setUserCombination(this@MainActivity, document.getString("combination") ?: "")
                setUserSemester(this@MainActivity, document.getString("semester") ?: "")
                setUserSection(this@MainActivity, document.getString("section") ?: "")
                setLoggedIn(this@MainActivity, true)
            }
            
            Log.d("MainActivity", "User details saved to SharedPreferences")
        } catch (e: Exception) {
            Log.e("MainActivity", "Error saving user details to SharedPreferences", e)
            Toast.makeText(
                this,
                "Error saving user details",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_profile -> {
                    startActivity(Intent(this, ViewProfileActivity::class.java))
                }
                R.id.nav_classmates -> {
                    // Start classmates activity
                    // startActivity(Intent(this, ClassmatesActivity::class.java))
                }
                R.id.nav_about -> {
                    // Show about dialog or start about activity
                    showAboutDialog()
                }
                R.id.nav_logout -> {
                    showLogoutConfirmationDialog()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun updateNavigationHeader() {
        val headerView = navigationView.getHeaderView(0)
        headerView.findViewById<TextView>(R.id.navHeaderName).text = UserDetails.getUserName(this)
        headerView.findViewById<TextView>(R.id.navHeaderEmail).text = UserDetails.getUserEmail(this)
    }

    private fun showAboutDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("About Us")
            .setMessage("Students Partner is an app designed to help students share and access study materials easily.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showLogoutConfirmationDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                // Perform logout
                FirebaseAuth.getInstance().signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun setupClickListeners() {
        binding.apply {
            viewClassTimetableButton.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, TimetableActivity::class.java).apply {
                        putExtra(TimetableActivity.EXTRA_TIMETABLE_TYPE, TimetableActivity.TYPE_CLASS)
                    }
                )
            }

            viewExamTimetableButton.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, TimetableActivity::class.java).apply {
                        putExtra(TimetableActivity.EXTRA_TIMETABLE_TYPE, TimetableActivity.TYPE_EXAM)
                    }
                )
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
} 