package com.example.keyboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onOpenSettings: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var inputMessage by remember { mutableStateOf("") }
    var selectedTone by remember { mutableStateOf(SmartReplyTone.AUTO) }
    var selectedLanguage by remember { mutableStateOf("auto") }
    var uiState by remember { mutableStateOf<SmartReplyUiState>(SmartReplyUiState.Idle) }

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

    fun analyzeMessage() {
        if (isSensitiveField) {
            uiState = SmartReplyUiState.SensitiveFieldBlocked
            return
        }

        if (inputMessage.isBlank()) {
            uiState = SmartReplyUiState.EmptyResult("Please paste or type a received message first.")
            return
        }

        coroutineScope.launch {
            uiState = SmartReplyUiState.Analyzing("Understanding message & intent...")
            delay(150) // visual feedback
            val resultState = repository.processSmartReply(
                message = inputMessage,
                tone = selectedTone,
                language = selectedLanguage,
                maxReplies = 4,
                editorInfo = null, // Checked directly via isSensitiveField flag
                isInputConnectionActive = isInputConnectionActive
            )
            uiState = resultState
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(272.dp)
            .background(theme.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
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
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Tools",
                        tint = theme.textColor,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Text(
                    text = "✨ Smart Reply",
                    color = theme.textColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                IconButton(
                    onClick = { showPrivacyDialog = true },
                    modifier = Modifier.size(22.dp)
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "Privacy Information",
                        tint = theme.accentColor,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Smart Reply Settings",
                        tint = theme.textSecondaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = theme.textSecondaryColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Privacy Modal Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFF10B981))
                        Text("Privacy & Security", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Text(
                        text = "Smart Reply only analyzes text you explicitly paste or provide. It does not automatically read your conversations, monitor apps, or send any messages without your explicit tap.\n\nSmart Reply is automatically disabled in password, PIN, and credential fields.",
                        fontSize = 12.5.sp,
                        lineHeight = 17.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("Got it", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }

        // Sensitive Field Warning
        if (isSensitiveField) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFEF4444).copy(alpha = 0.15f),
                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
                    Column {
                        Text(
                            text = "Smart Reply is unavailable in sensitive fields",
                            color = Color(0xFFEF4444),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Password and credential editors are strictly protected.",
                            color = theme.textColor.copy(alpha = 0.8f),
                            fontSize = 11.sp
                        )
                    }
                }
            }
            return@Column
        }

        // Input Connection Inactive Warning
        if (!isInputConnectionActive) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF59E0B).copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
            ) {
                Text(
                    text = "⚠️ Text field unavailable.",
                    color = Color(0xFFF59E0B),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Scrollable Body
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Input Message Box
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = theme.keyColor,
                    border = BorderStroke(1.dp, theme.keyBorderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(6.dp)) {
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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                // Paste button
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = theme.primaryColor.copy(alpha = 0.2f),
                                    modifier = Modifier.clickable {
                                        val clipText = readFromClipboard()
                                        if (clipText.isNotBlank()) {
                                            inputMessage = clipText
                                        } else {
                                            Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = null, tint = theme.accentColor, modifier = Modifier.size(11.dp))
                                        Text("Paste", color = theme.accentColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }

                                if (inputMessage.isNotEmpty()) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = theme.surfaceColor,
                                        modifier = Modifier.clickable { inputMessage = "" }
                                    ) {
                                        Text("Clear", color = theme.textSecondaryColor, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp))
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 34.dp, max = 56.dp)
                        ) {
                            if (inputMessage.isEmpty()) {
                                Text(
                                    text = "Paste or type the message you received...",
                                    color = theme.textSecondaryColor.copy(alpha = 0.6f),
                                    fontSize = 11.5.sp
                                )
                            }
                            BasicTextField(
                                value = inputMessage,
                                onValueChange = { inputMessage = it },
                                textStyle = TextStyle(color = theme.textColor, fontSize = 11.5.sp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Character limit count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${inputMessage.length} / 8000",
                                color = if (inputMessage.length > 8000) Color.Red else theme.textSecondaryColor.copy(alpha = 0.6f),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }

            // Tone & Language selector row
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tone:",
                        color = theme.textSecondaryColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    for (tone in SmartReplyTone.values()) {
                        val isSelected = (selectedTone == tone)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.primaryColor else theme.keyColor,
                            border = BorderStroke(0.5.dp, if (isSelected) theme.primaryColor else theme.keyBorderColor),
                            modifier = Modifier.clickable { selectedTone = tone }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(tone.iconEmoji, fontSize = 10.sp)
                                Text(
                                    tone.displayName,
                                    color = if (isSelected) Color.White else theme.textColor,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
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
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )

                    val languageOptions = listOf(
                        "auto" to "🌐 Auto (Match message)",
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
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) theme.accentColor.copy(alpha = 0.25f) else theme.keyColor,
                            border = BorderStroke(0.5.dp, if (isSelected) theme.accentColor else theme.keyBorderColor),
                            modifier = Modifier.clickable { selectedLanguage = code }
                        ) {
                            Text(
                                text = name,
                                color = if (isSelected) theme.accentColor else theme.textColor,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Action Button: Analyze / Regenerate
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { analyzeMessage() },
                        enabled = inputMessage.isNotBlank() && uiState !is SmartReplyUiState.Analyzing,
                        colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                if (uiState is SmartReplyUiState.Success) Icons.Default.Refresh else Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (uiState is SmartReplyUiState.Success) "Regenerate Replies" else "✨ Analyze Message",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Results / Loading / Errors
            when (val state = uiState) {
                is SmartReplyUiState.Analyzing -> {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.keyColor,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = theme.accentColor
                                )
                                Text(
                                    text = state.stepMessage,
                                    color = theme.textColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                is SmartReplyUiState.Success -> {
                    val response = state.response

                    // Language / Code-mixed badge
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(Icons.Default.Language, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(11.dp))
                                    Text(
                                        text = "${response.detectedLanguage} ${if (response.isCodeMixed) "(Code-Mixed)" else ""}",
                                        color = Color(0xFF10B981),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Text(
                                text = "Tap reply to send • 📋 to copy",
                                color = theme.textSecondaryColor,
                                fontSize = 9.5.sp
                            )
                        }
                    }

                    // Suggestions cards
                    items(response.replies.withIndex().toList()) { (index, reply) ->
                        val isEditingThis = (editingReplyIndex == index)

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = theme.keyColor,
                            border = BorderStroke(1.dp, theme.keyBorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                if (isEditingThis) {
                                    // Inline edit mode
                                    Text(
                                        text = "Edit reply before inserting:",
                                        color = theme.accentColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    BasicTextField(
                                        value = editedReplyText,
                                        onValueChange = { editedReplyText = it },
                                        textStyle = TextStyle(color = theme.textColor, fontSize = 12.sp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(theme.surfaceColor, RoundedCornerShape(6.dp))
                                            .padding(6.dp)
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        TextButton(
                                            onClick = { editingReplyIndex = null },
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Cancel", fontSize = 11.sp, color = theme.textSecondaryColor)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Button(
                                            onClick = {
                                                onInsertReply(editedReplyText)
                                                editingReplyIndex = null
                                            },
                                            shape = RoundedCornerShape(6.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = theme.primaryColor),
                                            modifier = Modifier.height(28.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                                        ) {
                                            Text("Insert", fontSize = 11.sp, color = Color.White)
                                        }
                                    }
                                } else {
                                    // Normal display card
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
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            // Edit button
                                            IconButton(
                                                onClick = {
                                                    editingReplyIndex = index
                                                    editedReplyText = reply.text
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = theme.textSecondaryColor, modifier = Modifier.size(13.dp))
                                            }

                                            // Copy button
                                            IconButton(
                                                onClick = { copyToClipboard(reply.text) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = theme.textSecondaryColor, modifier = Modifier.size(13.dp))
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // Main clickable text that commits directly to input connection
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
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            lineHeight = 16.sp
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
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = state.reason,
                                color = theme.textSecondaryColor,
                                fontSize = 11.5.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }
                }

                is SmartReplyUiState.NetworkError -> {
                    item {
                        ErrorCard(
                            theme = theme,
                            title = "Network Error",
                            message = state.message,
                            onRetry = { analyzeMessage() }
                        )
                    }
                }

                is SmartReplyUiState.RateLimited -> {
                    item {
                        ErrorCard(
                            theme = theme,
                            title = "Quota Reached",
                            message = state.message,
                            onRetry = onOpenSettings
                        )
                    }
                }

                is SmartReplyUiState.Timeout -> {
                    item {
                        ErrorCard(
                            theme = theme,
                            title = "Request Timed Out",
                            message = state.message,
                            onRetry = { analyzeMessage() }
                        )
                    }
                }

                is SmartReplyUiState.ProviderError -> {
                    item {
                        ErrorCard(
                            theme = theme,
                            title = "Unavailable",
                            message = state.message,
                            onRetry = { analyzeMessage() }
                        )
                    }
                }

                is SmartReplyUiState.AuthenticationError -> {
                    item {
                        ErrorCard(
                            theme = theme,
                            title = "Authentication Error",
                            message = state.message,
                            onRetry = onOpenSettings
                        )
                    }
                }

                else -> {}
            }

            // Privacy footer note
            item {
                Text(
                    text = "Smart Reply only processes text you explicitly provide. It never automatically reads or sends messages.",
                    color = theme.textSecondaryColor.copy(alpha = 0.6f),
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                )
            }
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
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
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
