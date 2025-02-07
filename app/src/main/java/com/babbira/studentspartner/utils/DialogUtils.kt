package com.babbira.studentspartner.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogUtils {
    fun showConfirmationDialog(
        context: Context,
        title: String,
        message: String,
        positiveButtonText: String = "Yes",
        negativeButtonText: String = "No",
        onPositiveClick: () -> Unit,
        onNegativeClick: (() -> Unit)? = null
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(positiveButtonText) { dialog, _ ->
                dialog.dismiss()
                onPositiveClick()
            }
            .setNegativeButton(negativeButtonText) { dialog, _ ->
                dialog.dismiss()
                onNegativeClick?.invoke()
            }
            .setCancelable(false)
            .show()
    }
} 