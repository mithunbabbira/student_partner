package com.babbira.studentspartner.utils

import android.content.Context
import android.os.Environment
import java.io.File

object CommonFunctions {
    
    fun clearAllLocalData(context: Context) {
        // Clear SharedPreferences
        clearSharedPreferences(context)
        
        // Clear downloaded PDFs
        clearDownloadedFiles(context)
    }

    private fun clearSharedPreferences(context: Context) {
        // Clear all user preferences
        val sharedPreferences = context.getSharedPreferences(
            Constants.USER_PREFERENCE,
            Context.MODE_PRIVATE
        )
        sharedPreferences.edit().clear().apply()
    }

    private fun clearDownloadedFiles(context: Context) {
        try {
            // Clear downloaded PDFs from external files directory
            val downloadDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            downloadDir?.listFiles()?.forEach { file ->
                if (file.isFile && file.extension.equals("pdf", ignoreCase = true)) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun deleteAllUserData(context: Context, onComplete: (Boolean) -> Unit) {
        try {
            // Clear all local data
            clearAllLocalData(context)
            
            // Callback with success
            onComplete(true)
        } catch (e: Exception) {
            e.printStackTrace()
            // Callback with failure
            onComplete(false)
        }
    }
} 