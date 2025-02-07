package com.babbira.studentspartner.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.babbira.studentspartner.R
import com.babbira.studentspartner.databinding.ActivityViewMaterialBinding
import com.babbira.studentspartner.ui.fragments.AddNewMaterialFragment
import com.babbira.studentspartner.ui.fragments.ChapterWiseFragment
import com.babbira.studentspartner.ui.fragments.ViewAllFragment

class ViewMaterialActivity : AppCompatActivity() {
    private lateinit var binding: ActivityViewMaterialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewMaterialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get subject name from intent
        val subjectName = intent.getStringExtra(EXTRA_SUBJECT_NAME) ?: return

      

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
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, AddNewMaterialFragment.newInstance(subjectName!!))
                        .commit()
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

    companion object {
        const val EXTRA_SUBJECT_NAME = "subject_name"
    }
} 