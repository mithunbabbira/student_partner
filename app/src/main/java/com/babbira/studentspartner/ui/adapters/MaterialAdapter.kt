package com.babbira.studentspartner.adapters

import com.babbira.studentspartner.data.model.SubjectMaterial
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.babbira.studentspartner.databinding.ItemMaterialBinding

class MaterialAdapter(
    private val materials: List<SubjectMaterial>
) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {

    class MaterialViewHolder(private val binding: ItemMaterialBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(material: SubjectMaterial) {
            binding.tvTitle.text = material.title
            binding.tvDescription.text = material.description
            binding.tvUploadedBy.text = "Uploaded by: ${material.addedBy}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemMaterialBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MaterialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        holder.bind(materials[position])
    }

    override fun getItemCount() = materials.size
} 