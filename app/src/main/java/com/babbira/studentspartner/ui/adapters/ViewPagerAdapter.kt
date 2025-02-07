package com.babbira.studentspartner.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.babbira.studentspartner.data.model.SubjectMaterial
import com.babbira.studentspartner.ui.fragments.ChapterWiseFragment
import com.babbira.studentspartner.ui.fragments.ViewAllFragment

class ViewPagerAdapter(
    fragmentManager: FragmentManager,
    lifecycle: Lifecycle
) : FragmentStateAdapter(fragmentManager, lifecycle) {

    private val fragments = mutableListOf<Fragment>()
    private val fragmentTitles = mutableListOf<String>()

    fun addFragment(fragment: Fragment, title: String) {
        fragments.add(fragment)
        fragmentTitles.add(title)
    }

    fun updateMaterials(materials: List<SubjectMaterial>) {
        fragments.forEach { fragment ->
            when (fragment) {
                is ViewAllFragment -> fragment.updateMaterials(materials)
                is ChapterWiseFragment -> fragment.updateMaterials(materials)
            }
        }
    }

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
} 