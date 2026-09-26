package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.billing.PlayBillingManager
import com.example.data.CreditsSecurityManager
import com.example.data.LingoKeyPreferences
import com.example.ui.components.GoogleSignInBottomSheet
import com.example.ui.components.SignInTriggerReason

data class SubscriptionPricingPlan(
    val id: String,
    val title: String,
    val price: String,
    val period: String,
    val tag: String? = null,
    val description: String,
    val trialText: String? = null
)

data class CreditPackItem(
    val id: String,
    val amount: Int,
    val price: String,
    val bonus: String? = null
)

private fun Context.findActivity(): Activity? {
    var currentContext = this
    while (currentContext is android.content.ContextWrapper) {
        if (currentContext is Activity) return currentContext
        currentContext = currentContext.baseContext
    }
    return null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()
    val aiUsageCount by preferences.aiUsageCount.collectAsState()
    val isSignedIn by preferences.isSignedIn.collectAsState()

    val creditsSecurityManager = remember { CreditsSecurityManager.getInstance(context) }
    val aiCredits by creditsSecurityManager.aiCredits.collectAsState()

    val playBillingManager = remember { PlayBillingManager.getInstance(context) }
    val isPurchasing by playBillingManager.isPurchasing.collectAsState()
    val subProductDetails by playBillingManager.subscriptionProductDetails.collectAsState()
    val inAppProductDetails by playBillingManager.inAppProductDetails.collectAsState()
    val billingMessage by playBillingManager.billingMessage.collectAsState()

    var selectedPlanId by remember { mutableStateOf(PlayBillingManager.SUB_VIP_ANNUAL) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showSignInSheet by remember { mutableStateOf(false) }
    var purchaseSuccessMessage by remember { mutableStateOf("") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: VIP Subscriptions, 1: Credit Packs

    val bgColor = if (isDarkMode) Color(0xFF0F1016) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val emeraldAccent = Color(0xFF10B981)
    val goldColor = Color(0xFFF59E0B)

    // Listen to billing messages
    LaunchedEffect(billingMessage) {
        billingMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            playBillingManager.clearBillingMessage()
        }
    }

    // Dynamic price formatting from Google Play Billing if available, else default pricing
    val annualDetails = subProductDetails[PlayBillingManager.SUB_VIP_ANNUAL]
    val monthlyDetails = subProductDetails[PlayBillingManager.SUB_VIP_MONTHLY]
    val weeklyDetails = subProductDetails[PlayBillingManager.SUB_VIP_WEEKLY]

    val annualPrice = annualDetails?.subscriptionOfferDetails?.firstOrNull()
        ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "$29.99"
    val monthlyPrice = monthlyDetails?.subscriptionOfferDetails?.firstOrNull()
        ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "$4.99"
    val weeklyPrice = weeklyDetails?.subscriptionOfferDetails?.firstOrNull()
        ?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice ?: "$1.99"

    val subscriptionPlans = remember(annualPrice, monthlyPrice, weeklyPrice) {
        listOf(
            SubscriptionPricingPlan(
                id = PlayBillingManager.SUB_VIP_ANNUAL,
                title = "Annual VIP Pass",
                price = annualPrice,
                period = "/ year",
                tag = "BEST VALUE • 7-DAY FREE TRIAL",
                description = "Try 7 days free! Billed annually. Cancel anytime in Google Play.",
                trialText = "7-Day Free Trial included"
            ),
            SubscriptionPricingPlan(
                id = PlayBillingManager.SUB_VIP_MONTHLY,
                title = "Monthly VIP Pass",
                price = monthlyPrice,
                period = "/ month",
                tag = "POPULAR CHOICE",
                description = "Flexible monthly billing. Renews automatically.",
                trialText = null
            ),
            SubscriptionPricingPlan(
                id = PlayBillingManager.SUB_VIP_WEEKLY,
                title = "Weekly VIP Pass",
                price = weeklyPrice,
                period = "/ week",
                tag = null,
                description = "Short-term access. Renews weekly.",
                trialText = null
            )
        )
    }

    // Consumable Credit Packs (Matching Product IDs)
    val pack500Details = inAppProductDetails[PlayBillingManager.INAPP_CREDITS_500]
    val pack1500Details = inAppProductDetails[PlayBillingManager.INAPP_CREDITS_1500]
    val pack5000Details = inAppProductDetails[PlayBillingManager.INAPP_CREDITS_5000]

    val pack500Price = pack500Details?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$0.99"
    val pack1500Price = pack1500Details?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$2.49"
    val pack5000Price = pack5000Details?.oneTimePurchaseOfferDetails?.formattedPrice ?: "$4.99"

    val creditPacks = remember(pack500Price, pack1500Price, pack5000Price) {
        listOf(
            CreditPackItem(
                id = PlayBillingManager.INAPP_CREDITS_500,
                amount = 500,
                price = pack500Price,
                bonus = null
            ),
            CreditPackItem(
                id = PlayBillingManager.INAPP_CREDITS_1500,
                amount = 1500,
                price = pack1500Price,
                bonus = "+20% BONUS"
            ),
            CreditPackItem(
                id = PlayBillingManager.INAPP_CREDITS_5000,
                amount = 5000,
                price = pack5000Price,
                bonus = "+40% MEGA VALUE"
            )
        )
    }

    fun startSubscriptionFlow(productId: String) {
        if (!isSignedIn) {
            selectedPlanId = productId
            showSignInSheet = true
            return
        }

        val activity = context.findActivity()
        if (activity == null) {
            Toast.makeText(context, "Activity context not available.", Toast.LENGTH_SHORT).show()
            return
        }

        playBillingManager.launchSubscriptionPurchase(
            activity = activity,
            productId = productId,
            onSuccess = {
                purchaseSuccessMessage = "Welcome to Global Keyboard VIP! All premium themes and unlimited AI features are unlocked."
                showSuccessDialog = true
            },
            onError = { err ->
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            }
        )
    }

    fun startCreditsPurchaseFlow(productId: String, amount: Int) {
        if (!isSignedIn) {
            showSignInSheet = true
            return
        }

        val activity = context.findActivity()
        if (activity == null) {
            Toast.makeText(context, "Activity context not available.", Toast.LENGTH_SHORT).show()
            return
        }

        playBillingManager.launchCreditsPurchase(
            activity = activity,
            productId = productId,
            creditsAmount = amount,
            onSuccess = { added ->
                purchaseSuccessMessage = "Successfully purchased $added AI Credits! Your new balance is ready to use."
                showSuccessDialog = true
            },
            onError = { err ->
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            }
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "VIP & In-App Store",
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 18.sp
                        )
                        if (isPremiumUser) {
                            Surface(
                                color = goldColor.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, goldColor)
                            ) {
                                Text(
                                    "👑 ACTIVE",
                                    color = goldColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                actions = {
                    // Current Balance Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) Color(0xFF1E222D) else Color(0xFFE2E8F0),
                        border = BorderStroke(1.dp, emeraldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("🪙", fontSize = 11.sp)
                            Text(
                                "$aiCredits",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = emeraldAccent
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Hero Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF047857), Color(0xFF10B981), Color(0xFF064E3B))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        if (isPremiumUser) "You are a VIP PRO Member" else "Upgrade to Global Keyboard PRO",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        if (isPremiumUser) "Ad-free experience, all 15+ VIP themes, unlimited AI rewrite tones and HD voices unlocked."
                        else "Remove all ads, unlock 15+ VIP luxury themes, get unlimited AI writing and neural voice synthesis.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )

                    if (isPremiumUser) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                Text("VIP Subscription Active • Ad-Free", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 2. Segmented Mode Selector: VIP Plans vs Credit Packs
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDarkMode) Color(0xFF161922) else Color(0xFFE2E8F0),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTab == 0) emeraldAccent else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 0 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 9.dp)) {
                            Text(
                                "👑 VIP Plans",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 0) Color.White else textMuted
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedTab == 1) goldColor else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = 1 }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 9.dp)) {
                            Text(
                                "🪙 Credit Packs",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (selectedTab == 1) Color.Black else textMuted
                            )
                        }
                    }
                }
            }

            // 3. Tab Content
            if (selectedTab == 0) {
                // ==============================
                // TAB 0: VIP SUBSCRIPTIONS
                // ==============================

                // VIP Benefits Checklist
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("VIP Benefits Included", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = textColor)

                        val benefits = listOf(
                            Pair("🚫 100% Ad-Free Experience", "No banners, popups, or rewarded video requirements anywhere"),
                            Pair("🎨 All 15+ VIP Luxury Themes", "Signature Dynamic, Cyber Glow, 3D Tactile, and Neumorphism"),
                            Pair("✍️ Unlimited AI Rewrite & Smart Reply", "Gemini-powered Professional, Casual, and Confident tones"),
                            Pair("🎙️ HD Neural Voice Dictation", "Natural high-fidelity voice inputs in Hindi, Marathi & English")
                        )

                        benefits.forEach { (title, desc) ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(emeraldAccent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = emeraldAccent, modifier = Modifier.size(14.dp))
                                }
                                Column {
                                    Text(title, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = textColor)
                                    Text(desc, fontSize = 11.sp, color = textMuted)
                                }
                            }
                        }
                    }
                }

                if (!isPremiumUser) {
                    Text(
                        "Choose Your VIP Plan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textColor
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        subscriptionPlans.forEach { plan ->
                            val isSelected = selectedPlanId == plan.id
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        if (isDarkMode) Color(0xFF1C2230) else Color(0xFFF0FDF4)
                                    } else cardColor
                                ),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) emeraldAccent else borderColor
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedPlanId = plan.id }
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedPlanId = plan.id },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = emeraldAccent,
                                                    unselectedColor = textMuted
                                                )
                                            )
                                            Column {
                                                Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                                                Text(plan.description, fontSize = 11.sp, color = textMuted)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(plan.price, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = textColor)
                                            Text(plan.period, fontSize = 10.5.sp, color = textMuted)
                                        }
                                    }

                                    if (plan.tag != null) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = emeraldAccent.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                plan.tag,
                                                color = Color(0xFF059669),
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Main Subscribe Button
                    val selectedPlan = subscriptionPlans.firstOrNull { it.id == selectedPlanId } ?: subscriptionPlans[0]
                    Button(
                        onClick = {
                            if (!isSignedIn) {
                                showSignInSheet = true
                            } else {
                                startSubscriptionFlow(selectedPlan.id)
                            }
                        },
                        enabled = !isPurchasing,
                        colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (isPurchasing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            val ctaLabel = if (selectedPlan.trialText != null) {
                                "Start 7-Day Free Trial • Then ${selectedPlan.price} ${selectedPlan.period}"
                            } else {
                                "Subscribe Now • ${selectedPlan.price} ${selectedPlan.period}"
                            }
                            Text(
                                ctaLabel,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Active VIP Controls
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, emeraldAccent.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = emeraldAccent)
                                Text("VIP Subscription Active", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                            }
                            Text(
                                "Your subscription is currently active with full Google Play protection. You can manage or cancel your subscription at any time via the Google Play Store.",
                                fontSize = 12.sp,
                                color = textMuted
                            )
                            Button(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("https://play.google.com/store/account/subscriptions")
                                        }
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        Toast.makeText(context, "Open Google Play Store to manage subscriptions.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = if (isDarkMode) Color(0xFF222736) else Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Manage Subscription in Google Play", color = textColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

            } else {
                // ==============================
                // TAB 1: CONSUMABLE CREDIT PACKS
                // ==============================
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "AI Credits Packs (Consumable)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                    Text(
                        "Credits are used for AI tone re-writing, smart grammar corrections, and instant multilingual translations. Repurchase whenever needed!",
                        fontSize = 12.sp,
                        color = textMuted
                    )

                    creditPacks.forEach { pack ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = cardColor),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, borderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(goldColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🪙", fontSize = 20.sp)
                                    }
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("${pack.amount} Credits", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = textColor)
                                            if (pack.bonus != null) {
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = goldColor.copy(alpha = 0.2f)
                                                ) {
                                                    Text(
                                                        pack.bonus,
                                                        color = Color(0xFFD97706),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text("${pack.amount / CreditsSecurityManager.COST_PER_AI_GENERATION} AI generations included", fontSize = 11.5.sp, color = textMuted)
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (!isSignedIn) {
                                            showSignInSheet = true
                                        } else {
                                            startCreditsPurchaseFlow(pack.id, pack.amount)
                                        }
                                    },
                                    enabled = !isPurchasing,
                                    colors = ButtonDefaults.buttonColors(containerColor = goldColor),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        pack.price,
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Restore Purchases & Tester Mode Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        playBillingManager.queryActivePurchases { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = textMuted, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Restore Purchases", color = textMuted, fontSize = 12.sp)
                }

                // Tester toggle to test free tier locking
                TextButton(
                    onClick = {
                        if (isPremiumUser) {
                            preferences.resetToFreeTier()
                            Toast.makeText(context, "Switched to Free Tier for testing locks!", Toast.LENGTH_SHORT).show()
                        } else {
                            preferences.activatePro()
                            Toast.makeText(context, "Switched to VIP PRO! All features unlocked.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text(
                        if (isPremiumUser) "Test as Free User" else "Test Instant Unlock",
                        color = emeraldAccent,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Google Play Store Mandatory Policy Transparency Notice
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isDarkMode) Color(0xFF141722) else Color(0xFFF1F5F9)
                ),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF202636) else Color(0xFFCBD5E1)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Google Play Subscription Policy & Terms:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = textColor
                    )
                    Text(
                        "• Payment will be charged to your Google Play account at confirmation of purchase.\n" +
                        "• Subscriptions automatically renew unless auto-renew is turned off at least 24 hours before the end of the current period.\n" +
                        "• Your account will be charged for renewal within 24 hours prior to the end of the current period.\n" +
                        "• You can manage or cancel your subscriptions anytime by going to your Account Settings on Google Play.\n" +
                        "• Any unused portion of a free trial period will be forfeited when purchasing a subscription.",
                        fontSize = 10.sp,
                        color = textMuted,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Success Purchase Celebration Dialog
    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false },
            containerColor = cardColor,
            icon = {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(emeraldAccent.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = emeraldAccent, modifier = Modifier.size(32.dp))
                }
            },
            title = {
                Text("Transaction Successful!", color = textColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    purchaseSuccessMessage.ifEmpty { "Your VIP entitlement and credits are now updated and synchronized with your account." },
                    color = textMuted,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = emeraldAccent),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showSignInSheet) {
        GoogleSignInBottomSheet(
            preferences = preferences,
            reason = SignInTriggerReason.UPGRADE_PRO,
            onDismiss = { showSignInSheet = false },
            onSignInSuccess = {
                showSignInSheet = false
                val selectedPlan = subscriptionPlans.firstOrNull { it.id == selectedPlanId } ?: subscriptionPlans[0]
                startSubscriptionFlow(selectedPlan.id)
            }
        )
    }
}
