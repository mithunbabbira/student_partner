package com.babbira.studentspartner.ui

import com.babbira.studentspartner.adapters.ViewPagerAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.babbira.studentspartner.R
import com.babbira.studentspartner.data.model.SubjectMaterial
import com.babbira.studentspartner.databinding.ActivityViewMaterialBinding
import com.babbira.studentspartner.ui.fragments.AddNewMaterialFragment
import com.babbira.studentspartner.ui.fragments.ChapterWiseFragment
import com.babbira.studentspartner.ui.fragments.ViewAllFragment
import com.babbira.studentspartner.utils.UserDetails
import com.google.firebase.firestore.FirebaseFirestore
import com.google.android.material.tabs.TabLayoutMediator
import androidx.core.view.isVisible
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import android.view.View

import com.babbira.studentspartner.ui.fragments.AddNewMaterialListener

class ViewMaterialActivity : AppCompatActivity(), AddNewMaterialListener {
    private lateinit var binding: ActivityViewMaterialBinding
    private val materialsList = mutableListOf<SubjectMaterial>()
    private val db = FirebaseFirestore.getInstance()
    private var subjectName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subjectName = intent.getStringExtra("subject_name")
        if (subjectName != null) {
            fetchMaterials(subjectName!!)
        }
        
        setupViewPager()
        setupAddNewButton()
    }

    private fun setupAddNewButton() {
        binding.btnAddNew.setOnClickListener {
            subjectName?.let { name ->
                // Make fragment container visible before transaction
                binding.fragmentContainer.visibility = View.VISIBLE
                
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, AddNewMaterialFragment.newInstance(name))
                    .addToBackStack(null)
                    .commit()
                
                // Hide the ViewPager and button
                binding.viewPager.visibility = View.GONE
                binding.tabLayout.visibility = View.GONE
                binding.btnAddNew.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            // Hide fragment container and show other views
            binding.fragmentContainer.visibility = View.GONE
            binding.viewPager.visibility = View.VISIBLE
            binding.tabLayout.visibility = View.VISIBLE
            binding.btnAddNew.visibility = View.VISIBLE
        } else {
            super.onBackPressed()
        }
    }

    private fun fetchMaterials(subjectName: String) {
        showLoading(true)
        
        // Hide ViewPager while loading
        binding.viewPager.alpha = 0.5f
        binding.viewPager.isEnabled = false

        val college = UserDetails.getUserCollege(this)
        val combination = UserDetails.getUserCombination(this)
        val semester = UserDetails.getUserSemester(this)

        db.collection("collegeList")
            .document(college)
            .collection("combination")
            .document(combination)
            .collection("semesters")
            .document(semester)
            .collection("subjectList")
            .document(subjectName)
            .collection("materials")
            .get()
            .addOnSuccessListener { documents ->
                materialsList.clear()
                for (document in documents) {
                    val material = document.toObject(SubjectMaterial::class.java)
                    materialsList.add(material)
                }
                showLoading(false)
                updateFragments()
                
                // Show ViewPager after loading
                binding.viewPager.alpha = 1.0f
                binding.viewPager.isEnabled = true
            }
            .addOnFailureListener { e ->
                showLoading(false)
                // Show ViewPager after error
                binding.viewPager.alpha = 1.0f
                binding.viewPager.isEnabled = true
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    private fun setupViewPager() {
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: run {
            Toast.makeText(this, "Error: Subject name is missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        
        viewPagerAdapter.addFragment(
            ViewAllFragment.newInstance(materialsList, subjectName),  // Pass both parameters
            "View All"
        )
        viewPagerAdapter.addFragment(
            ChapterWiseFragment.newInstance(materialsList),
            "Chapter Wise"
        )

        binding.viewPager.adapter = viewPagerAdapter
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "View All"
                1 -> "Chapter Wise"
                else -> ""
            }
        }.attach()
    }

    private fun updateFragments() {
        // Get current fragments and update their materials
        val viewPagerAdapter = binding.viewPager.adapter as ViewPagerAdapter
        viewPagerAdapter.updateMaterials(materialsList)
    }

    override fun onMaterialUploaded() {
        // Refresh materials
        subjectName?.let { 
            fetchMaterials(it)
        }
        
        // Remove AddNewMaterialFragment and show ViewPager
        supportFragmentManager.popBackStack()
        binding.fragmentContainer.visibility = View.GONE
        binding.viewPager.visibility = View.VISIBLE
        binding.tabLayout.visibility = View.VISIBLE
        binding.btnAddNew.visibility = View.VISIBLE
    }

    companion object {
        const val EXTRA_SUBJECT_NAME = "subject_name"
    }
} 