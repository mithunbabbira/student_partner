package com.babbira.studentspartner.adapters
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.babbira.studentspartner.R
import com.babbira.studentspartner.databinding.ItemClassmateBinding
import com.babbira.studentspartner.ui.ClassmateModel
import com.bumptech.glide.Glide

class ClassmatesAdapter(
    private val classmates: List<ClassmateModel>,
    private val onWhatsAppClick: (phone: String) -> Unit,
    private val onCallClick: (phone: String) -> Unit
) : RecyclerView.Adapter<ClassmatesAdapter.ViewHolder>() {

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
            
            // Load profile image using Glide
            Glide.with(profileImageView)
                .load(classmate.profileImageUrl)
                .placeholder(R.drawable.ic_profile_placeholder)
                .error(R.drawable.ic_profile_placeholder)
                .circleCrop()
                .into(profileImageView)

            // Set click listeners
            whatsappButton.setOnClickListener {
                onWhatsAppClick(classmate.phone)
            }

            callButton.setOnClickListener {
                onCallClick(classmate.phone)
            }
        }
    }

    override fun getItemCount() = classmates.size
} 