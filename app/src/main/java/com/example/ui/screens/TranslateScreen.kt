package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences
import com.example.engine.GoogleTranslationEngine
import com.example.engine.VoiceTTSEngine
import com.example.ui.components.NeumorphicCard
import com.example.ui.components.NeumorphicColors
import kotlinx.coroutines.launch

data class TransLang(val code: String, val name: String, val nativeName: String)

val SUPPORTED_TRANSLATION_LANGUAGES = listOf(
    TransLang("en", "English", "English"),
    TransLang("hi", "Hindi", "हिन्दी"),
    TransLang("mr", "Marathi", "मराठी"),
    TransLang("bn", "Bengali", "বাংলা"),
    TransLang("ta", "Tamil", "தமிழ்"),
    TransLang("te", "Telugu", "తెలుగు"),
    TransLang("gu", "Gujarati", "ગુજરાતી"),
    TransLang("kn", "Kannada", "ಕನ್ನಡ"),
    TransLang("ml", "Malayalam", "മലയാളം"),
    TransLang("pa", "Punjabi", "ਪੰਜਾਬੀ"),
    TransLang("ur", "Urdu", "اردو"),
    TransLang("es", "Spanish", "Español"),
    TransLang("fr", "French", "Français"),
    TransLang("de", "German", "Deutsch"),
    TransLang("ar", "Arabic", "العربية"),
    TransLang("ja", "Japanese", "日本語")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslateScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val isDarkMode by preferences.isDarkMode.collectAsState()
    val scope = rememberCoroutineScope()

    var sourceText by remember { mutableStateOf("") }
    var translatedText by remember { mutableStateOf("") }
    var isTranslating by remember { mutableStateOf(false) }

    var sourceLang by remember { mutableStateOf(SUPPORTED_TRANSLATION_LANGUAGES[0]) } // English
    var targetLang by remember { mutableStateOf(SUPPORTED_TRANSLATION_LANGUAGES[1]) } // Hindi

    var showSourcePicker by remember { mutableStateOf(false) }
    var showTargetPicker by remember { mutableStateOf(false) }

    val ttsEngine = remember { VoiceTTSEngine(context) }

    val bgColor = if (isDarkMode) NeumorphicColors.DarkScreenBg else NeumorphicColors.LightScreenBg
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted

    val wellBrush = if (isDarkMode) {
        Brush.linearGradient(listOf(NeumorphicColors.DarkWellTop, NeumorphicColors.DarkWellBottom))
    } else {
        Brush.linearGradient(listOf(NeumorphicColors.LightWellTop, NeumorphicColors.LightWellBottom))
    }

    fun performTranslate(text: String) {
        if (text.isBlank()) return
        scope.launch {
            isTranslating = true
            try {
                val result = GoogleTranslationEngine.translate(text, sourceLang.code, targetLang.code)
                translatedText = result
            } catch (e: Exception) {
                translatedText = "Translation failed. Please check internet connection."
            } finally {
                isTranslating = false
            }
        }
    }

    Scaffold(
        containerColor = bgColor,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF059669), Color(0xFF10B981))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column {
                            Text("Instant Translate", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = textColor)
                            Text("Fast Multilingual Translator", fontSize = 11.5.sp, color = textMuted)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 3D Neumorphic Language Selector Bar (Source <-> Swap <-> Target)
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                cornerRadius = 16.dp,
                elevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Source Language Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) Color(0xFF1E202E) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showSourcePicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("FROM", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeumorphicColors.EmeraldAccent)
                                Text(sourceLang.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                                Text(sourceLang.nativeName, fontSize = 11.sp, color = textMuted)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textMuted)
                        }
                    }

                    // Swap Button
                    IconButton(
                        onClick = {
                            val temp = sourceLang
                            sourceLang = targetLang
                            targetLang = temp
                            if (translatedText.isNotBlank() && translatedText != "Translation failed. Please check internet connection.") {
                                val oldTranslated = translatedText
                                translatedText = sourceText
                                sourceText = oldTranslated
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(38.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDarkMode) Color(0xFF064E3B).copy(alpha = 0.4f) else Color(0xFFD1FAE5)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = "Swap Languages",
                                tint = NeumorphicColors.EmeraldAccent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Target Language Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isDarkMode) Color(0xFF1E202E) else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTargetPicker = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("TO", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = NeumorphicColors.EmeraldAccent)
                                Text(targetLang.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = textColor)
                                Text(targetLang.nativeName, fontSize = 11.sp, color = textMuted)
                            }
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = textMuted)
                        }
                    }
                }
            }

            // 2. 3D Input Card (Type or Paste Text)
            NeumorphicCard(
                modifier = Modifier.fillMaxWidth(),
                isDarkMode = isDarkMode,
                cornerRadius = 18.dp,
                elevation = 5.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Enter Text (${sourceLang.name})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textMuted
                        )

                        // Quick Action: Paste from Clipboard
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isDarkMode) Color(0xFF26293B) else Color(0xFFE2E8F0),
                                modifier = Modifier.clickable {
                                    val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val item = clipManager?.primaryClip?.getItemAt(0)
                                    val pasted = item?.text?.toString() ?: ""
                                    if (pasted.isNotBlank()) {
                                        sourceText = pasted
                                        Toast.makeText(context, "Text pasted from clipboard", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Default.ContentPaste, contentDescription = null, tint = NeumorphicColors.EmeraldAccent, modifier = Modifier.size(13.dp))
                                    Text("Paste", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = textColor)
                                }
                            }

                            if (sourceText.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isDarkMode) Color(0xFF3B1A1A) else Color(0xFFFEE2E2),
                                    modifier = Modifier.clickable {
                                        sourceText = ""
                                        translatedText = ""
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(Icons.Default.Clear, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(13.dp))
                                        Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                                    }
                                }
                            }
                        }
                    }

                    // Text Field Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .background(wellBrush, RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0x18000000),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        OutlinedTextField(
                            value = sourceText,
                            onValueChange = { sourceText = it },
                            placeholder = {
                                Text("Type or paste any text to translate...", color = textMuted, fontSize = 14.sp)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Translate Button
                    Button(
                        onClick = { performTranslate(sourceText) },
                        enabled = sourceText.isNotBlank() && !isTranslating,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isTranslating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translating...", color = Color.White, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(Icons.Default.Translate, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Translate Now", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // 3. 3D Output Card (Translated Result & Copy Button)
            AnimatedVisibility(
                visible = translatedText.isNotBlank(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                NeumorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    isDarkMode = isDarkMode,
                    cornerRadius = 18.dp,
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                Text(
                                    "Translated to ${targetLang.name}",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                            }

                            // Speak Audio Button
                            IconButton(
                                onClick = { ttsEngine.speak(translatedText, targetLang.code) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = "Listen", tint = NeumorphicColors.EmeraldAccent, modifier = Modifier.size(20.dp))
                            }
                        }

                        // Display Translated Text Area
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(wellBrush, RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    Color(0xFF10B981).copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(14.dp)
                        ) {
                            Text(
                                text = translatedText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor,
                                lineHeight = 22.sp
                            )
                        }

                        // Bottom Actions: Copy & Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Copy Button
                            Button(
                                onClick = {
                                    val clipManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    val clip = ClipData.newPlainText("Translated Text", translatedText)
                                    clipManager?.setPrimaryClip(clip)
                                    Toast.makeText(context, "✅ Translated text copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDarkMode) Color(0xFF064E3B) else Color(0xFFD1FAE5),
                                    contentColor = if (isDarkMode) Color(0xFF34D399) else Color(0xFF065F46)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Text", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Share Button
                            OutlinedButton(
                                onClick = {
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, translatedText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Translation")
                                    context.startActivity(shareIntent)
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).height(44.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp), tint = textColor)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = textColor)
                            }
                        }
                    }
                }
            }

            // 4. Quick Phrases Bar
            Text(
                "Quick Phrases",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = textMuted
            )

            val quickPhrases = listOf(
                "Hello, how are you?",
                "Thank you very much!",
                "Where are you from?",
                "Nice to meet you.",
                "Can you help me please?",
                "Have a great day!"
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                quickPhrases.chunked(2).forEach { rowPhrases ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPhrases.forEach { phrase ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDarkMode) Color(0xFF1E202E) else Color(0xFFF1F5F9),
                                border = BorderStroke(
                                    1.dp,
                                    if (isDarkMode) Color.White.copy(alpha = 0.08f) else Color(0x18000000)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        sourceText = phrase
                                        performTranslate(phrase)
                                    }
                            ) {
                                Text(
                                    phrase,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = textColor,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Source Language Picker Modal
    if (showSourcePicker) {
        LanguagePickerModal(
            title = "Select Source Language",
            languages = SUPPORTED_TRANSLATION_LANGUAGES,
            selected = sourceLang,
            isDarkMode = isDarkMode,
            onSelect = {
                sourceLang = it
                showSourcePicker = false
                if (sourceText.isNotBlank()) performTranslate(sourceText)
            },
            onDismiss = { showSourcePicker = false }
        )
    }

    // Target Language Picker Modal
    if (showTargetPicker) {
        LanguagePickerModal(
            title = "Select Target Language",
            languages = SUPPORTED_TRANSLATION_LANGUAGES,
            selected = targetLang,
            isDarkMode = isDarkMode,
            onSelect = {
                targetLang = it
                showTargetPicker = false
                if (sourceText.isNotBlank()) performTranslate(sourceText)
            },
            onDismiss = { showTargetPicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LanguagePickerModal(
    title: String,
    languages: List<TransLang>,
    selected: TransLang,
    isDarkMode: Boolean,
    onSelect: (TransLang) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = if (isDarkMode) Color(0xFF181A26) else Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
            )

            languages.forEach { lang ->
                val isSelected = lang.code == selected.code
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) {
                        if (isDarkMode) Color(0xFF064E3B) else Color(0xFFD1FAE5)
                    } else Color.Transparent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(lang) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                lang.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (isDarkMode) Color.White else Color(0xFF0F172A)
                            )
                            Text(
                                lang.nativeName,
                                fontSize = 11.5.sp,
                                color = if (isSelected) NeumorphicColors.EmeraldAccent else Color(0xFF94A3B8)
                            )
                        }

                        if (isSelected) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = NeumorphicColors.EmeraldAccent)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
