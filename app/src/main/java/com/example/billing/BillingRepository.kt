package com.example.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.ProductDetails
import kotlinx.coroutines.flow.StateFlow

/**
 * Singleton Repository interface for Google Play Billing (IAP v7+).
 * Exposes observable StateFlows and purchase initiation methods.
 */
class BillingRepository private constructor(private val context: Context) {

    private val billingManager = PlayBillingManager.getInstance(context)

    val isConnected: StateFlow<Boolean> = billingManager.isServiceConnected
    val isPurchasing: StateFlow<Boolean> = billingManager.isPurchasing
    val inAppProducts: StateFlow<Map<String, ProductDetails>> = billingManager.inAppProductDetails
    val subscriptionProducts: StateFlow<Map<String, ProductDetails>> = billingManager.subscriptionProductDetails
    val billingMessage: StateFlow<String?> = billingManager.billingMessage

    fun startConnection() {
        billingManager.startConnection()
    }

    fun queryProducts() {
        billingManager.queryProducts()
    }

    fun buyCreditPack(
        activity: Activity,
        productId: String,
        amount: Int,
        onSuccess: (Int) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        billingManager.launchCreditsPurchase(activity, productId, amount, onSuccess, onError)
    }

    fun buySubscription(
        activity: Activity,
        productId: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        billingManager.launchSubscriptionPurchase(activity, productId, onSuccess, onError)
    }

    fun restorePurchases(onComplete: ((Boolean, String) -> Unit)? = null) {
        billingManager.queryActivePurchases(onComplete)
    }

    fun clearMessage() {
        billingManager.clearBillingMessage()
    }

    fun getFormattedPrice(productId: String, fallback: String): String {
        return billingManager.getFormattedPrice(productId, fallback)
    }

    companion object {
        @Volatile
        private var instance: BillingRepository? = null

        fun getInstance(context: Context): BillingRepository {
            return instance ?: synchronized(this) {
                instance ?: BillingRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
