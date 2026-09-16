package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ClipboardRepository
import com.example.data.LingoKeyPreferences

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardRepository = remember { ClipboardRepository.getInstance(context) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security Center") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // HERO "YOUR DATA IS SAFE" CARD (Matched directly from screenshot)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF10B981).copy(alpha = 0.08f)
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF10B981).copy(alpha = 0.6f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Green Shield with Checkmark
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFF10B981).copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Safe Shield",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Your data is safe",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF10B981)
                        )
                        Text(
                            text = "Global Keyboard Dynamic does not collect anything that you type. The warning you see during installation is displayed for ALL THIRD-PARTY keyboards from the Android System.",
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF06B6D4))
                        Text("Zero-Data Storage Architecture", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Text(
                        "Global Keyboard Dynamic is built with privacy by design. We do not transmit or log your keystrokes. Translation and speech synthesis happen locally on device or securely when you explicitly invoke an AI transformation.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider()

                    Text("🔒 Passwords & Sensitive Fields are automatically excluded from AI suggestions.", fontSize = 12.sp)
                    Text("🛡️ Real-time TTS audio and Voice Typing data are processed transiently on-device and never stored or uploaded.", fontSize = 12.sp)
                    Text("🎙️ Microphone access is strictly user-initiated, never recorded in the background, and released immediately upon finishing.", fontSize = 12.sp)
                    Text("🔑 Fully compliant with Google Play Data Safety and privacy policies.", fontSize = 12.sp)
                }
            }

            Text("Data Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            clipboardRepository.clearAll()
                            Toast.makeText(context, "Clipboard history cleared", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Clipboard History")
                    }
                }
            }

            Text("Google Play Data Safety & Support", fontWeight = FontWeight.Bold, fontSize = 16.sp)

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Policy, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Text("Google Play Compliance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }

                    Text(
                        "This keyboard does NOT sell, rent, or monetize your personal information. Regular typing keystrokes are processed strictly on-device. When AI tools are used, prompts are transmitted securely via encrypted HTTPS directly to Google Gemini APIs and are never used to train global advertising profiles.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 16.sp
                    )

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(16.dp))
                            Text("Support & Privacy Inquiries", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                        TextButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:rajeshchoukhe6@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Global Keyboard Dynamic - Privacy Inquiry")
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Contact: rajeshchoukhe6@gmail.com", Toast.LENGTH_LONG).show()
                                }
                            }
                        ) {
                            Text("Email Us", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
