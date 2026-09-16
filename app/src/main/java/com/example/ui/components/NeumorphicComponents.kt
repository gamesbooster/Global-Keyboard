package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Authentic 3D Neumorphism Design System for Light & Dark Modes.
 * Replicates the extruded physical surface with dual shadows (bright top-left highlight & deep bottom-right shadow),
 * convex/concave gradients, and beveled 3D cut rims.
 */
object NeumorphicColors {
    // Light Mode (Warm Ivory / Stone Clay matching sample image 1)
    val LightScreenBg = Color(0xFFEDE9E1)
    val LightCardTop = Color(0xFFF7F4EE)
    val LightCardBottom = Color(0xFFE5E0D6)
    val LightShadowDark = Color(0x35403426)
    val LightShadowLight = Color(0xD8FFFFFF)
    val LightTextPrimary = Color(0xFF1E2024)
    val LightTextMuted = Color(0xFF6B6E76)
    val LightWellTop = Color(0xFFDCD6CA)
    val LightWellBottom = Color(0xFFF3EFE7)

    // Dark Mode (Deep Charcoal / Obsidian matching sample image 2)
    val DarkScreenBg = Color(0xFF191A1E)
    val DarkCardTop = Color(0xFF262830)
    val DarkCardBottom = Color(0xFF1A1B20)
    val DarkShadowDark = Color(0xCC000000)
    val DarkShadowLight = Color(0x22FFFFFF)
    val DarkTextPrimary = Color(0xFFF1F3F7)
    val DarkTextMuted = Color(0xFF9195A2)
    val DarkWellTop = Color(0xFF131418)
    val DarkWellBottom = Color(0xFF23252E)

    // Vibrant Accent
    val EmeraldAccent = Color(0xFF10B981)
    val EmeraldAccentDark = Color(0xFF059669)
}

/**
 * 3D Real Neumorphic Extruded Container.
 * Renders authentic dual-source lighting with soft depth and 3D beveled rim.
 */
@Composable
fun NeumorphicCard(
    modifier: Modifier = Modifier,
    isDarkMode: Boolean = false,
    cornerRadius: Dp = 18.dp,
    elevation: Dp = 8.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val cardShape = RoundedCornerShape(cornerRadius)

    val surfaceBrush = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(NeumorphicColors.DarkCardTop, NeumorphicColors.DarkCardBottom),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(NeumorphicColors.LightCardTop, NeumorphicColors.LightCardBottom),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    val rimBorder = if (isDarkMode) {
        BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.18f),
                    Color.White.copy(alpha = 0.04f),
                    Color.Black.copy(alpha = 0.6f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        )
    } else {
        BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color.White.copy(alpha = 0.3f),
                    Color(0x28000000)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        )
    }

    val shadowColor = if (isDarkMode) NeumorphicColors.DarkShadowDark else NeumorphicColors.LightShadowDark
    val highlightColor = if (isDarkMode) NeumorphicColors.DarkShadowLight else NeumorphicColors.LightShadowLight

    Box(
        modifier = modifier
            // 1. Top-Left Diffused Light Glow
            .padding(2.dp)
            .shadow(
                elevation = elevation / 2,
                shape = cardShape,
                ambientColor = highlightColor,
                spotColor = highlightColor
            )
            // 2. Bottom-Right Deep 3D Shadow
            .shadow(
                elevation = elevation,
                shape = cardShape,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(cardShape)
            .background(surfaceBrush)
            .border(rimBorder, cardShape)
            .then(
                if (onClick != null) Modifier.clickable { onClick() } else Modifier
            ),
        content = content
    )
}

/**
 * 3D Real Neumorphic Dashboard Feature Card.
 * Maintains EXACT size and dimensions as the user's previous layout,
 * while applying the full 3D Neumorphism UI/UX requested.
 */
@Composable
fun NeumorphicFeatureCard(
    icon: ImageVector,
    title: String,
    desc: String,
    isDarkMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted

    // Inset Well Brush (gives the recessed/carved effect for the icon)
    val wellBrush = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(NeumorphicColors.DarkWellTop, NeumorphicColors.DarkWellBottom),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(NeumorphicColors.LightWellTop, NeumorphicColors.LightWellBottom),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    val wellBorder = if (isDarkMode) {
        BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(Color.Black.copy(alpha = 0.6f), Color.White.copy(alpha = 0.10f)),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        )
    } else {
        BorderStroke(
            1.dp,
            Brush.linearGradient(
                colors = listOf(Color(0x2A000000), Color.White.copy(alpha = 0.85f)),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        )
    }

    NeumorphicCard(
        modifier = modifier.fillMaxWidth(),
        isDarkMode = isDarkMode,
        cornerRadius = 18.dp,
        elevation = 7.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 3D Recessed Neumorphic Icon Well
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(wellBrush)
                    .border(wellBorder, RoundedCornerShape(13.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = NeumorphicColors.EmeraldAccent,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Text Titles
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 13.5.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = textMuted,
                    maxLines = 1
                )
            }

            // Subtle 3D indicator chevron
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = textMuted.copy(alpha = 0.5f),
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

/**
 * 3D Neumorphic Compact Top Header.
 * Solves the high vertical height issue by providing a slim, compact 3D card
 * with proper statusBarsPadding() so the device status bar (time, battery %, notifications, wifi/LTE)
 * is fully and crisply visible above the card with comfortable margin.
 */
@Composable
fun NeumorphicTopHeader(
    activeLanguageName: String,
    isPremiumUser: Boolean,
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onOpenVip: () -> Unit,
    onOpenHelp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = if (isDarkMode) NeumorphicColors.DarkTextPrimary else NeumorphicColors.LightTextPrimary
    val textMuted = if (isDarkMode) NeumorphicColors.DarkTextMuted else NeumorphicColors.LightTextMuted

    val wellBrush = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(NeumorphicColors.DarkWellTop, NeumorphicColors.DarkWellBottom),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(NeumorphicColors.LightWellTop, NeumorphicColors.LightWellBottom),
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
        )
    }

    val wellBorder = if (isDarkMode) {
        BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(Color.Black.copy(alpha = 0.6f), Color.White.copy(alpha = 0.10f))
            )
        )
    } else {
        BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(Color(0x2A000000), Color.White.copy(alpha = 0.85f))
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 14.dp, vertical = 4.dp)
    ) {
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            isDarkMode = isDarkMode,
            cornerRadius = 16.dp,
            elevation = 5.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: 3D Logo & App Branding (Compact)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 3D Embossed Logo
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF059669), Color(0xFF10B981), Color(0xFF34D399))
                                )
                            )
                            .border(
                                1.dp,
                                Brush.linearGradient(
                                    listOf(Color.White.copy(alpha = 0.6f), Color.Transparent)
                                ),
                                RoundedCornerShape(9.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "म",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Column {
                        Text(
                            "Global Keyboard Dynamic",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = textColor,
                            maxLines = 1
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Text(
                                "$activeLanguageName • Active",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF10B981),
                                maxLines = 1
                            )
                            if (isPremiumUser) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFF59E0B).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "VIP",
                                        color = Color(0xFFF59E0B),
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Right: 3D Compact Action Buttons (Dark/Light, VIP, Help)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Theme Switcher Button
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(wellBrush)
                            .border(wellBorder, RoundedCornerShape(8.dp))
                            .clickable { onToggleDarkMode() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else Color(0xFF059669),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // VIP / Crown Button
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(wellBrush)
                            .border(wellBorder, RoundedCornerShape(8.dp))
                            .clickable { onOpenVip() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = "VIP Pro Features",
                            tint = if (isPremiumUser) Color(0xFFF59E0B) else Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Help / Tutorial Button
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(wellBrush)
                            .border(wellBorder, RoundedCornerShape(8.dp))
                            .clickable { onOpenHelp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                            contentDescription = "Help & Tutorial",
                            tint = textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3D Neumorphic Bottom Navigation Bar.
 * Built with full 3D physical depth, extruded container, and recessed/elevated tabs:
 * Tab 0: Home (Replaces Layout)
 * Tab 1: VIP Pro (Prominently displays Pro features and subscription pricing)
 * Tab 2: Settings
 */
@Composable
fun NeumorphicBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        Triple(0, "Home", Icons.Default.Home),
        Triple(1, "VIP Pro", Icons.Default.WorkspacePremium),
        Triple(2, "Settings", Icons.Default.Settings)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        NeumorphicCard(
            modifier = Modifier.fillMaxWidth(),
            isDarkMode = isDarkMode,
            cornerRadius = 20.dp,
            elevation = 7.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { (index, label, icon) ->
                    val isSelected = selectedTab == index

                    val wellBrush = if (isDarkMode) {
                        Brush.linearGradient(
                            listOf(NeumorphicColors.DarkWellTop, NeumorphicColors.DarkWellBottom)
                        )
                    } else {
                        Brush.linearGradient(
                            listOf(NeumorphicColors.LightWellTop, NeumorphicColors.LightWellBottom)
                        )
                    }

                    val wellBorder = if (isDarkMode) {
                        BorderStroke(
                            1.dp,
                            if (isSelected) {
                                if (index == 1) Color(0xFFF59E0B) else Color(0xFF10B981)
                            } else {
                                Color.White.copy(alpha = 0.08f)
                            }
                        )
                    } else {
                        BorderStroke(
                            1.dp,
                            if (isSelected) {
                                if (index == 1) Color(0xFFF59E0B) else Color(0xFF10B981)
                            } else {
                                Color(0x20000000)
                            }
                        )
                    }

                    val tabColor = when {
                        isSelected && index == 1 -> Color(0xFFF59E0B)
                        isSelected -> Color(0xFF10B981)
                        isDarkMode -> NeumorphicColors.DarkTextMuted
                        else -> NeumorphicColors.LightTextMuted
                    }

                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.04f else 1.0f,
                        label = "tab_scale"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .scale(scale)
                            .clip(RoundedCornerShape(14.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .background(wellBrush)
                                        .border(wellBorder, RoundedCornerShape(14.dp))
                                } else {
                                    Modifier
                                }
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                onTabSelected(index)
                            }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = tabColor,
                                modifier = Modifier.size(if (isSelected) 21.dp else 19.dp)
                            )
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = tabColor
                            )
                        }
                    }
                }
            }
        }
    }
}
