package com.example.ui.screens

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
import com.example.data.LingoKeyPreferences

data class PricingPlan(
    val id: String,
    val title: String,
    val price: String,
    val period: String,
    val tag: String? = null,
    val description: String
)

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

    var selectedPlanId by remember { mutableStateOf("annual") }
    var isProcessingPurchase by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val bgColor = if (isDarkMode) Color(0xFF0F1016) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)

    val plans = listOf(
        PricingPlan(
            id = "annual",
            title = "Annual VIP Pass",
            price = "₹499",
            period = "/ year",
            tag = "BEST VALUE • SAVE 60%",
            description = "Only ₹41/month, billed annually. 7-day free trial."
        ),
        PricingPlan(
            id = "lifetime",
            title = "Lifetime Ultra",
            price = "₹999",
            period = "one-time",
            tag = "NO RECURRING FEES",
            description = "Pay once, enjoy forever on all future updates."
        ),
        PricingPlan(
            id = "monthly",
            title = "Monthly Pass",
            price = "₹99",
            period = "/ month",
            tag = null,
            description = "Flexible monthly subscription. Cancel anytime."
        )
    )

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
                            "VIP Membership",
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 18.sp
                        )
                        if (isPremiumUser) {
                            Surface(
                                color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B))
                            ) {
                                Text(
                                    "👑 ACTIVE",
                                    color = Color(0xFFF59E0B),
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Header Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF047857), Color(0xFF10B981), Color(0xFF064E3B))
                        )
                    )
                    .padding(22.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFEF08A),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        if (isPremiumUser) "You are a VIP PRO Member" else "Upgrade to Global Keyboard PRO",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        if (isPremiumUser) "All VIP themes, unlimited AI and neural voices are unlocked."
                        else "Unlock all 15+ VIP Themes, unlimited AI tone rewriting, and HD Namaste neural voice outputs.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )

                    if (isPremiumUser) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text("Full Lifetime Entitlement Active", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Status Card for Free vs PRO
            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Current Plan Status", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isPremiumUser) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFF64748B).copy(alpha = 0.15f)
                        ) {
                            Text(
                                if (isPremiumUser) "PRO ACTIVE" else "FREE TIER",
                                color = if (isPremiumUser) Color(0xFF10B981) else textMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (!isPremiumUser) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Free AI Credits Used", fontSize = 13.sp, color = textMuted)
                            Text("$aiUsageCount / 10 used", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                        }
                        LinearProgressIndicator(
                            progress = { (aiUsageCount.toFloat() / 10f).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF10B981),
                            trackColor = if (isDarkMode) Color(0xFF2C2C38) else Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            // Comparison / What's Included
            Text(
                "Exclusive PRO Perks",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = textColor
            )

            val features = listOf(
                Pair("All 15+ VIP Luxury & Trend Themes", "Pure AMOLED, Carbon Gold, Glassmorphism, Skeuomorphic 3D"),
                Pair("Unlimited Gemini AI Smart Rewrites", "No daily rate limits or tone restrictions"),
                Pair("Studio HD Namaste Neural Voice", "Polite, soothing localized Indian voice guidance & speech"),
                Pair("Custom Keyboard Theme Designer", "Create infinite gradient backgrounds, alpha & key shapes"),
                Pair("100% Ad-Free Experience", "Zero interruptions, maximum typing speed & privacy")
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = cardColor),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    for ((title, desc) in features) {
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                                Text(desc, fontSize = 11.5.sp, color = textMuted)
                            }
                        }
                    }
                }
            }

            // Subscription Tiers (if not already subscribed)
            if (!isPremiumUser) {
                Text(
                    "Choose Your Access Plan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textColor
                )

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    plans.forEach { plan ->
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
                                color = if (isSelected) Color(0xFF10B981) else borderColor
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPlanId = plan.id }
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
                                                selectedColor = Color(0xFF10B981),
                                                unselectedColor = textMuted
                                            )
                                        )
                                        Column {
                                            Text(plan.title, fontWeight = FontWeight.Bold, fontSize = 14.5.sp, color = textColor)
                                            Text(plan.description, fontSize = 11.5.sp, color = textMuted)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(plan.price, fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, color = textColor)
                                        Text(plan.period, fontSize = 10.5.sp, color = textMuted)
                                    }
                                }

                                if (plan.tag != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
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

                // Action Button: Subscribe / Activate
                Button(
                    onClick = {
                        isProcessingPurchase = true
                        // Simulate in-app purchase flow
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            isProcessingPurchase = false
                            preferences.activatePro()
                            showSuccessDialog = true
                        }, 800)
                    },
                    enabled = !isProcessingPurchase,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isProcessingPurchase) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                    } else {
                        val selected = plans.firstOrNull { it.id == selectedPlanId } ?: plans[0]
                        Text(
                            "Upgrade Now • ${selected.price} ${selected.period}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            } else {
                // If user is already PRO: Provide quick controls
                Button(
                    onClick = {
                        Toast.makeText(context, "VIP Membership is fully active on this device!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VIP Benefits Active & Unlocked", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                }
            }

            // Restore Purchases & Tester Mode Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(
                    onClick = {
                        val restored = preferences.restorePurchases()
                        if (restored) {
                            Toast.makeText(context, "Purchase restored successfully! All features unlocked.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "No prior purchase found for this Google account.", Toast.LENGTH_SHORT).show()
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
                        color = Color(0xFF10B981),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Text(
                "Recurring billing, cancel anytime in Google Play Store > Subscriptions. Terms of Service & Privacy Policy apply.",
                fontSize = 10.5.sp,
                color = textMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )

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
                        .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(32.dp))
                }
            },
            title = {
                Text("Welcome to Global Keyboard PRO!", color = textColor, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            },
            text = {
                Text(
                    "Your VIP membership is now activated. All luxury themes, unlimited AI rewrite tones, and HD Namaste voices are fully unlocked!",
                    color = textMuted,
                    fontSize = 13.5.sp,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = { showSuccessDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Explore VIP Features", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
