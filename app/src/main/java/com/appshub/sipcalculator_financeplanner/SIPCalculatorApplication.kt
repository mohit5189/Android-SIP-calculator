package com.appshub.sipcalculator_financeplanner

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.appshub.sipcalculator_financeplanner.utils.AdManager

class SIPCalculatorApplication : Application(), Application.ActivityLifecycleCallbacks, DefaultLifecycleObserver {
    
    private var currentActivity: Activity? = null
    private var wasInBackground = true
    
    companion object {
        private const val TAG = "SIPApplication"
    }
    
    override fun onCreate() {
        super<Application>.onCreate()
        
        Log.d(TAG, "Application onCreate - Initializing AdManager")
        
        // Initialize AdManager
        AdManager.getInstance().initializeAds(this)
        
        // Register lifecycle callbacks
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    
    override fun onStart(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStart(owner)
        Log.d(TAG, "App comes to foreground, wasInBackground: $wasInBackground")
        wasInBackground = false
    }
    
    override fun onStop(owner: LifecycleOwner) {
        super<DefaultLifecycleObserver>.onStop(owner)
        Log.d(TAG, "App goes to background")
        wasInBackground = true
    }
    
    
    // ActivityLifecycleCallbacks
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    
    override fun onActivityStarted(activity: Activity) {
        Log.d(TAG, "Activity started: ${activity.javaClass.simpleName}")
        currentActivity = activity
    }
    
    override fun onActivityResumed(activity: Activity) {
        Log.d(TAG, "Activity resumed: ${activity.javaClass.simpleName}")
        currentActivity = activity
    }
    
    override fun onActivityPaused(activity: Activity) {}
    
    override fun onActivityStopped(activity: Activity) {}
    
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    
    override fun onActivityDestroyed(activity: Activity) {
        if (currentActivity == activity) {
            currentActivity = null
        }
    }
}