package com.babbira.studentspartner.utils

import android.content.Context
import android.content.SharedPreferences

object UserDetails {
    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(Constants.USER_PREFERENCE, Context.MODE_PRIVATE)
    }

    private fun getEditor(context: Context): SharedPreferences.Editor {
        return getPreferences(context).edit()
    }

    private const val KEY_USER_VERIFIED = "user_verified"

    // User Name
    fun setUserName(context: Context, userName: String) {
        getEditor(context).apply {
            putString(Constants.PreferenceKeys.USER_NAME, userName)
            apply()
        }
    }

    fun getUserName(context: Context): String {
        return getPreferences(context).getString(Constants.PreferenceKeys.USER_NAME, "") ?: ""
    }

    // User Email
    fun setUserEmail(context: Context, email: String) {
        getEditor(context).apply {
            putString(Constants.PreferenceKeys.USER_EMAIL, email)
            apply()
        }
    }

    fun getUserEmail(context: Context): String {
        return getPreferences(context).getString(Constants.PreferenceKeys.USER_EMAIL, "") ?: ""
    }

    // User Phone
    fun setUserPhone(context: Context, phone: String) {
        getEditor(context).apply {
            putString(Constants.PreferenceKeys.USER_PHONE, phone)
            apply()
        }
    }

    fun getUserPhone(context: Context): String {
        return getPreferences(context).getString(Constants.PreferenceKeys.USER_PHONE, "") ?: ""
    }

    // User College
    fun setUserCollege(context: Context, college: String) {
        getEditor(context).apply {
            putString(Constants.PreferenceKeys.USER_COLLEGE, college)
            apply()
        }
    }

    fun getUserCollege(context: Context): String {
        return getPreferences(context).getString(Constants.PreferenceKeys.USER_COLLEGE, "") ?: ""
    }

    // User Combination
    fun setUserCombination(context: Context, combination: String) {
        getEditor(context).apply {
            putString(Constants.PreferenceKeys.USER_COMBINATION, combination)
            apply()
        }
    }

    fun getUserCombination(context: Context): String {
        return getPreferences(context).getString(Constants.PreferenceKeys.USER_COMBINATION, "") ?: ""
    }

    // User Semester
    fun setUserSemester(context: Context, semester: String) {
        getEditor(context).apply {
            putString(Constants.PreferenceKeys.USER_SEMESTER, semester)
            apply()
        }
    }

    fun getUserSemester(context: Context): String {
        return getPreferences(context).getString(Constants.PreferenceKeys.USER_SEMESTER, "") ?: ""
    }

    // User Section
    fun setUserSection(context: Context, section: String) {
        getEditor(context).apply {
            putString(Constants.PreferenceKeys.USER_SECTION, section)
            apply()
        }
    }

    fun getUserSection(context: Context): String {
        return getPreferences(context).getString(Constants.PreferenceKeys.USER_SECTION, "") ?: ""
    }

    // Login Status
    fun setLoggedIn(context: Context, isLoggedIn: Boolean) {
        getEditor(context).apply {
            putBoolean(Constants.PreferenceKeys.IS_LOGGED_IN, isLoggedIn)
            apply()
        }
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(Constants.PreferenceKeys.IS_LOGGED_IN, false)
    }

    // User ID
    fun setUserId(context: Context, userId: String) {
        getEditor(context).apply {
            putString(Constants.PreferenceKeys.USER_ID, userId)
            apply()
        }
    }

    fun getUserId(context: Context): String {
        return getPreferences(context).getString(Constants.PreferenceKeys.USER_ID, "") ?: ""
    }

    // User Profile Image URL
    fun setProfileImageUrl(context: Context, imageUrl: String) {
        getEditor(context).apply {
            putString(Constants.PreferenceKeys.USER_PROFILE_IMAGE, imageUrl)
            apply()
        }
    }

    fun getProfileImageUrl(context: Context): String {
        return getPreferences(context).getString(Constants.PreferenceKeys.USER_PROFILE_IMAGE, "") ?: ""
    }

    // User Verified
    fun setUserVerified(context: Context, verified: Boolean) {
        getEditor(context).apply {
            putBoolean(KEY_USER_VERIFIED, verified)
            apply()
        }
    }
    
    fun getUserVerified(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_USER_VERIFIED, false)
    }

    // Clear all user data
    fun clearUserData(context: Context) {
        // Clear SharedPreferences
        getEditor(context).apply {
            clear()
            apply()
        }
        
        // Also clear all local data using CommonFunctions
        CommonFunctions.clearAllLocalData(context)
    }
} 