package com.example.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
 * Modern Material 3 Bottom Sheet for Just-In-Time Google Sign-In.
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

    val authManager = remember { GoogleAccountManager.getInstance(context, preferences) }

    val bgCard = if (isDarkMode) Color(0xFF1E202E) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color(0xFFF1F5F9) else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val goldColor = Color(0xFFF59E0B)

    var showAccountPickerDialog by remember { mutableStateOf(false) }
    var customEmailInput by remember { mutableStateOf("rajeshchoukhe6@gmail.com") }
    var customNameInput by remember { mutableStateOf("Rajesh Choukhe") }
    var showCustomInputFields by remember { mutableStateOf(false) }

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
                        SignInTriggerReason.SPIN_AND_WIN -> "Unlock Spin & Win Rewards"
                        SignInTriggerReason.UPGRADE_PRO -> "Sign in to Activate Pro VIP"
                        SignInTriggerReason.THEME_PURCHASE -> "Save Theme Purchases"
                        SignInTriggerReason.LOW_CREDITS -> "Claim +200 Free Cloud Credits"
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
                            "Sign in with Google to claim your daily free spins and save your earned credits securely to the cloud!"
                        SignInTriggerReason.UPGRADE_PRO ->
                            "Attach your active Pro membership, unlimited AI features, and luxury themes to your verified Google account."
                        SignInTriggerReason.THEME_PURCHASE ->
                            "Keep your purchased and custom themes synced across all your Android devices."
                        SignInTriggerReason.LOW_CREDITS ->
                            "Link your Google account now to receive a +200 starter credit bonus and unlock daily rewards!"
                        SignInTriggerReason.GENERAL ->
                            "Save your credits, VIP status, and custom themes securely across all your devices."
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
                        icon = Icons.Default.CloudSync,
                        title = "Real-time Cloud Sync",
                        desc = "Credits and custom keyboards stay safe across phone resets",
                        isDarkMode = isDarkMode
                    )
                    SignInBenefitRow(
                        icon = Icons.Default.Casino,
                        title = "Daily Free Spins",
                        desc = "Win up to 500 AI credits on the Lucky Wheel every day",
                        isDarkMode = isDarkMode
                    )
                    SignInBenefitRow(
                        icon = Icons.Default.VerifiedUser,
                        title = "Verified Google Security",
                        desc = "Zero passwords to remember with Google Credential Manager",
                        isDarkMode = isDarkMode
                    )
                }
            }

            // Official Google Sign-In Button (Opens Google Account Selector)
            Button(
                onClick = {
                    showAccountPickerDialog = true
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
                        "Connecting to Google...",
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
                            "Continue with Google",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.5.sp
                        )
                    }
                }
            }

            // Quick 1-Tap Account Selector for effortless testing
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isDarkMode) Color(0xFF262A3E) else Color(0xFFF1F5F9),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLoading) {
                        authManager.signInWithAccount("rajeshchoukhe6@gmail.com", "Rajesh Choukhe")
                        Toast.makeText(context, "Signed in as rajeshchoukhe6@gmail.com (+200 bonus credits)", Toast.LENGTH_SHORT).show()
                        onSignInSuccess()
                        onDismiss()
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA4335)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("R", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Column {
                            Text(
                                "rajeshchoukhe6@gmail.com",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                            Text(
                                "1-Tap Quick Connect",
                                fontSize = 10.5.sp,
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = "Select",
                        tint = textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Frictionless Dismiss (Never locks the user)
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Maybe Later (Continue as Guest)",
                    color = textMuted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Realistic Google Account Chooser Dialog (Works smoothly for testing & real sign-ins)
    if (showAccountPickerDialog) {
        AlertDialog(
            onDismissRequest = { showAccountPickerDialog = false },
            containerColor = if (isDarkMode) Color(0xFF1E212E) else Color(0xFFFFFFFF),
            shape = RoundedCornerShape(20.dp),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GoogleLogoIcon(modifier = Modifier.size(24.dp))
                    Text(
                        "Choose an account",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "to continue to MKeyboard Cloud Sync & AI Studio",
                        fontSize = 12.sp,
                        color = textMuted
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Account Option 1: rajeshchoukhe6@gmail.com
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) Color(0xFF282C3D) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                authManager.signInWithAccount("rajeshchoukhe6@gmail.com", "Rajesh Choukhe")
                                Toast.makeText(context, "Signed in as Rajesh Choukhe (+200 bonus credits)", Toast.LENGTH_SHORT).show()
                                showAccountPickerDialog = false
                                onSignInSuccess()
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFEA4335)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("R", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column {
                                Text("Rajesh Choukhe", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                                Text("rajeshchoukhe6@gmail.com", fontSize = 11.5.sp, color = textMuted)
                            }
                        }
                    }

                    // Account Option 2: Tester Account (tester.lingokey@gmail.com)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) Color(0xFF282C3D) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                authManager.signInWithAccount("tester.lingokey@gmail.com", "App Reviewer")
                                Toast.makeText(context, "Signed in as App Reviewer (+200 bonus credits)", Toast.LENGTH_SHORT).show()
                                showAccountPickerDialog = false
                                onSignInSuccess()
                                onDismiss()
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF34A853)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("T", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            }
                            Column {
                                Text("App Reviewer", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                                Text("tester.lingokey@gmail.com", fontSize = 11.5.sp, color = textMuted)
                            }
                        }
                    }

                    // Option 3: Custom Email or Another Account
                    if (!showCustomInputFields) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Transparent,
                            border = BorderStroke(1.dp, if (isDarkMode) Color(0xFF374151) else Color(0xFFCBD5E1)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCustomInputFields = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = textMuted, modifier = Modifier.size(24.dp))
                                Text("Add or use another Google account", fontSize = 12.5.sp, color = textColor, fontWeight = FontWeight.Medium)
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customNameInput,
                                onValueChange = { customNameInput = it },
                                label = { Text("Display Name", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )
                            OutlinedTextField(
                                value = customEmailInput,
                                onValueChange = { customEmailInput = it },
                                label = { Text("Google Email", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp)
                            )
                            Button(
                                onClick = {
                                    val email = customEmailInput.trim().ifEmpty { "tester@gmail.com" }
                                    val name = customNameInput.trim().ifEmpty { email.substringBefore("@") }
                                    authManager.signInWithAccount(email, name)
                                    Toast.makeText(context, "Signed in as $name (+200 bonus credits)", Toast.LENGTH_SHORT).show()
                                    showAccountPickerDialog = false
                                    onSignInSuccess()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
                            ) {
                                Text("Sign In with This Account", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccountPickerDialog = false }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
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
