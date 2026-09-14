package com.example.ui.components

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ads.AdMobConfig
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

/**
 * Real Google AdMob Banner / Medium Rectangle Ad Composable.
 * Loads live ads in real-time as the user scrolls.
 * Replace AdMobConfig.DASHBOARD_BANNER_AD_UNIT_ID with your own Ad Unit ID in production.
 */
@Composable
fun RealAdMobBanner(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    adUnitId: String = AdMobConfig.DASHBOARD_BANNER_AD_UNIT_ID
) {
    val context = LocalContext.current
    var isAdLoaded by remember { mutableStateOf(false) }
    var adErrorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AdMobConfig.initialize(context)
    }

    NeumorphicCard(
        modifier = modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        cornerRadius = 18.dp,
        elevation = 7.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ad / Sponsored Label Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isDarkMode) Color(0xFF222433) else Color(0xFFF1F5F9),
                        border = BorderStroke(0.5.dp, if (isDarkMode) Color(0xFF33364D) else Color(0xFFCBD5E1))
                    ) {
                        Text(
                            "Ad",
                            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        "Google AdMob Test Unit",
                        color = if (isDarkMode) Color(0xFF64748B) else Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                if (isAdLoaded) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Text(
                            "LIVE",
                            color = Color(0xFF10B981),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Real Google AdView Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 280.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDarkMode) Color(0xFF0F1017) else Color(0xFFF8FAFC)),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    modifier = Modifier.wrapContentSize(),
                    factory = { ctx ->
                        AdView(ctx).apply {
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT
                            )
                            setAdSize(AdSize.MEDIUM_RECTANGLE)
                            this.adUnitId = adUnitId
                            adListener = object : AdListener() {
                                override fun onAdLoaded() {
                                    isAdLoaded = true
                                    adErrorMessage = null
                                }

                                override fun onAdFailedToLoad(error: LoadAdError) {
                                    isAdLoaded = false
                                    adErrorMessage = error.message
                                }
                            }
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )

                // Fallback / Loading notice when offline or loading test creative
                if (!isAdLoaded && adErrorMessage != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "AdMob Unit Ready",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            "Unit: $adUnitId",
                            fontSize = 10.5.sp,
                            color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "(Auto-refreshes when online: ${adErrorMessage})",
                            fontSize = 10.sp,
                            color = Color(0xFFF59E0B),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
