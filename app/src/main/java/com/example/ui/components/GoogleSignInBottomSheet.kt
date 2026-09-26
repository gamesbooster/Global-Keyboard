package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.auth.GoogleAccountManager
import com.example.auth.GoogleAuthResult
import com.example.data.LingoKeyPreferences
import kotlinx.coroutines.launch

enum class SignInTriggerReason {
    SPIN_AND_WIN,
    UPGRADE_PRO,
    THEME_PURCHASE,
    LOW_CREDITS,
    GENERAL
}

private fun Context.findActivity(): Activity? {
    var ctx = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

/**
 * Production-ready Material 3 Bottom Sheet for real Google Sign-In with Firebase Authentication.
 * Uses official Google Play Services Auth to display the authentic system Google Account Chooser dialog.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoogleSignInBottomSheet(
    preferences: LingoKeyPreferences,
    reason: SignInTriggerReason = SignInTriggerReason.GENERAL,
    onDismiss: () -> Unit,
    onSignInSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isDarkMode by preferences.isDarkMode.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val authManager = remember { GoogleAccountManager.getInstance(context, preferences) }

    // Official Google Play Services Account Chooser Launcher
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { activityResult ->
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val result = authManager.handleSignInResult(activityResult.data)
                when (result) {
                    is GoogleAuthResult.Success -> {
                        if (result.isNewUser) {
                            Toast.makeText(
                                context,
                                "🎉 Welcome! 200 Free Credits added to your account.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Signed in as ${result.user.displayName}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        onSignInSuccess()
                        onDismiss()
                    }
                    is GoogleAuthResult.Error -> {
                        errorMessage = result.message
                    }
                    is GoogleAuthResult.Cancelled -> {
                        // User dismissed the Google account chooser without selecting
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message ?: "Authentication failed. Please try again."
            } finally {
                isLoading = false
            }
        }
    }

    val bgCard = if (isDarkMode) Color(0xFF1E202E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = bgCard,
        tonalElevation = 8.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Google Brand Icon Header
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(
                        if (isDarkMode) Color(0xFF2E3147) else Color(0xFFF1F5F9)
                    ),
                contentAlignment = Alignment.Center
            ) {
                GoogleLogoIcon(modifier = Modifier.size(32.dp))
            }

            // Headline & Reason
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = when (reason) {
                        SignInTriggerReason.SPIN_AND_WIN -> "Sign In to Spin & Win"
                        SignInTriggerReason.UPGRADE_PRO -> "Sign In for VIP Pro Upgrade"
                        SignInTriggerReason.THEME_PURCHASE -> "Save Theme Purchases"
                        SignInTriggerReason.LOW_CREDITS -> "Claim 200 Free Welcome Coins"
                        SignInTriggerReason.GENERAL -> "Sign In with Google"
                    },
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = when (reason) {
                        SignInTriggerReason.SPIN_AND_WIN ->
                            "Sign in with Google to protect your coins and spin the wheel. Plus, get 200 Free Welcome Coins on your first sign-in!"
                        SignInTriggerReason.UPGRADE_PRO ->
                            "Sign in with Google to protect your VIP Pro status, secure your purchases, and link all premium themes to your verified account."
                        SignInTriggerReason.THEME_PURCHASE ->
                            "Keep your purchased and custom themes synced across all your Android devices."
                        SignInTriggerReason.LOW_CREDITS ->
                            "Link your Google account now to receive an immediate +200 free coin bonus and unlock daily rewards!"
                        SignInTriggerReason.GENERAL ->
                            "Save your credits, VIP status, and custom themes securely in the cloud."
                    },
                    fontSize = 13.sp,
                    color = textMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }

            // Value Proposition Highlights
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDarkMode) Color(0xFF141622) else Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF2E334D) else Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SignInBenefitRow(
                        icon = Icons.Default.MonetizationOn,
                        title = "200 Free Welcome Bonus",
                        desc = "Instantly credited to your vault on your first sign-in",
                        isDarkMode = isDarkMode
                    )
                    SignInBenefitRow(
                        icon = Icons.Default.CloudSync,
                        title = "Real-time Cloud Sync",
                        desc = "Credits and custom keyboards stay safe across phone resets",
                        isDarkMode = isDarkMode
                    )
                    SignInBenefitRow(
                        icon = Icons.Default.VerifiedUser,
                        title = "Official Google & Firebase Security",
                        desc = "Directly authenticated with Google Identity & Firebase Auth",
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Error Message Banner (if any)
            if (errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = "Error",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFB91C1C),
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Real Google Sign-In Button (Triggers Android's Native Google Account Picker)
            Button(
                onClick = {
                    val activity = context.findActivity()
                    if (activity == null) {
                        Toast.makeText(context, "Activity not available for Google Sign-In", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    errorMessage = null
                    isLoading = true
                    try {
                        val client = authManager.getGoogleSignInClient(activity)
                        // Sign out first to ensure the native Google account list always appears for account selection
                        client.signOut().addOnCompleteListener {
                            val signInIntent = client.signInIntent
                            googleSignInLauncher.launch(signInIntent)
                        }
                    } catch (e: Exception) {
                        isLoading = false
                        errorMessage = e.message ?: "Failed to open Google account selector."
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isDarkMode) Color(0xFFFFFFFF) else Color(0xFF0F172A),
                    contentColor = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = if (isDarkMode) Color(0xFF0F172A) else Color(0xFFFFFFFF)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Connecting with Google...",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        GoogleLogoIcon(modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Sign In with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // Close Button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Cancel",
                    color = textMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun SignInBenefitRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    desc: String,
    isDarkMode: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isDarkMode) Color(0xFF222638) else Color(0xFFEEF2F6)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
            )
            Text(
                text = desc,
                fontSize = 11.sp,
                color = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B),
                lineHeight = 14.sp
            )
        }
    }
}

/**
 * High-fidelity vector Canvas rendering of the multicolored Google "G" brand logo.
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = (minOf(w, h) / 2f) * 0.92f

        // Blue right segment
        val bluePath = Path().apply {
            moveTo(cx, cy)
            lineTo(cx + r, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r),
                startAngleDegrees = 0f,
                sweepAngleDegrees = 45f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(bluePath, color = Color(0xFF4285F4))

        // Red top segment
        val redPath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r),
                startAngleDegrees = 200f,
                sweepAngleDegrees = 115f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(redPath, color = Color(0xFFEA4335))

        // Yellow left segment
        val yellowPath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r),
                startAngleDegrees = 140f,
                sweepAngleDegrees = 65f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(yellowPath, color = Color(0xFFFBBC05))

        // Green bottom segment
        val greenPath = Path().apply {
            moveTo(cx, cy)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(cx - r, cy - r, cx + r, cy + r),
                startAngleDegrees = 45f,
                sweepAngleDegrees = 100f,
                forceMoveTo = false
            )
            close()
        }
        drawPath(greenPath, color = Color(0xFF34A853))

        // White inner cutout
        drawCircle(
            color = Color.White,
            radius = r * 0.58f,
            center = Offset(cx, cy)
        )

        // Center Blue crossbar
        val barWidth = r * 0.85f
        val barHeight = r * 0.35f
        drawRect(
            color = Color(0xFF4285F4),
            topLeft = Offset(cx, cy - barHeight / 2f),
            size = androidx.compose.ui.geometry.Size(barWidth, barHeight)
        )
    }
}
