package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
