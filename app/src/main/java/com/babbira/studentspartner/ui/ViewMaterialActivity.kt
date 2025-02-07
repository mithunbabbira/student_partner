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

class ViewMaterialActivity : AppCompatActivity() {
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
        // Show loading progress
        showLoading(true)

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
            }
            .addOnFailureListener { e ->
                showLoading(false)
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.isVisible = show
    }

    private fun setupViewPager() {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayout

        val adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        adapter.addFragment(ViewAllFragment.newInstance(materialsList), "View All")
        adapter.addFragment(ChapterWiseFragment.newInstance(materialsList), "Chapter Wise")

        viewPager.adapter = adapter
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
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

    companion object {
        const val EXTRA_SUBJECT_NAME = "subject_name"
    }
} 