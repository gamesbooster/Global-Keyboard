package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.billing.PlayBillingManager
import com.example.data.CreditsSecurityManager
import com.example.data.LingoKeyPreferences

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Production-ready Top-Up Sheet for purchasing Google Play In-App Consumable Credit Packs.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditTopUpSheet(
    preferences: LingoKeyPreferences,
    onDismiss: () -> Unit,
    onOpenFreeSpin: () -> Unit = {}
) {
    val context = LocalContext.current
    val isDarkMode by preferences.isDarkMode.collectAsState()
    val credits by preferences.aiCredits.collectAsState()
    val isPremium by preferences.isPremiumUser.collectAsState()
    val billingManager = remember { PlayBillingManager.getInstance(context) }
    val isPurchasing by billingManager.isPurchasing.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val bgCard = if (isDarkMode) Color(0xFF1E202E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgCard,
        tonalElevation = 10.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp, top = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Pill: Current Balance
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFFEF3C7),
                border = BorderStroke(1.2.dp, Color(0xFFF59E0B)),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🪙", fontSize = 16.sp)
                    Text(
                        text = if (isPremium) "VIP PRO (Unlimited)" else "$credits Coins Available",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
            }

            // Title & Security Tag
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Top Up Credit Vault",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Hardware Encrypted (AES-256-GCM + Google Play IAP)",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Credit Packages List
            val packs = listOf(
                CreditPackItem(
                    id = PlayBillingManager.INAPP_CREDITS_50,
                    amount = 50,
                    bonusText = null,
                    defaultPrice = "$0.99",
                    isPopular = false,
                    badgeColor = Color(0xFF64748B)
                ),
                CreditPackItem(
                    id = PlayBillingManager.INAPP_CREDITS_150,
                    amount = 150,
                    bonusText = "+25 FREE",
                    defaultPrice = "$2.49",
                    isPopular = true,
                    badgeColor = Color(0xFF10B981)
                ),
                CreditPackItem(
                    id = PlayBillingManager.INAPP_CREDITS_500,
                    amount = 500,
                    bonusText = "+100 FREE",
                    defaultPrice = "$6.99",
                    isPopular = false,
                    badgeColor = Color(0xFFF59E0B)
                )
            )

            for (pack in packs) {
                val formattedPrice = billingManager.getFormattedPrice(pack.id, pack.defaultPrice)
                CreditPackRow(
                    pack = pack,
                    formattedPrice = formattedPrice,
                    isDarkMode = isDarkMode,
                    isLoading = isPurchasing,
                    onClick = {
                        val activity = context.findActivity()
                        if (activity != null) {
                            billingManager.launchCreditsPurchase(
                                activity = activity,
                                productId = pack.id,
                                creditsAmount = pack.amount,
                                onSuccess = { added ->
                                    Toast.makeText(context, "🎉 Added +$added Coins to your vault!", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        } else {
                            // In IME soft keyboard: launch MainActivity with topup flag
                            val intent = Intent(context, MainActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                putExtra("open_credits_topup", true)
                                putExtra("target_product_id", pack.id)
                                putExtra("target_amount", pack.amount)
                            }
                            context.startActivity(intent)
                            onDismiss()
                        }
                    }
                )
            }

            // Free Spin Option
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) Color(0xFF141624) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2B3048) else Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onDismiss()
                        onOpenFreeSpin()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("🎡", fontSize = 20.sp)
                        Column {
                            Text(
                                "Daily Lucky Wheel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = textColor
                            )
                            Text(
                                "Win up to 50 free coins every day",
                                fontSize = 11.sp,
                                color = textMuted
                            )
                        }
                    }
                    Text(
                        "Spin Free →",
                        color = Color(0xFF3B82F6),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }

            // Cancel Button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close", color = textMuted, fontSize = 13.sp)
            }
        }
    }
}

private data class CreditPackItem(
    val id: String,
    val amount: Int,
    val bonusText: String?,
    val defaultPrice: String,
    val isPopular: Boolean,
    val badgeColor: Color
)

@Composable
private fun CreditPackRow(
    pack: CreditPackItem,
    formattedPrice: String,
    isDarkMode: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (pack.isPopular) Color(0xFF10B981) else if (isDarkMode) Color(0xFF2E334D) else Color(0xFFE2E8F0)
    val cardBg = if (pack.isPopular) {
        if (isDarkMode) Color(0xFF0F291E) else Color(0xFFECFDF5)
    } else {
        if (isDarkMode) Color(0xFF161826) else Color(0xFFF8FAFC)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        border = BorderStroke(if (pack.isPopular) 1.5.dp else 1.dp, borderColor),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !isLoading) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFBBF24), Color(0xFFD97706))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🪙", fontSize = 18.sp)
                }
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${pack.amount} Coins",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        if (pack.bonusText != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = pack.badgeColor.copy(alpha = 0.18f),
                                border = BorderStroke(0.8.dp, pack.badgeColor)
                            ) {
                                Text(
                                    text = pack.bonusText,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = pack.badgeColor,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Instant credit to secure vault",
                        fontSize = 11.sp,
                        color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
                    )
                }
            }

            Button(
                onClick = onClick,
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pack.isPopular) Color(0xFF10B981) else if (isDarkMode) Color(0xFF334155) else Color(0xFF0F172A),
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = formattedPrice,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Lightweight, non-intrusive alert when credits balance is depleted.
 */
@Composable
fun OutOfCreditsDialog(
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onOpenTopUp: () -> Unit,
    onOpenFreeSpin: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("⚡", fontSize = 22.sp)
                Text(
                    "Out of Coins",
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "You need 1 coin to perform this action. Top up instantly or spin the Lucky Wheel for free daily coins!",
                    fontSize = 13.5.sp,
                    color = if (isDarkMode) Color(0xFFCBD5E1) else Color(0xFF475569),
                    lineHeight = 19.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    onOpenTopUp()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF10B981),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Top Up Coins", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onOpenFreeSpin()
                }
            ) {
                Text("Free Spin 🎡", color = Color(0xFF3B82F6), fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = if (isDarkMode) Color(0xFF1E202E) else Color(0xFFFFFFFF),
        shape = RoundedCornerShape(20.dp)
    )
}
