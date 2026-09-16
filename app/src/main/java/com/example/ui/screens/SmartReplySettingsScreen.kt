package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences
import com.example.engine.smartreply.DefaultSmartReplyUsageRepository
import com.example.engine.smartreply.SmartReplyRepository
import com.example.model.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartReplySettingsScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit,
    onNavigateToPro: () -> Unit = {},
    onNavigateToSpinAndWin: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()
    val aiCredits by preferences.aiCredits.collectAsState()
    val smartReplyEnabled by preferences.smartReplyEnabled.collectAsState()
    val smartReplyDefaultStyleId by preferences.smartReplyDefaultStyleId.collectAsState()
    val smartReplyLanguage by preferences.smartReplyLanguage.collectAsState()
    val smartReplyMaxSuggestions by preferences.smartReplyMaxSuggestions.collectAsState()
    val smartReplyUsageCount by preferences.smartReplyUsageCount.collectAsState()

    val currentDefaultStyle = remember(smartReplyDefaultStyleId) {
        SmartReplyLibrary.findById(smartReplyDefaultStyleId)
    }

    var selectedSettingsCategory by remember { mutableStateOf(SmartReplyStyleCategory.SMART_AUTO) }
    var styleSearchQuery by remember { mutableStateOf("") }

    val bgColor = if (isDarkMode) Color(0xFF0F1016) else Color(0xFFF8FAFC)
    val cardColor = if (isDarkMode) Color(0xFF181924) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color.White else Color(0xFF0F172A)
    val textMuted = if (isDarkMode) Color(0xFF94A3B8) else Color(0xFF64748B)
    val borderColor = if (isDarkMode) Color(0xFF26293A) else Color(0xFFE2E8F0)
    val primaryColor = Color(0xFF6366F1)

    // Interactive Test State
    var testMessage by remember { mutableStateOf("Bhai kal meeting kitne baje hai? Are we finalizing the project?") }
    var testSelectedStyle by remember { mutableStateOf(SmartReplyLibrary.SMART_MATCH) }
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
                    Surface(
                        shape = CircleShape,
                        color = primaryColor.copy(alpha = 0.2f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = primaryColor, modifier = Modifier.size(24.dp))
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart Reply & Tone Library",
                            color = textColor,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Browse 60+ styles across 10 categories, purpose-driven replies, and multilingual context matching.",
                            color = textMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // Enable / Disable Smart Reply Switch
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
                        Text("Enable Smart Reply", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        Text("Show Smart Reply tool in keyboard toolbar", color = textMuted, fontSize = 12.sp)
                    }
                    Switch(
                        checked = smartReplyEnabled,
                        onCheckedChange = { preferences.setSmartReplyEnabled(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = primaryColor)
                    )
                }
            }

            // Daily Quota & VIP status
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = primaryColor, modifier = Modifier.size(20.dp))
                            Text("Usage Quota", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                        }

                        if (isPremiumUser) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Text(
                                    "👑 VIP Unlimited",
                                    color = Color(0xFF10B981),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
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
                            "You have unlimited Smart Reply generations across all 60+ styles and languages.",
                            color = textMuted,
                            fontSize = 12.sp
                        )
                    } else {
                        val used = smartReplyUsageCount
                        val remaining = (20 - used).coerceAtLeast(0)
                        Text(
                            "$used / 20 free generations used today ($remaining remaining). Each Smart Reply costs 1 credit.",
                            color = textMuted,
                            fontSize = 12.sp
                        )
                        LinearProgressIndicator(
                            progress = { (used.toFloat() / 20f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = primaryColor,
                            trackColor = borderColor
                        )
                    }

                    HorizontalDivider(color = borderColor.copy(alpha = 0.5f), thickness = 0.8.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("AI Credits Vault:", fontSize = 12.sp, color = textMuted)
                            Text(
                                if (isPremiumUser) "Unlimited 👑" else "$aiCredits Credits 🪙",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPremiumUser) Color(0xFF10B981) else Color(0xFFF59E0B)
                            )
                        }
                        if (!isPremiumUser) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F766E).copy(alpha = 0.15f),
                                modifier = Modifier.clickable { onNavigateToSpinAndWin() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🎰", fontSize = 12.sp)
                                    Text("Spin & Win", fontSize = 11.5.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Quick Testing Controls (Free 20 Quota vs VIP Unlimited)
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🧪 Testing Mode (Quota & VIP)", color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isPremiumUser) Color(0xFF8B5CF6).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.15f)
                        ) {
                            Text(
                                if (isPremiumUser) "👑 VIP Unlimited" else "👤 Free (20 Quota)",
                                color = if (isPremiumUser) Color(0xFFA78BFA) else Color(0xFF10B981),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        "Test switching between Free tier (20 free replies limit, 1 credit per reply) and Premium tier (unlimited replies):",
                        fontSize = 11.5.sp,
                        color = textMuted
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                preferences.setPremiumUser(!isPremiumUser)
                                Toast.makeText(
                                    context,
                                    if (!isPremiumUser) "👑 Switched to VIP Pro (Unlimited)!" else "👤 Switched to Free User (20 Free Quota)",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isPremiumUser) Color(0xFF10B981) else Color(0xFF8B5CF6)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text(
                                if (isPremiumUser) "Switch to Free" else "Switch to VIP Pro",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Button(
                            onClick = {
                                preferences.setCreditsForTesting(0)
                                Toast.makeText(context, "🪙 Set 0 Credits (Test Quota Limit Over)", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(0.9f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("Set 0 Credits", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        Button(
                            onClick = {
                                preferences.setCreditsForTesting(20)
                                preferences.resetSmartReplyUsageForTesting()
                                Toast.makeText(context, "🔄 Reset to 20 Free Credits & 0 Used!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1.1f).height(34.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                        ) {
                            Text("Reset 20 Free", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
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
                        Column {
                            Text("Default Reply Style", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text("Active: ${currentDefaultStyle.emoji} ${currentDefaultStyle.displayName}", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Category Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (cat in SmartReplyStyleCategory.values()) {
                            val isSel = (selectedSettingsCategory == cat)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) primaryColor else bgColor,
                                border = BorderStroke(1.dp, if (isSel) primaryColor else borderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedSettingsCategory = cat }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(cat.iconEmoji, fontSize = 12.sp)
                                    Text(
                                        cat.displayName,
                                        color = if (isSel) Color.White else textColor,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Styles list in selected category
                    val stylesInCat = remember(selectedSettingsCategory) {
                        SmartReplyLibrary.getStylesForCategory(selectedSettingsCategory)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (style in stylesInCat) {
                            val isSelected = (currentDefaultStyle.id == style.id)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) primaryColor.copy(alpha = 0.12f) else bgColor,
                                border = BorderStroke(1.dp, if (isSelected) primaryColor else borderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { preferences.setSmartReplyDefaultStyle(style.id) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(style.emoji, fontSize = 18.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(style.displayName, color = textColor, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(style.promptDescription, color = textMuted, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
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
                        Text("Number of options generated per message (3–10)", color = textMuted, fontSize = 12.sp)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(3, 5, 8, 10).forEach { count ->
                            val isSelected = (smartReplyMaxSuggestions == count)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) primaryColor else bgColor,
                                border = BorderStroke(1.dp, if (isSelected) primaryColor else borderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { preferences.setSmartReplyMaxSuggestions(count) }
                            ) {
                                Text(
                                    text = "$count",
                                    color = if (isSelected) Color.White else textColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
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
                    Text("Interactive Smart Reply Test", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("Test reply generation directly with any sample text and chosen style:", color = textMuted, fontSize = 12.sp)

                    OutlinedTextField(
                        value = testMessage,
                        onValueChange = { testMessage = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = LocalTextStyle.current.copy(color = textColor, fontSize = 12.5.sp),
                        placeholder = { Text("Enter received message...", color = textMuted) }
                    )

                    // Test Style Selector (Horizontal scroll)
                    Text("Selected Test Style: ${testSelectedStyle.emoji} ${testSelectedStyle.displayName}", color = primaryColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            SmartReplyLibrary.SMART_MATCH,
                            SmartReplyLibrary.CASUAL,
                            SmartReplyLibrary.PROFESSIONAL,
                            SmartReplyLibrary.SWEET,
                            SmartReplyLibrary.FUNNY,
                            SmartReplyLibrary.CONFIRM,
                            SmartReplyLibrary.DECLINE,
                            SmartReplyLibrary.APOLOGIZE_PURPOSE,
                            SmartReplyLibrary.SCHEDULE,
                            SmartReplyLibrary.VERY_SHORT
                        ).forEach { s ->
                            val isSel = (testSelectedStyle.id == s.id)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) primaryColor else bgColor,
                                border = BorderStroke(1.dp, if (isSel) primaryColor else borderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { testSelectedStyle = s }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(s.emoji, fontSize = 11.sp)
                                    Text(
                                        s.displayName,
                                        color = if (isSel) Color.White else textColor,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                testUiState = SmartReplyUiState.Analyzing("Generating replies with ${testSelectedStyle.displayName} style...")
                                val res = repository.processSmartReply(
                                    message = testMessage,
                                    style = testSelectedStyle,
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
                        Text("Generate Test Replies (${testSelectedStyle.displayName})", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    when (val state = testUiState) {
                        is SmartReplyUiState.Analyzing -> {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
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
                                    "Generated Replies (${state.response.detectedLanguage} • ${state.response.appliedStyle.displayName}):",
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
                        is SmartReplyUiState.ProviderError -> {
                            Text(state.message, color = Color(0xFFEF4444), fontSize = 12.sp)
                        }
                        else -> {}
                    }
                }
            }

            // Privacy Guarantee Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = cardColor,
                border = BorderStroke(1.dp, borderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                        Text("Privacy Guarantee", color = textColor, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        "• No screen scraping: Keyboard never scans active apps or chat screens.\n" +
                        "• Manual trigger only: Smart Reply only analyzes messages you explicitly paste or enter.\n" +
                        "• Strict credential blocking: Completely disabled in password and PIN fields.\n" +
                        "• Encrypted processing: Zero persistence of message contents.",
                        color = textMuted,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
        }
    }
}
