package com.babbira.studentspartner.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.babbira.studentspartner.R
import com.babbira.studentspartner.data.model.SubjectMaterial

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




    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chapter_wise, container, false)
    }


    fun updateMaterials(newMaterials: List<SubjectMaterial>) {

    }
} 