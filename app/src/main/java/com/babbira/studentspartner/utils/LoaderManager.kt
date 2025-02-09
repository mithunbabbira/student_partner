package com.babbira.studentspartner.utils

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.babbira.studentspartner.R

class LoaderManager private constructor() {
    private var loaderView: View? = null

    companion object {
        @Volatile
        private var instance: LoaderManager? = null

        fun getInstance(): LoaderManager {
            return instance ?: synchronized(this) {
                instance ?: LoaderManager().also { instance = it }
            }
        }
    }

    fun showLoader(activity: Activity) {
        if (loaderView == null) {
            loaderView = LayoutInflater.from(activity).inflate(R.layout.layout_loader, null)
            val rootView = activity.findViewById<ViewGroup>(android.R.id.content)
            rootView.addView(loaderView, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            ))
        }
        loaderView?.visibility = View.VISIBLE
    }

    fun hideLoader() {
        loaderView?.visibility = View.GONE
    }
} 