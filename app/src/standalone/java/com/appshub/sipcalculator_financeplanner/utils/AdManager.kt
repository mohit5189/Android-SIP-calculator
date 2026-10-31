package com.appshub.sipcalculator_financeplanner.utils

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdManager private constructor() {

    companion object {
        private const val TAG = "AdManager"
        // Prod Ad Unit IDs only
        private const val SIP_AD_UNIT_ID = "ca-app-pub-8809413893982569/5353999763"
        private const val SWP_AD_UNIT_ID = "ca-app-pub-8809413893982569/4344170423"
        private const val GOAL_AD_UNIT_ID = "ca-app-pub-8809413893982569/3326602222"
        private const val APP_OPEN_AD_UNIT_ID = "ca-app-pub-8809413893982569/5563493491"

        // SharedPreferences constants
        private const val PREFS_NAME = "ad_manager_prefs"
        private const val KEY_CALCULATION_COUNT = "calculation_count"
        private const val KEY_HAS_LAUNCHED = "has_launched"

        // Ad trigger counts
        private const val AD_TRIGGER_COUNT = 5 // show interstitial every 5 calculations

        // Calculation types
        enum class CalculationType {
            SIP, SWP, GOAL, SIMPLE_INTEREST, COMPOUND_INTEREST, EMI, RD, PPF, FD
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

    // App Open Ad variables
    private var appOpenAd: com.google.android.gms.ads.appopen.AppOpenAd? = null
    private var isAppOpenAdLoading = false
    private var isAppOpenAdShowing = false
    private var pendingShowAppOpenAd: Activity? = null

    fun initializeAds(context: Context) {
        // Initialize SharedPreferences
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Configure ads for Families Policy compliance
        val requestConfiguration = MobileAds.getRequestConfiguration()
            .toBuilder()
            .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
            .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)

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
        // Pre-load App Open Ad
        loadAppOpenAd(context)

        // Mark app as launched after first initialization
        if (!sharedPreferences!!.getBoolean(KEY_HAS_LAUNCHED, false)) {
            sharedPreferences!!.edit().putBoolean(KEY_HAS_LAUNCHED, true).apply()
        }
    }

    private fun isDebugBuild(context: Context): Boolean {
        return (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }

    private fun getInterstitialAdUnitId(type: CalculationType): String {
        return when (type) {
            CalculationType.SIP, CalculationType.SIMPLE_INTEREST, CalculationType.RD -> SIP_AD_UNIT_ID
            CalculationType.SWP, CalculationType.COMPOUND_INTEREST, CalculationType.PPF -> SWP_AD_UNIT_ID
            CalculationType.GOAL, CalculationType.EMI, CalculationType.FD -> GOAL_AD_UNIT_ID
        }
    }

    private fun getAppOpenAdUnitId(): String {
        return APP_OPEN_AD_UNIT_ID
    }

    private fun loadSipInterstitialAd(context: Context) {
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(context, getInterstitialAdUnitId(CalculationType.SIP), adRequest, object : InterstitialAdLoadCallback() {
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

        InterstitialAd.load(context, getInterstitialAdUnitId(CalculationType.SWP), adRequest, object : InterstitialAdLoadCallback() {
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

        InterstitialAd.load(context, getInterstitialAdUnitId(CalculationType.GOAL), adRequest, object : InterstitialAdLoadCallback() {
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
                    CalculationType.SIMPLE_INTEREST -> {
                        sipInterstitialAd = null
                        loadSipInterstitialAd(context)
                    }
                    CalculationType.COMPOUND_INTEREST -> {
                        swpInterstitialAd = null
                        loadSwpInterstitialAd(context)
                    }
                    CalculationType.EMI -> {
                        goalInterstitialAd = null
                        loadGoalInterstitialAd(context)
                    }
                    CalculationType.RD -> {
                        sipInterstitialAd = null
                        loadSipInterstitialAd(context)
                    }
                    CalculationType.PPF -> {
                        swpInterstitialAd = null
                        loadSwpInterstitialAd(context)
                    }
                    CalculationType.FD -> {
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
                    CalculationType.SIMPLE_INTEREST -> {
                        sipInterstitialAd = null
                        loadSipInterstitialAd(context)
                    }
                    CalculationType.COMPOUND_INTEREST -> {
                        swpInterstitialAd = null
                        loadSwpInterstitialAd(context)
                    }
                    CalculationType.EMI -> {
                        goalInterstitialAd = null
                        loadGoalInterstitialAd(context)
                    }
                    CalculationType.RD -> {
                        sipInterstitialAd = null
                        loadSipInterstitialAd(context)
                    }
                    CalculationType.PPF -> {
                        swpInterstitialAd = null
                        loadSwpInterstitialAd(context)
                    }
                    CalculationType.FD -> {
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

        // Show ad every 5 calculations
        if (newCount % AD_TRIGGER_COUNT == 0) {
            Log.d(TAG, "Triggering ad after $newCount calculations")
            showInterstitialAd(activity, calculationType)
        }
    }

    private fun showInterstitialAd(activity: Activity, calculationType: CalculationType) {
        // For new calculators, use existing ad units (rotate between them)
        val interstitialAd = when (calculationType) {
            CalculationType.SIP -> sipInterstitialAd
            CalculationType.SWP -> swpInterstitialAd
            CalculationType.GOAL -> goalInterstitialAd
            CalculationType.SIMPLE_INTEREST -> sipInterstitialAd // Use SIP ad unit
            CalculationType.COMPOUND_INTEREST -> swpInterstitialAd // Use SWP ad unit
            CalculationType.EMI -> goalInterstitialAd // Use GOAL ad unit
            CalculationType.RD -> sipInterstitialAd // Use SIP ad unit
            CalculationType.PPF -> swpInterstitialAd // Use SWP ad unit
            CalculationType.FD -> goalInterstitialAd // Use GOAL ad unit
        }

        if (interstitialAd != null) {
            Log.d(TAG, "Showing $calculationType interstitial ad")
            interstitialAd.show(activity)
        } else {
            Log.w(TAG, "$calculationType interstitial ad not ready. Reloading...")
            // Try to reload the appropriate ad
            when (calculationType) {
                CalculationType.SIP -> loadSipInterstitialAd(activity)
                CalculationType.SWP -> loadSwpInterstitialAd(activity)
                CalculationType.GOAL -> loadGoalInterstitialAd(activity)
                CalculationType.SIMPLE_INTEREST -> loadSipInterstitialAd(activity)
                CalculationType.COMPOUND_INTEREST -> loadSwpInterstitialAd(activity)
                CalculationType.EMI -> loadGoalInterstitialAd(activity)
                CalculationType.RD -> loadSipInterstitialAd(activity)
                CalculationType.PPF -> loadSwpInterstitialAd(activity)
                CalculationType.FD -> loadGoalInterstitialAd(activity)
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

    private fun loadAppOpenAd(context: Context) {
        if (isAppOpenAdLoading || isAppOpenAdShowing) return
        isAppOpenAdLoading = true
        val adRequest = AdRequest.Builder().build()
        com.google.android.gms.ads.appopen.AppOpenAd.load(
            context,
            getAppOpenAdUnitId(),
            adRequest,
            com.google.android.gms.ads.appopen.AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: com.google.android.gms.ads.appopen.AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenAdLoading = false
                    // If there was a pending show request, show the ad now
                    pendingShowAppOpenAd?.let {
                        showAppOpenAdIfAvailable(it)
                        pendingShowAppOpenAd = null
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenAdLoading = false
                }
            }
        )
    }

    fun showAppOpenAdIfAvailable(activity: Activity) {
        // Avoid showing ad on very first launch
        if (sharedPreferences?.getBoolean(KEY_HAS_LAUNCHED, false) == false) return
        if (isAppOpenAdShowing) return
        if (appOpenAd != null) {
            appOpenAd?.let { ad ->
                isAppOpenAdShowing = true
                ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        isAppOpenAdShowing = false
                        appOpenAd = null
                        loadAppOpenAd(activity)
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        isAppOpenAdShowing = false
                        appOpenAd = null
                        loadAppOpenAd(activity)
                    }

                    override fun onAdShowedFullScreenContent() {}
                }
                ad.show(activity)
            }
        } else {
            // No ad loaded yet, set pending show request
            pendingShowAppOpenAd = activity
            loadAppOpenAd(activity)
        }
    }
}