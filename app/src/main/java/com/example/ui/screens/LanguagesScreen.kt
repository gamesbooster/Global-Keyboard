package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LingoKeyPreferences
import com.example.model.Language
import com.example.model.LanguageCategory
import com.example.ui.components.NeumorphicColors

/**
 * Model representing each language entry in the streamlined language list.
 * Supports standard layout and phonetic transliteration variants.
 */
data class LanguageScreenItem(
    val id: String,
    val langId: String,
    val title: String,
    val flagEmoji: String,
    val isPhonetic: Boolean = false,
    val isGlobal: Boolean = false
)

val ALL_LANGUAGE_SCREEN_ITEMS = listOf(
    // 1. Primary & Indic languages with Phonetic options (matching design specification)
    LanguageScreenItem("en", "en", "English", "🇺🇸", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("hi_phonetic", "hi", "Hindi Phonetic/हिन्दी (A → अ)", "🇮🇳", isPhonetic = true, isGlobal = false),
    LanguageScreenItem("hi", "hi", "Hindi/हिन्दी", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("hinglish", "hi", "Hinglish", "🇮🇳", isPhonetic = true, isGlobal = false),
    LanguageScreenItem("bn", "bn", "Bengali/বাংলা", "🇧🇩", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("bn_phonetic", "bn", "Bengali Phonetic/বাংলা (A → অ)", "🇧🇩", isPhonetic = true, isGlobal = false),
    LanguageScreenItem("te", "te", "Telugu/తెలుగు", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("te_phonetic", "te", "Telugu Phonetic/తెలుగు (A → త)", "🇮🇳", isPhonetic = true, isGlobal = false),
    LanguageScreenItem("mr", "mr", "Marathi/मराठी", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("mr_phonetic", "mr", "Marathi Phonetic/मराठी (A → अ)", "🇮🇳", isPhonetic = true, isGlobal = false),
    LanguageScreenItem("ta", "ta", "Tamil/தமிழ்", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("ta_phonetic", "ta", "Tamil Phonetic/தமிழ் (A → அ)", "🇮🇳", isPhonetic = true, isGlobal = false),
    LanguageScreenItem("gu", "gu", "Gujarati/ગુજરાતી", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("gu_phonetic", "gu", "Gujarati Phonetic/ગુજરાતી (A → अ)", "🇮🇳", isPhonetic = true, isGlobal = false),

    // 2. Global & World Languages
    LanguageScreenItem("es", "es", "Spanish/Español", "🇪🇸", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("fr", "fr", "French/Français", "🇫🇷", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("de", "de", "German/Deutsch", "🇩🇪", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("ar", "ar", "Arabic/العربية", "🇸🇦", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("pt", "pt", "Portuguese/Português", "🇧🇷", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("ru", "ru", "Russian/Русский", "🇷🇺", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("it", "it", "Italian/Italiano", "🇮🇹", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("ja", "ja", "Japanese/日本語", "🇯🇵", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("ko", "ko", "Korean/한국어", "🇰🇷", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("zh", "zh", "Chinese/中文 (简体)", "🇨🇳", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("tr", "tr", "Turkish/Türkçe", "🇹🇷", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("id", "id", "Indonesian/Bahasa Indonesia", "🇮🇩", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("vi", "vi", "Vietnamese/Tiếng Việt", "🇻🇳", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("nl", "nl", "Dutch/Nederlands", "🇳🇱", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("pl", "pl", "Polish/Polski", "🇵🇱", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("th", "th", "Thai/ไทย", "🇹🇭", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("fa", "fa", "Persian/فارسی", "🇮🇷", isPhonetic = false, isGlobal = true),
    LanguageScreenItem("fil", "fil", "Filipino/Tagalog", "🇵🇭", isPhonetic = false, isGlobal = true),

    // 3. Other Regional Indian Languages
    LanguageScreenItem("kn", "kn", "Kannada/ಕನ್ನಡ", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("ml", "ml", "Malayalam/മലയാളം", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("pa", "pa", "Punjabi/ਪੰਜਾਬੀ", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("ur", "ur", "Urdu/اردو", "🇵🇰", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("sa", "sa", "Sanskrit/संस्कृतम्", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("ne", "ne", "Nepali/नेपाली", "🇳🇵", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("or", "or", "Odia/ଓଡ଼ିଆ", "🇮🇳", isPhonetic = false, isGlobal = false),
    LanguageScreenItem("as", "as", "Assamese/অসমীয়া", "🇮🇳", isPhonetic = false, isGlobal = false)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguagesScreen(
    preferences: LingoKeyPreferences,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val enabledLanguages by preferences.enabledLanguages.collectAsState()
    val activeLanguage by preferences.activeLanguage.collectAsState()
    val transliterationEnabled by preferences.transliterationEnabled.collectAsState()
    val targetTranslateLang by preferences.targetTranslationLanguage.collectAsState()
    val autoDetectLanguage by preferences.autoDetectLanguage.collectAsState()
    val isDarkMode by preferences.isDarkMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(LanguageCategory.ALL) }
    var showTargetPickerModal by remember { mutableStateOf(false) }

    // Palette: Clean and high contrast, matching both Light & Dark modes
    val screenBg = if (isDarkMode) NeumorphicColors.DarkScreenBg else Color(0xFFF9FAFB)
    val cardBg = if (isDarkMode) Color(0xFF20222B) else Color(0xFFFFFFFF)
    val textColor = if (isDarkMode) Color(0xFFF3F4F6) else Color(0xFF111827)
    val textMuted = if (isDarkMode) Color(0xFF9CA3AF) else Color(0xFF6B7280)
    val borderColor = if (isDarkMode) Color(0xFF2E323E) else Color(0xFFE5E7EB)
    val amberChecked = Color(0xFFEAB308) // Amber/Gold matching exact uploaded screenshot

    // Filter items based on category tabs and search bar
    val filteredItems = remember(searchQuery, selectedCategory) {
        val base = when (selectedCategory) {
            LanguageCategory.ALL -> ALL_LANGUAGE_SCREEN_ITEMS
            LanguageCategory.GLOBAL -> ALL_LANGUAGE_SCREEN_ITEMS.filter { it.isGlobal }
            LanguageCategory.INDIAN -> ALL_LANGUAGE_SCREEN_ITEMS.filter { !it.isGlobal }
        }
        if (searchQuery.isBlank()) {
            base
        } else {
            base.filter { it.title.contains(searchQuery, ignoreCase = true) || it.langId.contains(searchQuery, ignoreCase = true) }
        }
    }

    Scaffold(
        containerColor = screenBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Languages",
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = screenBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(screenBg)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Search Bar (At the top top of screen)
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Search languages or scripts...",
                            fontSize = 13.5.sp,
                            color = textMuted
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = null,
                            tint = textMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = textMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = cardBg,
                        unfocusedContainerColor = cardBg,
                        focusedBorderColor = amberChecked,
                        unfocusedBorderColor = borderColor,
                        focusedTextColor = textColor,
                        unfocusedTextColor = textColor
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                )
            }

            // 2. Category Tabs (All in one single row)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        LanguageCategory.ALL to "All Languages",
                        LanguageCategory.GLOBAL to "Global",
                        LanguageCategory.INDIAN to "Regional (Indian)"
                    )

                    categories.forEach { (cat, label) ->
                        val isSelected = selectedCategory == cat
                        val pillBg = if (isSelected) {
                            if (isDarkMode) Color(0xFF3F3714) else Color(0xFFFEF9C3)
                        } else cardBg
                        val pillBorder = if (isSelected) amberChecked else borderColor
                        val pillTextColor = if (isSelected) {
                            if (isDarkMode) Color(0xFFFDE047) else Color(0xFF854D0E)
                        } else textMuted

                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(20.dp),
                            color = pillBg,
                            border = BorderStroke(1.dp, pillBorder),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 7.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = pillTextColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // 3. Auto-Detect & Translation Target (Under Search and Filter)
            item {
                Surface(
                    color = cardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Auto-Detect row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { preferences.setAutoDetectLanguage(!autoDetectLanguage) }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = if (autoDetectLanguage) amberChecked else textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        "Auto-Detect Language",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = textColor
                                    )
                                    Text(
                                        "Automatically switches script while typing",
                                        fontSize = 11.sp,
                                        color = textMuted
                                    )
                                }
                            }

                            RoundCheckbox(
                                isSelected = autoDetectLanguage,
                                isDarkMode = isDarkMode,
                                amberColor = amberChecked
                            )
                        }

                        HorizontalDivider(color = borderColor, thickness = 0.5.dp)

                        // Target Translation row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTargetPickerModal = true }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Default.Translate,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        "Translation Target",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.5.sp,
                                        color = textColor
                                    )
                                    Text(
                                        "${targetTranslateLang.flagEmoji} ${targetTranslateLang.displayName} (${targetTranslateLang.nativeName})",
                                        fontSize = 11.sp,
                                        color = textMuted
                                    )
                                }
                            }

                            Text(
                                "Change",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF3B82F6),
                                modifier = Modifier.padding(end = 4.dp)
                            )
                        }
                    }
                }
            }

            // 4. Streamlined Language Listing (Ultra-smooth 60-120fps scrolling)
            item {
                Surface(
                    color = cardBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, borderColor),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        filteredItems.forEachIndexed { index, item ->
                            val isSelected = when {
                                item.id == "hinglish" -> activeLanguage.id == "hi" && transliterationEnabled
                                item.isPhonetic -> activeLanguage.id == item.langId && transliterationEnabled
                                else -> activeLanguage.id == item.langId && !transliterationEnabled
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clickable {
                                        val lang = Language.getById(item.langId)
                                        preferences.setActiveLanguage(lang)
                                        preferences.setTransliterationEnabled(item.isPhonetic)

                                        // Ensure enabled
                                        val list = enabledLanguages.toMutableList()
                                        if (!list.any { it.id == lang.id }) {
                                            list.add(lang)
                                            preferences.setEnabledLanguages(list)
                                        }

                                        Toast.makeText(context, "${item.title} selected", Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = item.flagEmoji,
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        text = item.title,
                                        fontSize = 14.sp,
                                        color = textColor,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }

                                RoundCheckbox(
                                    isSelected = isSelected,
                                    isDarkMode = isDarkMode,
                                    amberColor = amberChecked
                                )
                            }

                            if (index < filteredItems.lastIndex) {
                                HorizontalDivider(
                                    color = borderColor,
                                    thickness = 0.5.dp,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Modal Bottom Sheet for Translation Target Language
    if (showTargetPickerModal) {
        ModalBottomSheet(
            onDismissRequest = { showTargetPickerModal = false },
            containerColor = if (isDarkMode) Color(0xFF1E2028) else Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Text(
                    "Select Translation Target",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = textColor
                )
                Text(
                    "Instant translation converts text into this language",
                    fontSize = 12.sp,
                    color = textMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp)
                ) {
                    itemsIndexed(Language.ALL_LANGUAGES) { index, lang ->
                        val isSelected = targetTranslateLang.id == lang.id

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clickable {
                                    preferences.setTargetTranslationLanguage(lang)
                                    showTargetPickerModal = false
                                }
                                .padding(horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(lang.flagEmoji, fontSize = 18.sp)
                                Text(
                                    text = "${lang.displayName} (${lang.nativeName})",
                                    fontSize = 14.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = textColor
                                )
                            }

                            RoundCheckbox(
                                isSelected = isSelected,
                                isDarkMode = isDarkMode,
                                amberColor = amberChecked
                            )
                        }

                        if (index < Language.ALL_LANGUAGES.lastIndex) {
                            HorizontalDivider(color = borderColor, thickness = 0.5.dp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Lightweight, circular checkbox component exactly matching image.png.
 * When selected: Amber filled circle with clean white checkmark.
 * When unselected: Crisp circular outline.
 */
@Composable
private fun RoundCheckbox(
    isSelected: Boolean,
    isDarkMode: Boolean,
    amberColor: Color,
    modifier: Modifier = Modifier
) {
    if (isSelected) {
        Box(
            modifier = modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(amberColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(15.dp)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(22.dp)
                .clip(CircleShape)
                .border(
                    width = 1.4.dp,
                    color = if (isDarkMode) Color(0xFF6B7280) else Color(0xFFCBD5E1),
                    shape = CircleShape
                )
        )
    }
}
