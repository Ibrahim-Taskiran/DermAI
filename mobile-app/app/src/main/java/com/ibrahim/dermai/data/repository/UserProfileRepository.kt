package com.ibrahim.dermai.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.ibrahim.dermai.data.model.PatientMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun saveUserProfile(metadata: PatientMetadata) {
        val json = gson.toJson(metadata)
        prefs.edit().putString(KEY_USER_PROFILE, json).apply()
    }

    fun getUserProfile(): PatientMetadata? {
        val json = prefs.getString(KEY_USER_PROFILE, null) ?: return null
        return try {
            gson.fromJson(json, PatientMetadata::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearProfile() {
        prefs.edit().remove(KEY_USER_PROFILE).apply()
    }

    companion object {
        private const val PREFS_NAME = "dermai_user_profile"
        private const val KEY_USER_PROFILE = "patient_metadata"
    }
}
