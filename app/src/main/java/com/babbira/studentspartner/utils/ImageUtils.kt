package com.babbira.studentspartner.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream

object ImageUtils {
    fun compressImage(imageUri: Uri, contentResolver: ContentResolver): ByteArray? {
        return try {
            // Load the image
            val inputStream = contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Calculate new dimensions while maintaining aspect ratio
            val maxDimension = 800 // Max width or height
            val scale = if (originalBitmap.width > originalBitmap.height) {
                maxDimension.toFloat() / originalBitmap.width
            } else {
                maxDimension.toFloat() / originalBitmap.height
            }

            val newWidth = (originalBitmap.width * scale).toInt()
            val newHeight = (originalBitmap.height * scale).toInt()

            // Resize the image
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                newWidth,
                newHeight,
                true
            )

            // Compress to JPEG
            val outputStream = ByteArrayOutputStream()
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

            // Clean up
            if (originalBitmap != resizedBitmap) {
                originalBitmap.recycle()
            }
            resizedBitmap.recycle()

            outputStream.toByteArray()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
} 