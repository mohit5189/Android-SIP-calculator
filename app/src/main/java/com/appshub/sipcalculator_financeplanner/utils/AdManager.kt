package com.appshub.sipcalculator_financeplanner.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager private constructor() {
    
    companion object {
        private const val TAG = "AdManager"
        
        // Ad Unit IDs
        private const val SIP_AD_UNIT_ID = "ca-app-pub-8809413893982569/5353999763"
        private const val SWP_AD_UNIT_ID = "ca-app-pub-8809413893982569/4344170423"
        private const val GOAL_AD_UNIT_ID = "ca-app-pub-8809413893982569/3326602222"
        
        // Test Ad Unit IDs - use during development
        private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        
        // Set to true to use test ads for debugging
        private const val USE_TEST_ADS = false
        
        // SharedPreferences constants
        private const val PREFS_NAME = "ad_manager_prefs"
        private const val KEY_CALCULATION_COUNT = "calculation_count"
        
        // Ad trigger counts
        private const val AD_TRIGGER_COUNT = 3 // show interstitial every 3 calculations
        
        // Calculation types
        enum class CalculationType {
            SIP, SWP, GOAL
        }
        
        @Volatile
        private var INSTANCE: AdManager? = null
        
        fun getInstance(): AdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AdManager().also { INSTANCE = it }
            }
        }
    }
    
    private var sharedPreferences: SharedPreferences? = null
    private var sipInterstitialAd: InterstitialAd? = null
    private var swpInterstitialAd: InterstitialAd? = null
    private var goalInterstitialAd: InterstitialAd? = null
    
    fun initializeAds(context: Context) {
        // Initialize SharedPreferences
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        
        MobileAds.initialize(context) { initializationStatus ->
            Log.d(TAG, "AdMob initialized: ${initializationStatus.adapterStatusMap}")
        }
        
        // Log current counts on initialization
        val calculationCount = getCalculationCount()
        Log.d(TAG, "AdManager initialized. Calculation count: $calculationCount")
        
        // Pre-load interstitial ads immediately
        loadSipInterstitialAd(context)
        loadSwpInterstitialAd(context)
        loadGoalInterstitialAd(context)
    }
    
    private fun loadSipInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(context, SIP_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "SIP Interstitial ad failed to load: ${adError.message}")
                sipInterstitialAd = null
            }
            
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "SIP Interstitial ad loaded successfully")
                sipInterstitialAd = interstitialAd
                setInterstitialAdCallbacks(interstitialAd, CalculationType.SIP, context)
            }
        })
    }
    
    private fun loadSwpInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(context, SWP_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "SWP Interstitial ad failed to load: ${adError.message}")
                swpInterstitialAd = null
            }
            
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "SWP Interstitial ad loaded successfully")
                swpInterstitialAd = interstitialAd
                setInterstitialAdCallbacks(interstitialAd, CalculationType.SWP, context)
            }
        })
    }
    
    private fun loadGoalInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(context, GOAL_AD_UNIT_ID, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e(TAG, "Goal Interstitial ad failed to load: ${adError.message}")
                goalInterstitialAd = null
            }
            
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Goal Interstitial ad loaded successfully")
                goalInterstitialAd = interstitialAd
                setInterstitialAdCallbacks(interstitialAd, CalculationType.GOAL, context)
            }
        })
    }
    
    private fun setInterstitialAdCallbacks(
        interstitialAd: InterstitialAd,
        type: CalculationType,
        context: Context
    ) {
        interstitialAd.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdClicked() {
                Log.d(TAG, "$type Interstitial ad was clicked")
            }
            
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "$type Interstitial ad dismissed")
                // Reload the ad for next time
                when (type) {
                    CalculationType.SIP -> {
                        sipInterstitialAd = null
                        loadSipInterstitialAd(context)
                    }
                    CalculationType.SWP -> {
                        swpInterstitialAd = null
                        loadSwpInterstitialAd(context)
                    }
                    CalculationType.GOAL -> {
                        goalInterstitialAd = null
                        loadGoalInterstitialAd(context)
                    }
                }
            }
            
            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                Log.e(TAG, "$type Interstitial ad failed to show: ${adError.message}")
                // Reload the ad for next time
                when (type) {
                    CalculationType.SIP -> {
                        sipInterstitialAd = null
                        loadSipInterstitialAd(context)
                    }
                    CalculationType.SWP -> {
                        swpInterstitialAd = null
                        loadSwpInterstitialAd(context)
                    }
                    CalculationType.GOAL -> {
                        goalInterstitialAd = null
                        loadGoalInterstitialAd(context)
                    }
                }
            }
            
            override fun onAdImpression() {
                Log.d(TAG, "$type Interstitial ad recorded an impression")
            }
            
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "$type Interstitial ad showed full screen content")
            }
        }
    }
    
    fun onCalculationPerformed(activity: Activity, calculationType: CalculationType) {
        val newCount = incrementCalculationCount()
        Log.d(TAG, "Calculation performed. Count: $newCount, Type: $calculationType")
        
        // Show ad every 3 calculations
        if (newCount % AD_TRIGGER_COUNT == 0) {
            Log.d(TAG, "Triggering ad after $newCount calculations")
            showInterstitialAd(activity, calculationType)
        }
    }
    
    private fun showInterstitialAd(activity: Activity, calculationType: CalculationType) {
        val interstitialAd = when (calculationType) {
            CalculationType.SIP -> sipInterstitialAd
            CalculationType.SWP -> swpInterstitialAd
            CalculationType.GOAL -> goalInterstitialAd
        }
        
        if (interstitialAd != null) {
            Log.d(TAG, "Showing $calculationType interstitial ad")
            interstitialAd.show(activity)
        } else {
            Log.w(TAG, "$calculationType interstitial ad not ready. Reloading...")
            // Try to reload the ad
            when (calculationType) {
                CalculationType.SIP -> loadSipInterstitialAd(activity)
                CalculationType.SWP -> loadSwpInterstitialAd(activity)
                CalculationType.GOAL -> loadGoalInterstitialAd(activity)
            }
        }
    }
    
    private fun incrementCalculationCount(): Int {
        val currentCount = getCalculationCount()
        val newCount = currentCount + 1
        sharedPreferences?.edit()?.putInt(KEY_CALCULATION_COUNT, newCount)?.apply()
        return newCount
    }
    
    fun getCalculationCount(): Int {
        return sharedPreferences?.getInt(KEY_CALCULATION_COUNT, 0) ?: 0
    }
    
    fun resetCalculationCount() {
        sharedPreferences?.edit()?.putInt(KEY_CALCULATION_COUNT, 0)?.apply()
        Log.d(TAG, "Calculation count reset to 0")
    }
    
}