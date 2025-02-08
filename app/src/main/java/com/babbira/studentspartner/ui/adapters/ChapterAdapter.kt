package com.babbira.studentspartner.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.babbira.studentspartner.databinding.ItemChapterBinding
import com.babbira.studentspartner.ui.fragments.ChapterWiseFragment

class ChapterAdapter(
    private val chapters: List<ChapterWiseFragment.ChapterWithMaterials>
) : RecyclerView.Adapter<ChapterAdapter.ChapterViewHolder>() {

    inner class ChapterViewHolder(
        private val binding: ItemChapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val materialAdapter = MaterialAdapter(mutableListOf())

        init {
            binding.chapterHeader.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val chapter = chapters[position]
                    chapter.isExpanded = !chapter.isExpanded
                    binding.ivExpandIcon.rotation = if (chapter.isExpanded) 180f else 0f
                    binding.rvChapterMaterials.isVisible = chapter.isExpanded
                }
            }

            binding.rvChapterMaterials.apply {
                adapter = materialAdapter
                layoutManager = LinearLayoutManager(context)
            }
        }

        fun bind(chapter: ChapterWiseFragment.ChapterWithMaterials) {
            binding.tvChapterTitle.text = "Chapter ${chapter.chapterNumber}"
            binding.ivExpandIcon.rotation = if (chapter.isExpanded) 180f else 0f
            binding.rvChapterMaterials.isVisible = chapter.isExpanded

            // Update materials in the nested RecyclerView
            (binding.rvChapterMaterials.adapter as MaterialAdapter).apply {
                updateMaterials(chapter.materials)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(chapters[position])
    }

    override fun getItemCount() = chapters.size
} 