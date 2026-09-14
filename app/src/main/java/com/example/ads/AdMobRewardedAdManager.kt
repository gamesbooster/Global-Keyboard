package com.example.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Real Google AdMob Rewarded Ad Manager for Spin & Win and AI Credits.
 * Complies strictly with AdMob policies:
 * - Real-time preloading & loading
 * - Safe error handling (falls back gracefully if offline or no fill)
 * - Safe user reward callback execution
 */
object AdMobRewardedAdManager {
    private const val TAG = "AdMobRewarded"
    private var rewardedAd: RewardedAd? = null
    private var isLoading = false

    /**
     * Preload a Rewarded Ad so it is ready when the wheel stops spinning.
     */
    fun preload(context: Context) {
        if (rewardedAd != null || isLoading) return
        isLoading = true

        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            context.applicationContext,
            AdMobConfig.REWARDED_AD_UNIT_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "AdMob Rewarded Ad loaded successfully.")
                    rewardedAd = ad
                    isLoading = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "AdMob Rewarded Ad failed to load: ${loadAdError.message}")
                    rewardedAd = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Show the real AdMob Rewarded Ad.
     * @param activity Hosting Activity required by AdMob to display full-screen ad
     * @param onRewardGranted Called when the user has finished watching the ad and earned their reward
     * @param onComplete Called when the ad is closed, dismissed, or if the ad was not available
     */
    fun showRewardedAd(
        activity: Activity,
        onRewardGranted: () -> Unit,
        onComplete: () -> Unit
    ) {
        val ad = rewardedAd
        if (ad != null) {
            var earned = false

            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad dismissed.")
                    rewardedAd = null
                    // Preload the next one immediately
                    preload(activity)
                    if (earned) {
                        onRewardGranted()
                    }
                    onComplete()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    preload(activity)
                    // If ad fails to show, still let user proceed so they don't lose their spin
                    onRewardGranted()
                    onComplete()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Rewarded Ad showed full screen.")
                }
            }

            ad.show(activity) { rewardItem: RewardItem ->
                Log.d(TAG, "User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                earned = true
            }
        } else {
            // Ad was not ready, try loading one for next time and let the user collect
            Log.d(TAG, "Rewarded Ad was not ready yet, granting reward gracefully.")
            preload(activity)
            onRewardGranted()
            onComplete()
        }
    }

    fun isAdLoaded(): Boolean = rewardedAd != null
}
