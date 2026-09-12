package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences
import com.example.engine.smartreply.DefaultSmartReplyUsageRepository
import com.example.engine.smartreply.SmartReplyRepository
import com.example.model.SmartReplyTone
import com.example.model.SmartReplyUiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartReplySettingsScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit,
    onNavigateToPro: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()

    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()
    val smartReplyEnabled by preferences.smartReplyEnabled.collectAsState()
    val smartReplyDefaultTone by preferences.smartReplyDefaultTone.collectAsState()
    val smartReplyLanguage by preferences.smartReplyLanguage.collectAsState()
    val smartReplyMaxSuggestions by preferences.smartReplyMaxSuggestions.collectAsState()
    val smartReplyUsageCount by preferences.smartReplyUsageCount.collectAsState()

    val bgColor = if (isDarkMode) Color(0xFF0F1016) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val primaryColor = Color(0xFF6366F1)

    // Interactive Test State
    var testMessage by remember { mutableStateOf("Bhai kal meeting kitne baje hai? Are we finalizing the project?") }
    var testUiState by remember { mutableStateOf<SmartReplyUiState>(SmartReplyUiState.Idle) }

    val repository = remember {
        SmartReplyRepository(
            usageRepository = DefaultSmartReplyUsageRepository(preferences)
        )
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = { Text("✨ Smart Reply Settings", color = textColor, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = cardColor)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Intro Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = cardColor,
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .background(
                            Brush.horizontalGradient(
                                listOf(primaryColor.copy(alpha = 0.12f), Color.Transparent)
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Instant Contextual Replies", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Paste any received message into Keyboard → Tools → Smart Reply to get 3–5 intelligent, multilingual suggestions.",
                            color = textMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Master Enable Toggle
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Enable Smart Reply", color = textColor, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold)
                        Text("Show Smart Reply in keyboard tools & toolbar", color = textMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = smartReplyEnabled,
                        onCheckedChange = { preferences.setSmartReplyEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }
            }

            // Quota / VIP Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                if (isPremiumUser) Icons.Default.WorkspacePremium else Icons.Default.ElectricBolt,
                                contentDescription = null,
                                tint = if (isPremiumUser) Color(0xFFF59E0B) else primaryColor
                            )
                            Text(
                                if (isPremiumUser) "VIP Unlimited Access" else "Daily Free Quota",
                                color = textColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (!isPremiumUser) {
                            Button(
                                onClick = onNavigateToPro,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(30.dp)
                            ) {
                                Text("Get VIP", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (isPremiumUser) {
                        Text(
                            "You have unlimited Smart Reply generations across all languages and styles.",
                            color = textMuted,
                            fontSize = 12.sp
                        )
                    } else {
                        val used = smartReplyUsageCount
                        val remaining = (10 - used).coerceAtLeast(0)
                        Text(
                            "$used / 10 free generations used today ($remaining remaining).",
                            color = textMuted,
                            fontSize = 12.sp
                        )
                        LinearProgressIndicator(
                            progress = { (used.toFloat() / 10f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = primaryColor,
                            trackColor = borderColor
                        )
                    }
                }
            }

            // Default Tone Configuration
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Default Tone", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Choose the initial reply style when opening Smart Reply:", color = textMuted, fontSize = 12.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (tone in SmartReplyTone.values()) {
                            val isSelected = smartReplyDefaultTone.equals(tone.name, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) primaryColor.copy(alpha = 0.12f) else bgColor,
                                border = BorderStroke(1.dp, if (isSelected) primaryColor else borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { preferences.setSmartReplyDefaultTone(tone.name) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(tone.iconEmoji, fontSize = 16.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(tone.displayName, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(tone.promptDescription, color = textMuted, fontSize = 11.sp)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = primaryColor, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Suggestions Count (3, 4, or 5)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Reply Suggestions Count", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Number of options generated per message (3–5)", color = textMuted, fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 4, 5).forEach { count ->
                            val isSelected = (smartReplyMaxSuggestions == count)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) primaryColor else bgColor,
                                border = BorderStroke(1.dp, if (isSelected) primaryColor else borderColor),
                                modifier = Modifier.clickable { preferences.setSmartReplyMaxSuggestions(count) }
                            ) {
                                Text(
                                    text = "$count",
                                    color = if (isSelected) Color.White else textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Interactive Playground Test Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Interactive Smart Reply Test", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Text("Test reply generation directly with any sample text:", color = textMuted, fontSize = 12.sp)

                    OutlinedTextField(
                        value = testMessage,
                        onValueChange = { testMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = textColor, fontSize = 12.5.sp),
                        placeholder = { Text("Enter received message...", color = textMuted) }
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                testUiState = SmartReplyUiState.Analyzing("Generating replies...")
                                val tone = SmartReplyTone.fromString(smartReplyDefaultTone)
                                val res = repository.processSmartReply(
                                    message = testMessage,
                                    tone = tone,
                                    language = "auto",
                                    maxReplies = smartReplyMaxSuggestions
                                )
                                testUiState = res
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Generate Test Replies", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    when (val state = testUiState) {
                        is SmartReplyUiState.Analyzing -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(8.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = primaryColor)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(state.stepMessage, color = textMuted, fontSize = 12.sp)
                            }
                        }
                        is SmartReplyUiState.Success -> {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    "Generated Replies (${state.response.detectedLanguage}):",
                                    color = primaryColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                state.response.replies.forEach { reply ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = bgColor,
                                        border = BorderStroke(1.dp, borderColor),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(reply.text, color = textColor, fontSize = 12.sp, modifier = Modifier.weight(1f))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = primaryColor.copy(alpha = 0.15f)
                                            ) {
                                                Text(
                                                    reply.tone,
                                                    color = primaryColor,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is SmartReplyUiState.EmptyResult -> {
                            Text(state.reason, color = textMuted, fontSize = 12.sp)
                        }
                        is SmartReplyUiState.RateLimited -> {
                            Text(state.message, color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                        is SmartReplyUiState.NetworkError -> {
                            Text(state.message, color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                        else -> {}
                    }
                }
            }

            // Privacy Guarantees Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        Text("Privacy & Security Architecture", color = textColor, fontSize = 13.5.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "• Smart Reply only analyzes text that you explicitly copy and paste into the panel.\n" +
                                "• It does NOT run background monitoring or read conversations.\n" +
                                "• Password, PIN, OTP, and credential fields are strictly shielded from Smart Reply.\n" +
                                "• No message is ever sent automatically without your explicit selection.",
                        color = textMuted,
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}
