package com.example.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences
import com.example.engine.smartreply.SmartReplyRepository
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SmartReplyPanel(
    theme: KeyboardTheme,
    repository: SmartReplyRepository,
    isSensitiveField: Boolean,
    isInputConnectionActive: Boolean,
    onInsertReply: (String) -> Unit,
    onBack: () -> Unit,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit,
    panelHeight: Dp = 272.dp
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferences = remember { LingoKeyPreferences.getInstance(context) }
    val maxSuggestions by preferences.smartReplyMaxSuggestions.collectAsState()

    var inputMessage by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(SmartReplyLibrary.SMART_MATCH) }
    var selectedCategory by remember { mutableStateOf(SmartReplyStyleCategory.SMART_AUTO) }
    var selectedLanguage by remember { mutableStateOf("auto") }
    var uiState by remember { mutableStateOf<SmartReplyUiState>(SmartReplyUiState.Idle) }

    // More Styles library modal state
    var showMoreStylesModal by remember { mutableStateOf(false) }
    var styleSearchQuery by remember { mutableStateOf("") }
    var modalFilterCategory by remember { mutableStateOf<SmartReplyStyleCategory?>(null) }

    // Editing before insert state
    var editingReplyIndex by remember { mutableStateOf<Int?>(null) }
    var editedReplyText by remember { mutableStateOf("") }

    // Privacy info dialog state
    var showPrivacyDialog by remember { mutableStateOf(false) }

    fun readFromClipboard(): String {
        return try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = cm?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                clip.getItemAt(0)?.text?.toString() ?: ""
            } else ""
        } catch (e: Exception) {
            ""
        }
    }

    fun copyToClipboard(text: String) {
        try {
            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            val clip = ClipData.newPlainText("Smart Reply", text)
            cm?.setPrimaryClip(clip)
            Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun analyzeMessage(styleToUse: SmartReplyStyle = selectedStyle) {
        if (isSensitiveField) {
            uiState = SmartReplyUiState.SensitiveFieldBlocked
            return
        }

        if (inputMessage.isBlank()) {
            uiState = SmartReplyUiState.EmptyResult("Please paste or type a received message first.")
            return
        }

        coroutineScope.launch {
            uiState = SmartReplyUiState.Analyzing("Applying ${styleToUse.displayName} style...")
            delay(150) // visual feedback
            val resultState = repository.processSmartReply(
                message = inputMessage,
                style = styleToUse,
                language = selectedLanguage,
                maxReplies = maxSuggestions,
                editorInfo = null,
                isInputConnectionActive = isInputConnectionActive
            )
            uiState = resultState
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
    ) {
        // Main Smart Reply View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Keyboard",
                            tint = theme.textColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "Smart Reply",
                        color = theme.textColor,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Active Style Indicator Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = theme.primaryColor.copy(alpha = 0.18f),
                        border = BorderStroke(0.5.dp, theme.primaryColor.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showMoreStylesModal = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(selectedStyle.emoji, fontSize = 10.sp)
                            Text(
                                text = selectedStyle.displayName,
                                color = theme.primaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = "Change Style",
                                tint = theme.primaryColor,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { showPrivacyDialog = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Privacy Information",
                            tint = theme.textSecondaryColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Smart Reply Settings",
                            tint = theme.textSecondaryColor,
                            modifier = Modifier.size(15.dp)
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = theme.textSecondaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Sensitive field warning if applicable
            if (isSensitiveField) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, Color(0xFFEF4444)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(18.dp))
                        Text(
                            text = "Smart Reply is disabled in password and credential fields for your security.",
                            color = theme.textColor,
                            fontSize = 11.sp
                        )
                    }
                }
                return@Column
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Input box
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(theme.keyColor, RoundedCornerShape(8.dp))
                            .border(BorderStroke(0.5.dp, theme.keyBorderColor), RoundedCornerShape(8.dp))
                            .padding(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Received Message:",
                                color = theme.textSecondaryColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = theme.surfaceColor,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            val clipText = readFromClipboard()
                                            if (clipText.isNotBlank()) {
                                                inputMessage = clipText
                                            } else {
                                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(11.dp))
                                        Text("Paste", color = theme.accentColor, fontSize = 9.5.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (inputMessage.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = theme.surfaceColor,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                inputMessage = ""
                                                uiState = SmartReplyUiState.Idle
                                            }
                                    ) {
                                        Text("Clear", color = theme.textSecondaryColor, fontSize = 9.5.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 28.dp, max = 54.dp)
                        ) {
                            if (inputMessage.isEmpty()) {
                                Text(
                                    text = "Paste or type received message to generate replies...",
                                    color = theme.textSecondaryColor.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                            BasicTextField(
                                value = inputMessage,
                                onValueChange = {
                                    if (it.length <= 8000) {
                                        inputMessage = it
                                        if (uiState is SmartReplyUiState.EmptyResult || uiState is SmartReplyUiState.Idle) {
                                            uiState = SmartReplyUiState.InputEntered(it)
                                        }
                                    }
                                },
                                textStyle = TextStyle(
                                    color = theme.textColor,
                                    fontSize = 11.5.sp
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${inputMessage.length} / 8000",
                                color = if (inputMessage.length > 8000) Color.Red else theme.textSecondaryColor.copy(alpha = 0.6f),
                                fontSize = 8.5.sp
                            )
                        }
                    }
                }

                // Row 1: Reply Style Categories + "More Styles" Button
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "More Styles" button
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = theme.primaryColor.copy(alpha = 0.15f),
                            border = BorderStroke(0.7.dp, theme.primaryColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showMoreStylesModal = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(Icons.Default.Menu, contentDescription = null, tint = theme.primaryColor, modifier = Modifier.size(11.dp))
                                Text(
                                    text = "More (${SmartReplyLibrary.ALL_STYLES.size})",
                                    color = theme.primaryColor,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Category Pills
                        for (cat in SmartReplyStyleCategory.values()) {
                            val isCatSelected = (selectedCategory == cat)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isCatSelected) theme.accentColor.copy(alpha = 0.22f) else theme.keyColor,
                                border = BorderStroke(0.5.dp, if (isCatSelected) theme.accentColor else theme.keyBorderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { selectedCategory = cat }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(cat.iconEmoji, fontSize = 9.5.sp)
                                    Text(
                                        text = cat.displayName,
                                        color = if (isCatSelected) theme.accentColor else theme.textColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                // Row 2: Styles in currently selected category
                item {
                    val stylesInCat = remember(selectedCategory) {
                        SmartReplyLibrary.getStylesForCategory(selectedCategory)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Style:",
                            color = theme.textSecondaryColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )

                        for (style in stylesInCat) {
                            val isStyleSelected = (selectedStyle.id == style.id)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isStyleSelected) theme.primaryColor else theme.keyColor,
                                border = BorderStroke(0.5.dp, if (isStyleSelected) theme.primaryColor else theme.keyBorderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedStyle = style
                                        if (inputMessage.isNotBlank() && uiState is SmartReplyUiState.Success) {
                                            analyzeMessage(style)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(style.emoji, fontSize = 10.sp)
                                    Text(
                                        text = style.displayName,
                                        color = if (isStyleSelected) Color.White else theme.textColor,
                                        fontSize = 10.sp,
                                        fontWeight = if (isStyleSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (isStyleSelected) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
                                    }
                                }
                            }
                        }
                    }
                }

                // Language output selector row
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reply Lang:",
                            color = theme.textSecondaryColor,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )

                        val languageOptions = listOf(
                            "auto" to "🌐 Auto",
                            "en" to "🇺🇸 English",
                            "hi" to "🇮🇳 Hindi",
                            "mr" to "🚩 Marathi",
                            "bn" to "🇧🇩 Bengali",
                            "gu" to "🇮🇳 Gujarati",
                            "ta" to "🇮🇳 Tamil",
                            "te" to "🇮🇳 Telugu",
                            "kn" to "🇮🇳 Kannada",
                            "pa" to "🇮🇳 Punjabi"
                        )

                        for ((code, name) in languageOptions) {
                            val isSelected = (selectedLanguage == code)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSelected) theme.accentColor.copy(alpha = 0.25f) else theme.keyColor,
                                border = BorderStroke(0.5.dp, if (isSelected) theme.accentColor else theme.keyBorderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { selectedLanguage = code }
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) theme.accentColor else theme.textColor,
                                    fontSize = 9.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.5.dp)
                                )
                            }
                        }
                    }
                }

                // Action Button: Analyze / Regenerate
                item {
                    Button(
                        onClick = { analyzeMessage() },
                        enabled = inputMessage.isNotBlank() && uiState !is SmartReplyUiState.Analyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                if (uiState is SmartReplyUiState.Success) Icons.Default.Refresh else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (uiState is SmartReplyUiState.Success) {
                                    "Regenerate (${selectedStyle.emoji} ${selectedStyle.displayName})"
                                } else {
                                    "Generate Replies • ${selectedStyle.emoji} ${selectedStyle.displayName}"
                                },
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Results / Loading / Errors
                when (val state = uiState) {
                    is SmartReplyUiState.Analyzing -> {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.keyColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                        color = theme.accentColor
                                    )
                                    Text(
                                        text = state.stepMessage,
                                        color = theme.textColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    is SmartReplyUiState.Success -> {
                        val response = state.response

                        // Info badges
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(10.dp))
                                            Text(
                                                text = "${response.detectedLanguage} ${if (response.isCodeMixed) "(Code-Mixed)" else ""}",
                                                color = Color(0xFF10B981),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = theme.primaryColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${response.appliedStyle.emoji} ${response.appliedStyle.displayName}",
                                            color = theme.primaryColor,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "${response.replies.size} Replies • Tap to insert • 📋 Copy",
                                    color = theme.textSecondaryColor,
                                    fontSize = 9.sp
                                )
                            }
                        }

                        // Suggestions cards
                        items(response.replies.withIndex().toList()) { (index, reply) ->
                            val isEditingThis = (editingReplyIndex == index)

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.keyColor,
                                border = BorderStroke(0.8.dp, theme.keyBorderColor),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(7.dp)) {
                                    if (isEditingThis) {
                                        Text(
                                            text = "Edit reply before inserting:",
                                            color = theme.accentColor,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        BasicTextField(
                                            value = editedReplyText,
                                            onValueChange = { editedReplyText = it },
                                            textStyle = TextStyle(color = theme.textColor, fontSize = 11.5.sp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(theme.surfaceColor, RoundedCornerShape(6.dp))
                                                .padding(6.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.End,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            TextButton(
                                                onClick = { editingReplyIndex = null },
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text("Cancel", fontSize = 10.sp, color = theme.textSecondaryColor)
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Button(
                                                onClick = {
                                                    onInsertReply(editedReplyText)
                                                    editingReplyIndex = null
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                                modifier = Modifier.height(26.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                                            ) {
                                                Text("Insert", fontSize = 10.sp, color = Color.White)
                                            }
                                        }
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = theme.accentColor.copy(alpha = 0.18f)
                                            ) {
                                                Text(
                                                    text = reply.tone,
                                                    color = theme.accentColor,
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        editingReplyIndex = index
                                                        editedReplyText = reply.text
                                                    },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit reply", tint = theme.textSecondaryColor, modifier = Modifier.size(12.dp))
                                                }

                                                IconButton(
                                                    onClick = { copyToClipboard(reply.text) },
                                                    modifier = Modifier.size(24.dp)
                                                ) {
                                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy to clipboard", tint = theme.textSecondaryColor, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(2.dp))

                                        // Reply body - tap to insert directly
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable { onInsertReply(reply.text) }
                                                .padding(vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = reply.text,
                                                color = theme.textColor,
                                                fontSize = 11.5.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    is SmartReplyUiState.EmptyResult -> {
                        item {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = theme.keyColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = state.reason,
                                    color = theme.textSecondaryColor,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    is SmartReplyUiState.NetworkError -> {
                        item {
                            ErrorCard(theme, "Network Error", state.message) { analyzeMessage() }
                        }
                    }

                    is SmartReplyUiState.RateLimited -> {
                        item {
                            ErrorCard(theme, "Quota Limit", state.message) { analyzeMessage() }
                        }
                    }

                    is SmartReplyUiState.AuthenticationError -> {
                        item {
                            ErrorCard(theme, "Authentication Error", state.message) { analyzeMessage() }
                        }
                    }

                    is SmartReplyUiState.Timeout -> {
                        item {
                            ErrorCard(theme, "Timeout", state.message) { analyzeMessage() }
                        }
                    }

                    is SmartReplyUiState.ProviderError -> {
                        item {
                            ErrorCard(theme, "Service Unavailable", state.message) { analyzeMessage() }
                        }
                    }

                    else -> {}
                }
            }
        }

        // Expandable "More Styles" Library Full Screen Modal within Keyboard Panel
        AnimatedVisibility(
            visible = showMoreStylesModal,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = theme.backgroundColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    // Modal Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { showMoreStylesModal = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = theme.textColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "Reply Style & Tone Library",
                                color = theme.textColor,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = theme.primaryColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showMoreStylesModal = false }
                        ) {
                            Text(
                                text = "Done",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Search input
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = theme.keyColor,
                        border = BorderStroke(0.5.dp, theme.keyBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Default.Search, contentDescription = null, tint = theme.textSecondaryColor, modifier = Modifier.size(14.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (styleSearchQuery.isEmpty()) {
                                    Text(
                                        "Search styles (e.g. casual, confirm, sweet, firm)...",
                                        color = theme.textSecondaryColor.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                                BasicTextField(
                                    value = styleSearchQuery,
                                    onValueChange = { styleSearchQuery = it },
                                    textStyle = TextStyle(color = theme.textColor, fontSize = 11.sp),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (styleSearchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { styleSearchQuery = "" },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = theme.textSecondaryColor, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Category Filter Tabs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val isAllSelected = (modalFilterCategory == null)
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (isAllSelected) theme.primaryColor else theme.keyColor,
                            border = BorderStroke(0.5.dp, if (isAllSelected) theme.primaryColor else theme.keyBorderColor),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { modalFilterCategory = null }
                        ) {
                            Text(
                                text = "All (${SmartReplyLibrary.ALL_STYLES.size})",
                                color = if (isAllSelected) Color.White else theme.textColor,
                                fontSize = 9.5.sp,
                                fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                            )
                        }

                        for (cat in SmartReplyStyleCategory.values()) {
                            val isSel = (modalFilterCategory == cat)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) theme.primaryColor else theme.keyColor,
                                border = BorderStroke(0.5.dp, if (isSel) theme.primaryColor else theme.keyBorderColor),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { modalFilterCategory = cat }
                            ) {
                                Text(
                                    text = "${cat.iconEmoji} ${cat.displayName}",
                                    color = if (isSel) Color.White else theme.textColor,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Filtered Styles List
                    val filteredStyles = remember(styleSearchQuery, modalFilterCategory) {
                        SmartReplyLibrary.ALL_STYLES.filter { style ->
                            val matchesCategory = (modalFilterCategory == null || style.category == modalFilterCategory)
                            val matchesSearch = styleSearchQuery.isBlank() ||
                                style.displayName.contains(styleSearchQuery, ignoreCase = true) ||
                                style.promptDescription.contains(styleSearchQuery, ignoreCase = true) ||
                                style.category.displayName.contains(styleSearchQuery, ignoreCase = true)
                            matchesCategory && matchesSearch
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredStyles, key = { it.id }) { style ->
                            val isChosen = (selectedStyle.id == style.id)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isChosen) theme.primaryColor.copy(alpha = 0.18f) else theme.keyColor,
                                border = BorderStroke(if (isChosen) 1.dp else 0.5.dp, if (isChosen) theme.primaryColor else theme.keyBorderColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedStyle = style
                                        selectedCategory = style.category
                                        showMoreStylesModal = false
                                        if (inputMessage.isNotBlank()) {
                                            analyzeMessage(style)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(style.emoji, fontSize = 16.sp)

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = style.displayName,
                                                color = theme.textColor,
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(3.dp),
                                                color = theme.surfaceColor
                                            ) {
                                                Text(
                                                    text = style.category.displayName,
                                                    color = theme.textSecondaryColor,
                                                    fontSize = 8.5.sp,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = style.promptDescription,
                                            color = theme.textSecondaryColor,
                                            fontSize = 9.5.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (isChosen) {
                                        Icon(
                                            Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = theme.primaryColor,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Privacy dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = theme.accentColor)
                        Text("Smart Reply Privacy Protection", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "• Zero Background Scraping: Smart Reply NEVER continuously reads or scans your screen.",
                            fontSize = 12.sp
                        )
                        Text(
                            "• User Initiated Only: Text is analyzed only when you explicitly paste or type into the Smart Reply box and tap Generate.",
                            fontSize = 12.sp
                        )
                        Text(
                            "• Sensitive Fields Blocked: Disabled automatically in password, PIN, and credential input fields.",
                            fontSize = 12.sp
                        )
                        Text(
                            "• Secure Inference: Analyzed via encrypted direct connection with prompt injection defense and strict safety filtering.",
                            fontSize = 12.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("Got it", color = theme.primaryColor, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

@Composable
private fun ErrorCard(
    theme: KeyboardTheme,
    title: String,
    message: String,
    onRetry: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFEF4444).copy(alpha = 0.12f),
        border = BorderStroke(0.5.dp, Color(0xFFEF4444).copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color(0xFFEF4444), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(message, color = theme.textColor, fontSize = 10.sp)
            }
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(28.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Text("Retry", fontSize = 10.5.sp, color = Color.White)
            }
        }
    }
}
