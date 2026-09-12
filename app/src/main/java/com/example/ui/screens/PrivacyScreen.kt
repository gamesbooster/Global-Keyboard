package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
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
                        "LingoKey AI is built with privacy by design. We do not transmit or log your keystrokes. Translation and speech synthesis happen locally on device or securely when you explicitly invoke an AI transformation.",
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
        }
    }
}
