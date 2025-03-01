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

        setupToolbar()
        setupViewPager()

        // Then fetch materials
        subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME)
        if (subjectName != null) {
            fetchMaterials(subjectName!!)
        }
        
        setupAddNewButton()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbarLayout.toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = subjectName ?: getString(R.string.materials)
        }
    }



    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun setupAddNewButton() {

        val isUserVerified = UserDetails.getUserVerified(this)

        binding.btnAddNew.setOnClickListener {
            if(!isUserVerified) {
                Toast.makeText(
                    this,
                    "Your account needs verification before you can add pdf here.",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }

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
        binding.viewPager.alpha = 0.5f

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
                
                updateFragments()
                showLoading(false)
                binding.viewPager.alpha = 1.0f
                binding.viewPager.isUserInputEnabled = true  // Enable swipe after data is loaded
            }
            .addOnFailureListener { e ->
                showLoading(false)
                binding.viewPager.alpha = 1.0f
                binding.viewPager.isUserInputEnabled = true
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
        
        binding.viewPager.adapter = viewPagerAdapter
        binding.viewPager.offscreenPageLimit = 2  // Keep both fragments in memory
        
        // Initialize fragments with empty lists first
        viewPagerAdapter.addFragment(
            ViewAllFragment.newInstance(ArrayList(materialsList), subjectName),
            "View All"
        )
        viewPagerAdapter.addFragment(
            ChapterWiseFragment.newInstance(ArrayList(materialsList)),
            "Chapter Wise"
        )

        // Disable ViewPager swipe until data is loaded
        binding.viewPager.isUserInputEnabled = false
        
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "View All"
                1 -> "Chapter Wise"
                else -> ""
            }
        }.attach()

        // Add page change callback to ensure fragments are updated
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val fragment = supportFragmentManager.findFragmentByTag("f$position")
                when (fragment) {
                    is ViewAllFragment -> fragment.updateMaterials(materialsList)
                    is ChapterWiseFragment -> fragment.updateMaterials(materialsList)
                }
            }
        })
    }

    private fun updateFragments() {
        try {
            val viewPagerAdapter = binding.viewPager.adapter as? ViewPagerAdapter
            viewPagerAdapter?.updateMaterials(ArrayList(materialsList))

            // Update both fragments explicitly
            supportFragmentManager.fragments.forEach { fragment ->
                when (fragment) {
                    is ViewAllFragment -> fragment.updateMaterials(materialsList)
                    is ChapterWiseFragment -> fragment.updateMaterials(materialsList)
                }
            }

            // Ensure the current fragment is properly updated
            binding.viewPager.post {
                val currentFragment = supportFragmentManager.findFragmentByTag("f${binding.viewPager.currentItem}")
                when (currentFragment) {
                    is ViewAllFragment -> currentFragment.updateMaterials(materialsList)
                    is ChapterWiseFragment -> currentFragment.updateMaterials(materialsList)
                }
            }
        } catch (e: Exception) {
            Log.e("ViewMaterialActivity", "Error updating fragments: ${e.message}")
            Toast.makeText(this, "Error updating materials", Toast.LENGTH_SHORT).show()
        }
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

        // Force update the current fragment
        binding.viewPager.post {
            updateFragments()
        }
    }

    companion object {
        const val EXTRA_SUBJECT_NAME = "subject_name"
    }
} 