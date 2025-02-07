package com.babbira.studentspartner.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.babbira.studentspartner.R
import com.babbira.studentspartner.databinding.ActivityViewMaterialBinding
import com.babbira.studentspartner.ui.fragments.AddNewMaterialFragment
import com.babbira.studentspartner.ui.fragments.ChapterWiseFragment
import com.babbira.studentspartner.ui.fragments.ViewAllFragment
import androidx.fragment.app.Fragment

class ViewMaterialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewMaterialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get subject name from intent
        val subjectName = intent.getStringExtra("subject_name") ?: return

        // Set up bottom navigation
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_chapter_wise -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ChapterWiseFragment())
                        .commit()
                    true
                }
                R.id.navigation_view_all -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, ViewAllFragment())
                        .commit()
                    true
                }
                R.id.navigation_add_new -> {
                    loadFragment(AddNewMaterialFragment())
                    true
                }
                else -> false
            }
        }

        // Set default fragment
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_chapter_wise
        }
    }

    private fun loadFragment(fragment: Fragment) {
        val bundle = Bundle().apply {
            // Get the subject name from intent or wherever it's available
            val subjectName = intent.getStringExtra("subject_name")
            putString("subject_name", subjectName)
        }
        
        fragment.arguments = bundle
        
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    companion object {
        const val EXTRA_SUBJECT_NAME = "subject_name"
    }
} 