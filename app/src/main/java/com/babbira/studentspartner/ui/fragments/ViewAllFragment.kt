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
import com.google.firebase.storage.FirebaseStorage
import android.app.AlertDialog

class ViewAllFragment : Fragment() {
    companion object {
        private const val ARG_MATERIALS = "materials"
        private const val ARG_SUBJECT_NAME = "subjectName"

        fun newInstance(materials: List<SubjectMaterial>, subjectName: String): ViewAllFragment {
            return ViewAllFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_MATERIALS, ArrayList(materials))
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
        loadMaterials()
    }

    private fun setupRecyclerView() {
        materialAdapter = MaterialAdapter(materialsList)
        materialAdapter.setOnDeleteClickListener { material ->
            showDeleteConfirmationDialog(material)
        }
        binding.rvMaterials.apply {
            adapter = materialAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun loadMaterials() {
        arguments?.getParcelableArrayList<SubjectMaterial>(ARG_MATERIALS)?.let { materials ->
            materialsList.clear()
            materialsList.addAll(materials)
            materialAdapter.notifyDataSetChanged()
            binding.tvEmptyState.isVisible = materialsList.isEmpty()
        }
    }

    fun updateMaterials(newMaterials: List<SubjectMaterial>) {
        materialsList.clear()
        materialsList.addAll(newMaterials)
        materialAdapter.notifyDataSetChanged()
        binding.tvEmptyState.isVisible = materialsList.isEmpty()
    }

    private fun showDeleteConfirmationDialog(material: SubjectMaterial) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.dialog_title_delete_material))
            .setMessage(getString(R.string.dialog_message_delete_confirmation))
            .setPositiveButton(getString(R.string.dialog_button_yes)) { _, _ ->
                deleteMaterial(material)
            }
            .setNegativeButton(getString(R.string.dialog_button_no), null)
            .show()
    }

    private fun deleteMaterial(material: SubjectMaterial) {
        binding.progressBar.isVisible = true
        
        val college = UserDetails.getUserCollege(requireContext())
        val combination = UserDetails.getUserCombination(requireContext())
        val semester = UserDetails.getUserSemester(requireContext())
        val subjectName = arguments?.getString(ARG_SUBJECT_NAME)

        if (subjectName == null) {
            Toast.makeText(context, getString(R.string.error_subject_name_missing), Toast.LENGTH_SHORT).show()
            binding.progressBar.isVisible = false
            return
        }

        // Delete from Firebase Storage
        val storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(material.pdfUrl)
        storageRef.delete().addOnCompleteListener { storageTask ->
            if (storageTask.isSuccessful) {
                // Delete from Firestore
                db.collection("collegeList")
                    .document(college)
                    .collection("combination")
                    .document(combination)
                    .collection("semesters")
                    .document(semester)
                    .collection("subjectList")
                    .document(subjectName)
                    .collection("materials")
                    .document(material.id)
                    .delete()
                    .addOnSuccessListener {
                        materialsList.remove(material)
                        materialAdapter.notifyDataSetChanged()
                        binding.tvEmptyState.isVisible = materialsList.isEmpty()
                        binding.progressBar.isVisible = false
                        Toast.makeText(context, getString(R.string.success_material_deleted), Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        binding.progressBar.isVisible = false
                        Toast.makeText(context, getString(R.string.error_delete_material_failed, e.message), Toast.LENGTH_SHORT).show()
                    }
            } else {
                binding.progressBar.isVisible = false
                Toast.makeText(context, getString(R.string.error_delete_file_failed, storageTask.exception?.message), Toast.LENGTH_SHORT).show()
            }
        }
    }
} 