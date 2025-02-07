package com.babbira.studentspartner.ui.fragments

import com.babbira.studentspartner.data.model.SubjectMaterial
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.babbira.studentspartner.R
import com.babbira.studentspartner.adapters.MaterialAdapter

import com.babbira.studentspartner.databinding.FragmentViewAllBinding
import com.babbira.studentspartner.ui.fragments.AddNewMaterialFragment.Companion
import com.babbira.studentspartner.utils.UserDetails

import com.google.firebase.firestore.FirebaseFirestore

class ViewAllFragment : Fragment() {
    companion object {
        private const val ARG_SUBJECT_NAME = "subject_name"

        fun newInstance(subjectName: String): ViewAllFragment {
            return ViewAllFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_SUBJECT_NAME, subjectName)
                }
            }
        }
    }



    private lateinit var binding: FragmentViewAllBinding
    private lateinit var materialAdapter: MaterialAdapter
    private val materialsList = mutableListOf<SubjectMaterial>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentViewAllBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        fetchMaterials()
    }

    private fun setupRecyclerView() {
        materialAdapter = MaterialAdapter(materialsList)
        binding.rvMaterials.apply {
            adapter = materialAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun fetchMaterials() {
        // Show loading progress
        binding.progressBar.isVisible = true



        val college = UserDetails.getUserCollege(requireContext())
        val combination = UserDetails.getUserCombination(requireContext())
        val semester = UserDetails.getUserSemester(requireContext())
        val subjectName = arguments?.getString(ViewAllFragment.ARG_SUBJECT_NAME)

        if (subjectName != null) {
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
                    materialAdapter.notifyDataSetChanged()
                    binding.progressBar.isVisible = false

                    // Show empty state if no materials
                    binding.tvEmptyState.isVisible = materialsList.isEmpty()
                }
                .addOnFailureListener { e ->
                    binding.progressBar.isVisible = false
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
} 