package com.babbira.studentspartner.adapters

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.babbira.studentspartner.data.model.SubjectMaterial
import com.babbira.studentspartner.databinding.ItemMaterialBinding
import com.babbira.studentspartner.utils.UserDetails
import java.io.File
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MaterialAdapter(
    private val materials: MutableList<SubjectMaterial>
) : RecyclerView.Adapter<MaterialAdapter.MaterialViewHolder>() {

    private var onDeleteClickListener: ((SubjectMaterial) -> Unit)? = null

    fun setOnDeleteClickListener(listener: (SubjectMaterial) -> Unit) {
        onDeleteClickListener = listener
    }

    fun updateMaterials(newMaterials: List<SubjectMaterial>) {
        materials.clear()
        materials.addAll(newMaterials)
        notifyDataSetChanged()
    }

    inner class MaterialViewHolder(
        private val binding: ItemMaterialBinding,
        private val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(material: SubjectMaterial) {
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
            val fileName = "${material.filename}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

            if (file.exists() && file.length() > 0) {
                openPdf(file, material)
            } else {
                downloadPdf(material.pdfUrl, fileName, material)
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun downloadPdf(url: String, fileName: String, material: SubjectMaterial) {
            binding.btnView.isVisible = false
            binding.progressDownload.isVisible = true

            val request = DownloadManager.Request(Uri.parse(url))
                .setTitle(fileName)
                .setDescription("Downloading PDF")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, fileName)

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            // Create a BroadcastReceiver to monitor download completion
            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        context?.unregisterReceiver(this)
                        binding.btnView.isVisible = true
                        binding.progressDownload.isVisible = false

                        val file = File(context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
                        if (file.exists() && file.length() > 0) {
                            openPdf(file, material)
                        } else {
                            Toast.makeText(context, "Failed to download PDF. Please try again.", Toast.LENGTH_LONG).show()
                            file.delete() // Delete the potentially corrupted file
                        }
                    }
                }
            }

            // Register the BroadcastReceiver
            context.registerReceiver(
                onComplete,
                IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                Context.RECEIVER_NOT_EXPORTED
            )

            // Add download status check
            val query = DownloadManager.Query().setFilterById(downloadId)
            Thread {
                var downloading = true
                while (downloading) {
                    val cursor = downloadManager.query(query)
                    cursor.moveToFirst()
                    
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_FAILED -> {
                            downloading = false
                            binding.root.post {
                                binding.btnView.isVisible = true
                                binding.progressDownload.isVisible = false
                                Toast.makeText(context, "Download failed. Please try again.", Toast.LENGTH_LONG).show()
                            }
                            context.unregisterReceiver(onComplete)
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            downloading = false
                        }
                    }
                    cursor.close()
                    Thread.sleep(1000)
                }
            }.start()
        }

        private fun openPdf(file: File, material: SubjectMaterial) {
            try {
                if (!file.exists() || file.length() == 0L) {
                    Toast.makeText(context, "Invalid PDF file. Retrying download...", Toast.LENGTH_LONG).show()
                    file.delete()
                    downloadPdf(material.pdfUrl, file.name, material)
                    return
                }

                // Create URI using FileProvider
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )

                // Create intent with correct MIME type and flags
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }

                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    // If no activity can handle PDF, try alternative method
                    val alternativeIntent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/*")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    }
                    
                    try {
                        context.startActivity(alternativeIntent)
                    } catch (e: Exception) {
                        // If still fails, show dialog to install PDF viewer
                        MaterialAlertDialogBuilder(context)
                            .setTitle("PDF Viewer Required")
                            .setMessage("You need a PDF viewer app to open this file. Would you like to install one from Play Store?")
                            .setPositiveButton("Install") { _, _ ->
                                val marketIntent = Intent(Intent.ACTION_VIEW, 
                                    Uri.parse("market://search?q=pdf+viewer&c=apps"))
                                context.startActivity(marketIntent)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error opening PDF: ${e.message}", Toast.LENGTH_LONG).show()
                file.delete()
                downloadPdf(material.pdfUrl, file.name, material)
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
        holder.bind(materials[position])
    }

    override fun getItemCount() = materials.size
} 