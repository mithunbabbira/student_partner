package com.babbira.studentspartner.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.babbira.studentspartner.R

class SubjectListAdapter(
    private var subjects: List<String> = emptyList(),
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<SubjectListAdapter.SubjectViewHolder>() {

    class SubjectViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val subjectName: TextView = view.findViewById(R.id.subjectNameTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subject, parent, false)
        return SubjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubjectViewHolder, position: Int) {
        val subject = subjects[position]
        holder.subjectName.text = subject
        holder.itemView.setOnClickListener { onItemClick(subject) }
    }

    override fun getItemCount() = subjects.size

    fun updateSubjects(newSubjects: List<String>) {
        subjects = newSubjects
        notifyDataSetChanged()
    }
} 