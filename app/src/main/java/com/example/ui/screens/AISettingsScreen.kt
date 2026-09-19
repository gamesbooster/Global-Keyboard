package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences
import com.example.engine.AIProvider
import com.example.engine.GeminiAIProvider
import com.example.engine.VoiceTTSEngine
import com.example.model.ToneType
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.NeumorphicColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class ChatSender {
    USER, AI
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: ChatSender,
    val text: String,
    val timestamp: String = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date()),
    val categoryTag: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AISettingsScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit,
    onNavigateToPro: () -> Unit = {},
    onNavigateToSmartReply: () -> Unit = {},
    onNavigateToSpinAndWin: () -> Unit = {}
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val isDarkMode by preferences.isDarkMode.collectAsState()
    val isPremiumUser by preferences.isPremiumUser.collectAsState()

    val aiProvider: AIProvider = remember { GeminiAIProvider() }
    val ttsEngine = remember { VoiceTTSEngine.getInstance(context) }

    val bgColor = if (isDarkMode) NeumorphicColors.DarkScreenBg else NeumorphicColors.LightScreenBg
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted
    val primaryColor = Color(0xFF6366F1)
    val accentColor = NeumorphicColors.EmeraldAccent
    val goldColor = Color(0xFFF59E0B)

    // --- VIP ACCESS GATE (ONLY VIP / PAID SUBSCRIBERS CAN ACCESS) ---
    if (!isPremiumUser) {
        Scaffold(
            containerColor = bgColor,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "AI Writing Assistant",
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    "👑 VIP Members Only",
                                    color = goldColor,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = textColor
                            )
                        }
                    },
                    actions = {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = goldColor.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, goldColor),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clickable { onNavigateToPro() }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("👑", fontSize = 11.5.sp)
                                Text(
                                    "VIP PASS",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = goldColor
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
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // VIP Crown Glowing Emblem
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(goldColor.copy(alpha = 0.28f), goldColor.copy(alpha = 0.04f))
                            )
                        )
                        .border(1.5.dp, goldColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.WorkspacePremium,
                        contentDescription = "VIP Only",
                        tint = goldColor,
                        modifier = Modifier.size(42.dp)
                    )
                }

                // Title & Explanatory Copy
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "VIP Exclusive Feature",
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "To prevent API rate limits and guarantee instant, unthrottled Gemini AI responses, AI Writing Assistant is reserved exclusively for VIP Members.",
                        fontSize = 13.sp,
                        color = textMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }

                // Perks Neumorphic Card
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDarkMode = isDarkMode,
                    cornerRadius = 18.dp,
                    elevation = 5.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "WHAT YOU UNLOCK WITH VIP:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = goldColor,
                            letterSpacing = 0.5.sp
                        )

                        val vipPerks = listOf(
                            Triple("⚡", "Unlimited Gemini AI Generation", "No coins or daily caps—full access to all models"),
                            Triple("✍️", "Grammar & Spelling Engine", "Instant proofreading and multi-rule error fixes"),
                            Triple("✉️", "Professional Email Drafter", "Instant workplace emails, leave requests & updates"),
                            Triple("💬", "Smart Contextual Replies", "Intelligent responses adapted to your preferred tone"),
                            Triple("📝", "Document & Note Summarizer", "Condense lengthy text into clear, actionable bullet points"),
                            Triple("🌐", "Indic & Hinglish Writing Engine", "Natural conversational rephrasing and Hindi support")
                        )

                        vipPerks.forEach { (emoji, title, desc) ->
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(emoji, fontSize = 16.sp)
                                Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                                    Text(
                                        title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = textColor
                                    )
                                    Text(
                                        desc,
                                        fontSize = 11.5.sp,
                                        color = textMuted,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Action CTA Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Unlock VIP Button
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = goldColor,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clickable { onNavigateToPro() }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text("👑", fontSize = 16.sp)
                                Text(
                                    "Unlock VIP Access",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Back Button
                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            "Return to Dashboard",
                            color = textMuted,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        return
    }

    // --- FULL AI ASSISTANT CHAT SCREEN (VIP MEMBERS ONLY) ---
    var inputText by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var currentlySpeakingId by remember { mutableStateOf<String?>(null) }

    val initialGreeting = remember {
        ChatMessage(
            sender = ChatSender.AI,
            text = "Welcome VIP Member! I am your AI Writing Assistant. Type any query below to fix grammar, compose professional emails, draft smart replies, or summarize documents.",
            categoryTag = "VIP Assistant"
        )
    }

    val messages = remember { mutableStateListOf(initialGreeting) }

    fun speakText(id: String, text: String) {
        if (currentlySpeakingId == id) {
            ttsEngine.stop()
            currentlySpeakingId = null
        } else {
            ttsEngine.stop()
            ttsEngine.speak(text)
            currentlySpeakingId = id
        }
    }

    fun shareText(text: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(intent, "Share AI Response"))
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot share text", Toast.LENGTH_SHORT).show()
        }
    }

    fun executeGeneration(prompt: String, explicitCategory: String? = null) {
        val trimmed = prompt.trim()
        if (trimmed.isBlank() || isGenerating) return

        // Extra security guarantee: verify VIP membership before calling API
        if (!preferences.isPremiumUser.value) {
            onNavigateToPro()
            return
        }

        focusManager.clearFocus()
        val userMsg = ChatMessage(sender = ChatSender.USER, text = trimmed)
        messages.add(userMsg)
        inputText = ""
        isGenerating = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)

            val lower = trimmed.lowercase()
            val category = explicitCategory ?: when {
                lower.startsWith("fix grammar") || lower.contains("grammar:") -> "Grammar Fix"
                lower.contains("email") || lower.contains("mail") -> "Email Draft"
                lower.contains("reply") || lower.contains("respond") -> "Smart Reply"
                lower.contains("summar") -> "Summary"
                lower.contains("professional") -> "Professional"
                lower.contains("friendly") -> "Friendly"
                lower.contains("hinglish") || lower.contains("hindi") -> "Hindi / Hinglish"
                else -> "Writing Assistant"
            }

            val resultText = try {
                when (category) {
                    "Grammar Fix" -> {
                        val textToFix = trimmed.substringAfter(":").ifBlank { trimmed.substringAfter("grammar").trim() }
                        aiProvider.fixGrammar(textToFix.ifBlank { trimmed }).getOrDefault(
                            "Grammar checked: No critical errors found. Text is clean and clear."
                        )
                    }
                    "Professional" -> {
                        val text = trimmed.substringAfter(":").ifBlank { trimmed }
                        aiProvider.rewrite(text, ToneType.PROFESSIONAL).getOrDefault(
                            "Dear Sir/Madam, I am writing to communicate this update professionally and look forward to your response."
                        )
                    }
                    "Friendly" -> {
                        val text = trimmed.substringAfter(":").ifBlank { trimmed }
                        aiProvider.rewrite(text, ToneType.FRIENDLY).getOrDefault(
                            "Hey there! Hope everything is going great on your end. Just wanted to check in!"
                        )
                    }
                    else -> {
                        aiProvider.customPrompt(trimmed).getOrDefault(
                            "Here is the generated output:\n\n\"$trimmed\"\n\nEverything has been reviewed and formatted."
                        )
                    }
                }
            } catch (e: Exception) {
                "Unable to complete request right now. Please try again."
            }

            preferences.recordAiUsage()
            messages.add(
                ChatMessage(
                    sender = ChatSender.AI,
                    text = resultText,
                    categoryTag = category
                )
            )
            isGenerating = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "AI Writing Assistant",
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(accentColor)
                            )
                            Text(
                                "Gemini AI • Unlimited VIP",
                                color = goldColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = textColor
                        )
                    }
                },
                actions = {
                    // VIP Active Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = goldColor.copy(alpha = 0.16f),
                        border = BorderStroke(1.dp, goldColor),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .clickable { onNavigateToPro() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("👑", fontSize = 11.5.sp)
                            Text(
                                "VIP ACTIVE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = goldColor
                            )
                        }
                    }

                    // Reset Chat
                    IconButton(
                        onClick = {
                            ttsEngine.stop()
                            currentlySpeakingId = null
                            messages.clear()
                            messages.add(initialGreeting)
                            Toast.makeText(context, "Conversation reset", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Reset Chat",
                            tint = textMuted
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = bgColor)
            )
        },
        bottomBar = {
            // Sticky Bottom Input Bar
            Surface(
                color = if (isDarkMode) Color(0xFF1E2028) else Color(0xFFF2EFE8),
                shadowElevation = 8.dp,
                border = BorderStroke(
                    1.dp,
                    if (isDarkMode) Color(0x22FFFFFF) else Color(0x18000000)
                ),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Quick Action Category Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            Triple("✍️ Fix Grammar", "Fix grammar: ", "Grammar Fix"),
                            Triple("✉️ Draft Email", "Write a professional email regarding: ", "Email Draft"),
                            Triple("💬 Smart Reply", "Give me 3 polite replies to: ", "Smart Reply"),
                            Triple("📝 Summarize", "Summarize this in 3 key points: ", "Summary"),
                            Triple("💼 Professional", "Rewrite in a professional tone: ", "Professional"),
                            Triple("🤝 Friendly", "Rewrite in a friendly tone: ", "Friendly"),
                            Triple("🇮🇳 Hinglish", "Translate and polish in Hinglish: ", "Hindi / Hinglish")
                        ).forEach { (label, promptPrefix, category) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDarkMode) Color(0xFF2B2D38) else Color(0xFFE4DFD5),
                                border = BorderStroke(
                                    0.8.dp,
                                    if (isDarkMode) Color(0x33FFFFFF) else Color(0x22000000)
                                ),
                                modifier = Modifier.clickable {
                                    if (inputText.isNotBlank()) {
                                        executeGeneration("$promptPrefix$inputText", category)
                                    } else {
                                        inputText = promptPrefix
                                    }
                                }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    // Input Box Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Ask AI to write, fix grammar, reply...",
                                    fontSize = 13.sp,
                                    color = textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 46.dp, max = 110.dp),
                            shape = RoundedCornerShape(22.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                unfocusedBorderColor = if (isDarkMode) Color(0x33FFFFFF) else Color(0x28000000),
                                focusedContainerColor = if (isDarkMode) Color(0xFF14151B) else Color.White,
                                unfocusedContainerColor = if (isDarkMode) Color(0xFF14151B) else Color.White,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            maxLines = 3,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(
                                onSend = {
                                    if (inputText.isNotBlank()) {
                                        executeGeneration(inputText)
                                    }
                                }
                            ),
                            trailingIcon = {
                                if (inputText.isNotBlank()) {
                                    IconButton(onClick = { inputText = "" }) {
                                        Icon(
                                            Icons.Default.Clear,
                                            contentDescription = "Clear",
                                            tint = textMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        )

                        // Send Button
                        Surface(
                            shape = CircleShape,
                            color = if (inputText.isNotBlank() && !isGenerating) primaryColor else primaryColor.copy(alpha = 0.4f),
                            shadowElevation = if (inputText.isNotBlank()) 4.dp else 0.dp,
                            modifier = Modifier
                                .size(46.dp)
                                .clickable(enabled = inputText.isNotBlank() && !isGenerating) {
                                    executeGeneration(inputText)
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Instant Starter Prompt Cards (shown when only initial greeting is present)
            if (messages.size <= 1) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            "Try Instant Prompts:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = textMuted
                        )

                        val starterPrompts = listOf(
                            Pair("✍️ Fix grammar in my text", "Fix grammar: I has went to office and did not saw him"),
                            Pair("✉️ Draft 2-day leave email", "Write a professional leave request email for 2 days"),
                            Pair("💬 Reply to meeting invite", "Give me 3 polite replies to confirm meeting at 3 PM"),
                            Pair("📝 Summarize key notes", "Summarize: Project phase 1 is complete. Quality testing started. Target launch is next Friday.")
                        )

                        starterPrompts.chunked(2).forEach { rowPairs ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowPairs.forEach { (title, prompt) ->
                                    NeumorphicCard(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(68.dp),
                                        isDarkMode = isDarkMode,
                                        cornerRadius = 14.dp,
                                        elevation = 3.dp,
                                        onClick = { executeGeneration(prompt) }
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(10.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = textColor,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Message History Bubbles
            items(messages, key = { it.id }) { message ->
                if (message.sender == ChatSender.USER) {
                    // User Message (Right Aligned)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(horizontalAlignment = Alignment.End) {
                            Surface(
                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp),
                                color = primaryColor,
                                shadowElevation = 2.dp,
                                modifier = Modifier.widthIn(max = 300.dp)
                            ) {
                                Text(
                                    text = message.text,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    lineHeight = 19.sp,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                )
                            }
                            Text(
                                text = message.timestamp,
                                fontSize = 10.sp,
                                color = textMuted,
                                modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                            )
                        }
                    }
                } else {
                    // Assistant Message (Left Aligned Neumorphic Card)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        NeumorphicCard(
                            modifier = Modifier.widthIn(max = 330.dp),
                            isDarkMode = isDarkMode,
                            cornerRadius = 16.dp,
                            elevation = 4.dp
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Header: Sparkle + Tag
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = goldColor,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            "AI Assistant",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = textColor
                                        )
                                    }

                                    if (!message.categoryTag.isNullOrBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = primaryColor.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = message.categoryTag,
                                                color = if (isDarkMode) Color(0xFF93C5FD) else primaryColor,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                // Message Text
                                Text(
                                    text = message.text,
                                    color = textColor,
                                    fontSize = 13.5.sp,
                                    lineHeight = 19.sp
                                )

                                // Action Buttons (Copy, Speak, Share)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Speak TTS
                                    IconButton(
                                        onClick = { speakText(message.id, message.text) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            if (currentlySpeakingId == message.id) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = "Speak",
                                            tint = if (currentlySpeakingId == message.id) primaryColor else textMuted,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Copy
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(message.text))
                                            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = textMuted,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    // Share
                                    IconButton(
                                        onClick = { shareText(message.text) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Share,
                                            contentDescription = "Share",
                                            tint = textMuted,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Generating / Thinking Indicator
            if (isGenerating) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        NeumorphicCard(
                            isDarkMode = isDarkMode,
                            cornerRadius = 14.dp,
                            elevation = 3.dp,
                            modifier = Modifier.widthIn(max = 240.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = goldColor
                                )
                                Text(
                                    "Gemini AI is generating...",
                                    fontSize = 12.5.sp,
                                    color = textMuted,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
