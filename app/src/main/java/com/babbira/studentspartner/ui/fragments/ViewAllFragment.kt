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
        private const val ARG_MATERIALS = "materials"

        fun newInstance(materials: List<SubjectMaterial>): ViewAllFragment {
            return ViewAllFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_MATERIALS, ArrayList(materials))
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
} 