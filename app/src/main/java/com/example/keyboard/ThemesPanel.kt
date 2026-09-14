package com.example.keyboard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.KeyboardTheme
import com.example.model.ThemeStyle

@Composable
fun ThemesPanel(
    theme: KeyboardTheme,
    onSelectTheme: (KeyboardTheme) -> Unit,
    onOpenFullSettings: () -> Unit,
    onClose: () -> Unit,
    panelHeight: Dp = 268.dp
) {
    var selectedCategory by remember { mutableStateOf("🌟 Master 10") }
    val categories = listOf(
        "🌟 Master 10",
        "🔘 3D Buttons",
        "☁️ Neumorphism",
        "🫧 Glass & Translucent",
        "🎨 Claymorphism",
        "🕹️ Skeuomorphism",
        "⚡ Neobrutalism",
        "📐 Flat & 2.0",
        "All"
    )

    val displayedThemes = remember(selectedCategory) {
        when (selectedCategory) {
            "🌟 Master 10" -> KeyboardTheme.ALL_THEMES.filter { it.category == "Master 10" }
            "🔘 3D Buttons" -> KeyboardTheme.ALL_THEMES.filter { it.themeStyle == ThemeStyle.TACTILE_3D }
            "☁️ Neumorphism" -> KeyboardTheme.ALL_THEMES.filter {
                it.themeStyle == ThemeStyle.NEUMORPHISM_LIGHT ||
                it.themeStyle == ThemeStyle.NEUMORPHISM_DARK
            }
            "🫧 Glass & Translucent" -> KeyboardTheme.ALL_THEMES.filter {
                it.themeStyle == ThemeStyle.SEMI_TRANSPARENT ||
                it.themeStyle == ThemeStyle.GLASSMORPHISM ||
                it.keyAlpha < 1.0f
            }
            "🎨 Claymorphism" -> KeyboardTheme.ALL_THEMES.filter { it.themeStyle == ThemeStyle.CLAYMORPHISM }
            "🕹️ Skeuomorphism" -> KeyboardTheme.ALL_THEMES.filter { it.themeStyle == ThemeStyle.SKEUOMORPHISM }
            "⚡ Neobrutalism" -> KeyboardTheme.ALL_THEMES.filter { it.themeStyle == ThemeStyle.NEOBRUTALISM }
            "📐 Flat & 2.0" -> KeyboardTheme.ALL_THEMES.filter {
                it.themeStyle == ThemeStyle.FLAT_DESIGN ||
                it.themeStyle == ThemeStyle.FLAT_2_0 ||
                it.themeStyle == ThemeStyle.MINIMALISM
            }
            else -> KeyboardTheme.ALL_THEMES
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(panelHeight)
            .background(theme.backgroundColor)
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "THEMES & STYLES",
                        color = theme.textColor,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Tap to apply & set as default",
                        color = theme.textSecondaryColor,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = theme.keyColor,
                    border = BorderStroke(0.8.dp, theme.accentColor.copy(alpha = 0.5f)),
                    modifier = Modifier.clickable { onOpenFullSettings() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Tune,
                            contentDescription = "Builder",
                            tint = theme.accentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "Custom Builder",
                            color = theme.textColor,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

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

        Spacer(modifier = Modifier.height(4.dp))

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isActive = selectedCategory == cat
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isActive) theme.primaryColor else theme.keyColor,
                    border = BorderStroke(
                        0.8.dp,
                        if (isActive) theme.primaryColor else theme.keyBorderColor.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.clickable { selectedCategory = cat }
                ) {
                    Text(
                        text = cat,
                        color = if (isActive) Color.White else theme.textColor,
                        fontSize = 11.sp,
                        fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Themes Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
            contentPadding = PaddingValues(bottom = 4.dp)
        ) {
            items(displayedThemes) { itemTheme ->
                val isSelected = theme.id == itemTheme.id

                val bgMod = if (itemTheme.backgroundGradient != null) {
                    Modifier.background(Brush.verticalGradient(itemTheme.backgroundGradient))
                } else {
                    Modifier.background(itemTheme.backgroundColor)
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = itemTheme.backgroundColor,
                    border = BorderStroke(
                        if (isSelected) 2.dp else 1.dp,
                        if (isSelected) theme.accentColor else theme.keyBorderColor.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(68.dp)
                        .clickable { onSelectTheme(itemTheme) }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(bgMod)
                            .padding(6.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = itemTheme.name,
                                    color = itemTheme.textColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(16.dp)
                                            .clip(CircleShape)
                                            .background(theme.accentColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Check,
                                            contentDescription = "Active",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }

                            // Style Tag + Key preview
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = itemTheme.primaryColor.copy(alpha = 0.25f),
                                    border = BorderStroke(0.5.dp, itemTheme.primaryColor.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = itemTheme.themeStyle.iconEmoji + " " + itemTheme.themeStyle.displayName,
                                        color = itemTheme.textColor,
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }

                                // Micro sample keycap
                                Box(
                                    modifier = Modifier
                                        .size(width = 24.dp, height = 16.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(
                                            if (itemTheme.themeStyle == ThemeStyle.SEMI_TRANSPARENT)
                                                itemTheme.keyColor.copy(alpha = 0.4f)
                                            else itemTheme.keyColor
                                        )
                                        .border(0.6.dp, itemTheme.keyBorderColor, RoundedCornerShape(3.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("A", fontSize = 8.sp, color = itemTheme.textColor, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
