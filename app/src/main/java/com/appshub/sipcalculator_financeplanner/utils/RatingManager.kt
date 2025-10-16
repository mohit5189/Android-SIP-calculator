package com.appshub.sipcalculator_financeplanner.utils

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.util.Log

class RatingManager private constructor() {
    
    companion object {
        private const val TAG = "RatingManager"
        private const val PREFS_NAME = "rating_prefs"
        private const val KEY_HAS_RATED = "has_rated"
        private const val KEY_RATING_PROMPT_COUNT = "rating_prompt_count"
        private const val KEY_FIRST_LAUNCH_TIME = "first_launch_time"
        private const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id="
        
        @Volatile
        private var INSTANCE: RatingManager? = null
        
        fun getInstance(): RatingManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RatingManager().also { INSTANCE = it }
            }
        }
    }
    
    @Volatile
    private var sharedPreferences: SharedPreferences? = null
    
    @Volatile
    private var isInitialized = false
    
    fun initialize(context: Context) {
        try {
            sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            
            // Set first launch time if not already set
            if (getFirstLaunchTime() == 0L) {
                setFirstLaunchTime(System.currentTimeMillis())
            }
            
            isInitialized = true
            Log.d(TAG, "RatingManager initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize RatingManager: ${e.message}")
        }
    }
    
    fun isEligibleToShowRating(): Boolean {
        // Return false if not initialized to prevent crashes
        if (!isInitialized || sharedPreferences == null) {
            return false
        }
        
        try {
            // Don't show if user has already rated
            if (hasUserRated()) {
                return false
            }
            
            // Show rating after 3 days of first launch
            val firstLaunchTime = getFirstLaunchTime()
            val threeDaysInMillis = 3 * 24 * 60 * 60 * 1000L
            val hasUsedAppForThreeDays = (System.currentTimeMillis() - firstLaunchTime) >= threeDaysInMillis
            
            // Don't show more than 3 times
            val promptCount = getRatingPromptCount()
            val hasNotExceededMaxPrompts = promptCount < 3
            
            return hasUsedAppForThreeDays && hasNotExceededMaxPrompts
        } catch (e: Exception) {
            Log.e(TAG, "Error checking rating eligibility: ${e.message}")
            return false
        }
    }
    
    fun showRatingDialog(context: Context) {
        val packageName = context.packageName
        val playStoreUrl = "$PLAY_STORE_URL$packageName"
        
        try {
            // Try to open Play Store app first
            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            playStoreIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(playStoreIntent)
        } catch (e: Exception) {
            // Fallback to browser
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        }
        
        // Mark as rated and increment prompt count
        markAsRated()
        incrementRatingPromptCount()
        
        Log.d(TAG, "Rating dialog shown and marked as rated")
    }
    
    fun dismissRating() {
        incrementRatingPromptCount()
        Log.d(TAG, "Rating dialog dismissed, prompt count: ${getRatingPromptCount()}")
    }
    
    private fun hasUserRated(): Boolean {
        return try {
            sharedPreferences?.getBoolean(KEY_HAS_RATED, false) ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Error reading has_rated: ${e.message}")
            false
        }
    }
    
    private fun markAsRated() {
        try {
            sharedPreferences?.edit()?.putBoolean(KEY_HAS_RATED, true)?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error marking as rated: ${e.message}")
        }
    }
    
    private fun getRatingPromptCount(): Int {
        return try {
            sharedPreferences?.getInt(KEY_RATING_PROMPT_COUNT, 0) ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error reading prompt count: ${e.message}")
            0
        }
    }
    
    private fun incrementRatingPromptCount() {
        try {
            val currentCount = getRatingPromptCount()
            sharedPreferences?.edit()?.putInt(KEY_RATING_PROMPT_COUNT, currentCount + 1)?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error incrementing prompt count: ${e.message}")
        }
    }
    
    private fun getFirstLaunchTime(): Long {
        return try {
            sharedPreferences?.getLong(KEY_FIRST_LAUNCH_TIME, 0L) ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error reading first launch time: ${e.message}")
            0L
        }
    }
    
    private fun setFirstLaunchTime(time: Long) {
        try {
            sharedPreferences?.edit()?.putLong(KEY_FIRST_LAUNCH_TIME, time)?.apply()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting first launch time: ${e.message}")
        }
    }
    
    // For testing purposes
    fun resetRatingData() {
        sharedPreferences?.edit()?.clear()?.apply()
        Log.d(TAG, "Rating data reset")
    }
}