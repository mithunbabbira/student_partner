package com.babbira.studentspartner.adapters

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.babbira.studentspartner.data.model.SubjectMaterial
import com.babbira.studentspartner.databinding.ItemMaterialBinding
import com.babbira.studentspartner.utils.UserDetails
import java.io.File

class MaterialAdapter(
    private val materials: List<SubjectMaterial>
) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {

    private var onDeleteClickListener: ((SubjectMaterial) -> Unit)? = null

    fun setOnDeleteClickListener(listener: (SubjectMaterial) -> Unit) {
        onDeleteClickListener = listener
    }

    class MaterialViewHolder(
        private val binding: ItemMaterialBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(material: SubjectMaterial, onDeleteClickListener: ((SubjectMaterial) -> Unit)?) {
            binding.apply {
                tvTitle.text = material.title
                tvDescription.text = material.description
                tvUploadedBy.text = "Uploaded by: ${material.addedBy}"
                
                // Show delete button only if current user is the uploader
                btnDelete.isVisible = material.userId == UserDetails.getUserId(context)
                btnDelete.setOnClickListener {
                    onDeleteClickListener?.invoke(material)
                }
                
                btnView.setOnClickListener {
                    downloadAndViewPdf(material)
                }
            }
        }

        private fun downloadAndViewPdf(material: SubjectMaterial) {
            val fileName = "${material.title}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

            if (file.exists()) {
                openPdf(file)
            } else {
                downloadPdf(material.pdfUrl, fileName)
            }
        }

        private fun downloadPdf(url: String, fileName: String) {
            binding.btnView.isVisible = false
            binding.progressDownload.isVisible = true

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Downloading PDF")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            // You might want to register a BroadcastReceiver to handle download completion
            // For now, we'll just show the button again after a delay
            binding.root.postDelayed({
                binding.btnView.isVisible = true
                binding.progressDownload.isVisible = false
                val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                if (file.exists()) {
                    openPdf(file)
                }
            }, 2000)
        }

        private fun openPdf(file: File) {
            try {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                }
                context.startActivity(intent)
            } catch (e: Exception) {
                Toast.makeText(context, "Error opening PDF: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MaterialViewHolder {
        val binding = ItemMaterialBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MaterialViewHolder(binding, parent.context)
    }

    override fun onBindViewHolder(holder: MaterialViewHolder, position: Int) {
        holder.bind(materials[position], onDeleteClickListener)
    }

    override fun getItemCount() = materials.size
} 