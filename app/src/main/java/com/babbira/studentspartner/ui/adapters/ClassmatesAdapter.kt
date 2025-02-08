package com.babbira.studentspartner.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.babbira.studentspartner.databinding.ItemClassmateBinding
import com.babbira.studentspartner.ui.ClassmateModel

class ClassmatesAdapter(private val classmates: List<ClassmateModel>) :
    RecyclerView.Adapter<ClassmatesAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemClassmateBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemClassmateBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val classmate = classmates[position]
        holder.binding.apply {
            nameTextView.text = classmate.name
            phoneTextView.text = classmate.phone
            // Add click listeners if needed
        }
    }

    override fun getItemCount() = classmates.size
} 