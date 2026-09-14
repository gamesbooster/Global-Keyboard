package com.example.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds

/**
 * AdMob Configuration & Ad Unit IDs.
 *
 * NOTE FOR PRODUCTION:
 * Replace the sample test IDs below with your real Google AdMob Ad Unit IDs
 * from your AdMob console (https://admob.google.com).
 */
object AdMobConfig {
    // Official Google AdMob Sample App ID (matches AndroidManifest.xml)
    const val SAMPLE_APP_ID = "ca-app-pub-3940256099942544~3347511713"

    // Official Google AdMob Test Ad Unit IDs:
    // 1. In-Feed Banner / Medium Rectangle Ad (Dashboard scroll feed)
    // Replace with your real AdMob Banner / MREC Ad Unit ID
    const val DASHBOARD_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"

    // 2. Rewarded Video Ad (Spin & Win / Credits)
    // Replace with your real AdMob Rewarded Ad Unit ID
    const val REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"

    // 3. Interstitial Ad (Themes & Transitions)
    // Replace with your real AdMob Interstitial Ad Unit ID
    const val INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"

    private var isInitialized = false

    /**
     * Initialize the Mobile Ads SDK on app startup.
     */
    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                MobileAds.initialize(context) {
                    isInitialized = true
                }
            } catch (_: Exception) {
                // Ignore initialization failures in test environments
            }
        }
    }
}
