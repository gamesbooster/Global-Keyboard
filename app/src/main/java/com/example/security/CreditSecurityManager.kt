package com.example.security

import android.content.Context
import com.example.data.CreditsSecurityManager
import kotlinx.coroutines.flow.StateFlow

/**
 * Dedicated Security Package Facade for Credit Vault Operations.
 * Protects credit balance using Hardware-Backed Android KeyStore (AES-256-GCM),
 * HMAC-SHA256 signature verification, and RAM XOR-memory obfuscation.
 */
class CreditSecurityManager private constructor(private val context: Context) {

    private val delegate = CreditsSecurityManager.getInstance(context)

    val creditsBalance: StateFlow<Int> = delegate.creditsBalance
    val isTampered: StateFlow<Boolean> = delegate.isTampered

    fun hasCredits(cost: Int = 1, isPremium: Boolean): Boolean {
        return delegate.hasCredits(cost, isPremium)
    }

    fun deductCredits(cost: Int = 1, isPremium: Boolean): Boolean {
        return delegate.deductCredits(cost, isPremium)
    }

    fun addCredits(amount: Int): Boolean {
        return delegate.addCredits(amount)
    }

    fun canSpin(): Boolean = delegate.canSpin()

    fun consumeSpin(): Boolean = delegate.consumeSpin()

    fun recordSpinResult(creditsWon: Int): Boolean = delegate.recordSpinResult(creditsWon)

    companion object {
        @Volatile
        private var instance: CreditSecurityManager? = null

        fun getInstance(context: Context): CreditSecurityManager {
            return instance ?: synchronized(this) {
                instance ?: CreditSecurityManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
