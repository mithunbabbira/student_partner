package com.babbira.studentspartner.utils


import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater

import android.app.AlertDialog
import com.babbira.studentspartner.R

class LoaderManager private constructor() {
    private var progressDialog: AlertDialog? = null

    fun showLoader(context: Context) {
        if (progressDialog?.isShowing == true) return

        val dialogView = LayoutInflater.from(context).inflate(R.layout.layout_loader, null)
        progressDialog = AlertDialog.Builder(context, R.style.TransparentDialog)
            .setView(dialogView)
            .setCancelable(false)
            .create()
        
        progressDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        progressDialog?.show()
    }

    fun hideLoader() {
        progressDialog?.dismiss()
        progressDialog = null
    }

    companion object {
        @Volatile
        private var instance: LoaderManager? = null

        fun getInstance(): LoaderManager {
            return instance ?: synchronized(this) {
                instance ?: LoaderManager().also { instance = it }
            }
        }
    }
} 