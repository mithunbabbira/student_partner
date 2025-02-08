package com.babbira.studentspartner.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.babbira.studentspartner.R
import com.babbira.studentspartner.adapters.ChapterAdapter
import com.babbira.studentspartner.data.model.SubjectMaterial
import com.babbira.studentspartner.databinding.FragmentChapterWiseBinding

class ChapterWiseFragment : Fragment() {
    companion object {
        private const val ARG_MATERIALS = "materials"

        fun newInstance(materials: List<SubjectMaterial>): ChapterWiseFragment {
            return ChapterWiseFragment().apply {
                arguments = Bundle().apply {
                    putParcelableArrayList(ARG_MATERIALS, ArrayList(materials))
                }
            }
        }
    }

    private lateinit var binding: FragmentChapterWiseBinding
    private lateinit var chapterAdapter: ChapterAdapter
    private val chaptersList = mutableListOf<ChapterWithMaterials>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize adapter with empty list
        chapterAdapter = ChapterAdapter(chaptersList)
        
        // Load initial materials if available
        arguments?.getParcelableArrayList<SubjectMaterial>(ARG_MATERIALS)?.let { materials ->
            if (materials.isNotEmpty()) {
                updateMaterials(materials)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChapterWiseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        // Remove adapter initialization here since it's done in onCreate
        binding.rvChapters.apply {
            adapter = chapterAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    fun updateMaterials(newMaterials: List<SubjectMaterial>) {
        if (!isAdded) return  // Skip if fragment is not added to activity
        
        if (!this::chapterAdapter.isInitialized) {
            chapterAdapter = ChapterAdapter(mutableListOf())
        }

        val groupedMaterials = newMaterials.groupBy { it.chapterNumber }
            .map { (chapter, materials) ->
                ChapterWithMaterials(
                    chapterNumber = chapter,
                    materials = materials.sortedBy { it.title }
                )
            }
            .sortedBy { it.chapterNumber }

        chaptersList.clear()
        chaptersList.addAll(groupedMaterials)
        chapterAdapter.notifyDataSetChanged()
        
        view?.post {
            binding.tvEmptyState.isVisible = chaptersList.isEmpty()
        }
    }

    data class ChapterWithMaterials(
        val chapterNumber: Int,
        val materials: List<SubjectMaterial>,
        var isExpanded: Boolean = false
    )
} 