package com.example.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemeStyle {
    STANDARD,
    GLASSMORPHISM,
    NEUMORPHISM_LIGHT,
    NEUMORPHISM_DARK,
    FLAT_DESIGN,
    MINIMALISM,
    SKEUOMORPHISM,
    MATERIAL_YOU
}

data class KeyboardTheme(
    val id: String,
    val name: String,
    val isDark: Boolean,
    val primaryColor: Color,
    val backgroundColor: Color,
    val surfaceColor: Color,
    val keyColor: Color,
    val keySpecialColor: Color,
    val keyPressedColor: Color,
    val textColor: Color,
    val textSecondaryColor: Color,
    val accentColor: Color,
    val keyBorderColor: Color,
    val backgroundGradient: List<Color>? = null,
    val keyAlpha: Float = 1.0f,
    val backdropType: String = "solid",
    val themeStyle: ThemeStyle = ThemeStyle.STANDARD,
    val keyCornerRadius: Float = 6f,
    val keyElevation: Float = 1.5f,
    val category: String = "Popular",
    val isPro: Boolean = false
) {
    companion object {
        val MIDNIGHT = KeyboardTheme(
            id = "midnight",
            name = "Immersive Dark",
            isDark = true,
            primaryColor = Color(0xFF6366F1),
            backgroundColor = Color(0xFF16161D),
            surfaceColor = Color(0xFF1C1C24),
            keyColor = Color(0xFF2C2C38),
            keySpecialColor = Color(0xFF3D3D4D),
            keyPressedColor = Color(0xFF4B4B5E),
            textColor = Color(0xFFF1F5F9),
            textSecondaryColor = Color(0xFF94A3B8),
            accentColor = Color(0xFF818CF8),
            keyBorderColor = Color(0x1AFFFFFF),
            backdropType = "solid"
        )

        val COSMIC = KeyboardTheme(
            id = "cosmic",
            name = "Cosmic Nebula",
            isDark = true,
            primaryColor = Color(0xFFA855F7),
            backgroundColor = Color(0xFF0D0B1E),
            surfaceColor = Color(0xFF181533),
            keyColor = Color(0x33382BF0),
            keySpecialColor = Color(0x55581C87),
            keyPressedColor = Color(0x777C3AED),
            textColor = Color(0xFFFAF5FF),
            textSecondaryColor = Color(0xFFD8B4FE),
            accentColor = Color(0xFFC084FC),
            keyBorderColor = Color(0x44A855F7),
            backgroundGradient = listOf(Color(0xFF0F0C29), Color(0xFF302B63), Color(0xFF24243E)),
            keyAlpha = 0.85f,
            backdropType = "cosmic",
            isPro = true
        )

        val SUNSET = KeyboardTheme(
            id = "sunset",
            name = "Sunset Horizon",
            isDark = true,
            primaryColor = Color(0xFFF43F5E),
            backgroundColor = Color(0xFF1C0A1E),
            surfaceColor = Color(0xFF2E1035),
            keyColor = Color(0x38FF4D6D),
            keySpecialColor = Color(0x50BE185D),
            keyPressedColor = Color(0x70E11D48),
            textColor = Color(0xFFFFF1F2),
            textSecondaryColor = Color(0xFFFDA4AF),
            accentColor = Color(0xFFFB7185),
            keyBorderColor = Color(0x44F43F5E),
            backgroundGradient = listOf(Color(0xFF2D112C), Color(0xFF5A1846), Color(0xFF900C3F), Color(0xFFC70039)),
            keyAlpha = 0.85f,
            backdropType = "sunset"
        )

        val AURORA = KeyboardTheme(
            id = "aurora",
            name = "Northern Aurora",
            isDark = true,
            primaryColor = Color(0xFF14B8A6),
            backgroundColor = Color(0xFF041C1A),
            surfaceColor = Color(0xFF082F2C),
            keyColor = Color(0x330D9488),
            keySpecialColor = Color(0x550F766E),
            keyPressedColor = Color(0x77115E59),
            textColor = Color(0xFFF0FDFA),
            textSecondaryColor = Color(0xFF99F6E4),
            accentColor = Color(0xFF2DD4BF),
            keyBorderColor = Color(0x4414B8A6),
            backgroundGradient = listOf(Color(0xFF021B1A), Color(0xFF064E3B), Color(0xFF0F766E), Color(0xFF115E59)),
            keyAlpha = 0.85f,
            backdropType = "aurora"
        )

        val CYBERPUNK = KeyboardTheme(
            id = "cyberpunk",
            name = "Cyberpunk 2077",
            isDark = true,
            primaryColor = Color(0xFFFFEE00),
            backgroundColor = Color(0xFF05050A),
            surfaceColor = Color(0xFF0F0F1A),
            keyColor = Color(0xFF181829),
            keySpecialColor = Color(0xFF26263D),
            keyPressedColor = Color(0xFFFFEE00),
            textColor = Color(0xFF00FFFF),
            textSecondaryColor = Color(0xFFFF007F),
            accentColor = Color(0xFFFFEE00),
            keyBorderColor = Color(0x6600FFFF),
            backgroundGradient = listOf(Color(0xFF0B0014), Color(0xFF1D002C), Color(0xFF080D21)),
            keyAlpha = 0.9f,
            backdropType = "cyberpunk"
        )

        val EMERALD = KeyboardTheme(
            id = "emerald",
            name = "Emerald Neon",
            isDark = true,
            primaryColor = Color(0xFF10B981),
            backgroundColor = Color(0xFF061A14),
            surfaceColor = Color(0xFF0D2820),
            keyColor = Color(0xFF133B2F),
            keySpecialColor = Color(0xFF1D5242),
            keyPressedColor = Color(0xFF266854),
            textColor = Color(0xFFECFDF5),
            textSecondaryColor = Color(0xFF6EE7B7),
            accentColor = Color(0xFF34D399),
            keyBorderColor = Color(0x33059669),
            backdropType = "solid"
        )

        val OCEAN = KeyboardTheme(
            id = "ocean",
            name = "Deep Ocean",
            isDark = true,
            primaryColor = Color(0xFF0284C7),
            backgroundColor = Color(0xFF081426),
            surfaceColor = Color(0xFF0F243E),
            keyColor = Color(0xFF16375C),
            keySpecialColor = Color(0xFF1E4878),
            keyPressedColor = Color(0xFF275C96),
            textColor = Color(0xFFF0F9FF),
            textSecondaryColor = Color(0xFF7DD3FC),
            accentColor = Color(0xFF38BDF8),
            keyBorderColor = Color(0x330284C7),
            backdropType = "solid"
        )

        val PURPLE = KeyboardTheme(
            id = "purple",
            name = "Cyber Violet",
            isDark = true,
            primaryColor = Color(0xFF9333EA),
            backgroundColor = Color(0xFF130826),
            surfaceColor = Color(0xFF220F42),
            keyColor = Color(0xFF341763),
            keySpecialColor = Color(0xFF4A238A),
            keyPressedColor = Color(0xFF6232B3),
            textColor = Color(0xFFFAF5FF),
            textSecondaryColor = Color(0xFFD8B4FE),
            accentColor = Color(0xFFC084FC),
            keyBorderColor = Color(0x339333EA),
            backdropType = "solid"
        )

        val SAKURA = KeyboardTheme(
            id = "sakura",
            name = "Sakura Blossom",
            isDark = false,
            primaryColor = Color(0xFFE11D48),
            backgroundColor = Color(0xFFFFF1F2),
            surfaceColor = Color(0xFFFFE4E6),
            keyColor = Color(0xFFFFFFFF),
            keySpecialColor = Color(0xFFFECDD3),
            keyPressedColor = Color(0xFFFDA4AF),
            textColor = Color(0xFF881337),
            textSecondaryColor = Color(0xFF9F1239),
            accentColor = Color(0xFFF43F5E),
            keyBorderColor = Color(0x33FB7185),
            backgroundGradient = listOf(Color(0xFFFFF0F5), Color(0xFFFFDEE9), Color(0xFFB5FFFC)),
            keyAlpha = 0.92f,
            backdropType = "sakura"
        )

        val CARBON_GOLD = KeyboardTheme(
            id = "carbon_gold",
            name = "Carbon Luxury Gold",
            isDark = true,
            primaryColor = Color(0xFFEAB308),
            backgroundColor = Color(0xFF121212),
            surfaceColor = Color(0xFF1E1E1E),
            keyColor = Color(0xFF282828),
            keySpecialColor = Color(0xFF333333),
            keyPressedColor = Color(0xFF444444),
            textColor = Color(0xFFFEF08A),
            textSecondaryColor = Color(0xFFCA8A04),
            accentColor = Color(0xFFFACC15),
            keyBorderColor = Color(0x44EAB308),
            backdropType = "solid",
            isPro = true
        )

        val MINIMAL_LIGHT = KeyboardTheme(
            id = "minimal_light",
            name = "Minimal Light",
            isDark = false,
            primaryColor = Color(0xFF4F46E5),
            backgroundColor = Color(0xFFF8FAFC),
            surfaceColor = Color(0xFFFFFFFF),
            keyColor = Color(0xFFFFFFFF),
            keySpecialColor = Color(0xFFE2E8F0),
            keyPressedColor = Color(0xFFCBD5E1),
            textColor = Color(0xFF0F172A),
            textSecondaryColor = Color(0xFF64748B),
            accentColor = Color(0xFF4F46E5),
            keyBorderColor = Color(0x22CBD5E1),
            backdropType = "solid"
        )

        val AMOLED = KeyboardTheme(
            id = "amoled",
            name = "Pure AMOLED",
            isDark = true,
            primaryColor = Color(0xFF38BDF8),
            backgroundColor = Color(0xFF000000),
            surfaceColor = Color(0xFF0A0A0A),
            keyColor = Color(0xFF171717),
            keySpecialColor = Color(0xFF262626),
            keyPressedColor = Color(0xFF404040),
            textColor = Color(0xFFFFFFFF),
            textSecondaryColor = Color(0xFFA3A3A3),
            accentColor = Color(0xFF22D3EE),
            keyBorderColor = Color(0x3338BDF8),
            backdropType = "solid",
            isPro = true
        )

        val GLASSMORPHISM = KeyboardTheme(
            id = "glassmorphism",
            name = "Frosted Glassmorphism",
            isDark = true,
            primaryColor = Color(0xFF38BDF8),
            backgroundColor = Color(0xFF0B0F19),
            surfaceColor = Color(0xFF161E31),
            keyColor = Color(0x35FFFFFF),
            keySpecialColor = Color(0x48FFFFFF),
            keyPressedColor = Color(0x8038BDF8),
            textColor = Color(0xFFFFFFFF),
            textSecondaryColor = Color(0xCCBAE6FD),
            accentColor = Color(0xFF38BDF8),
            keyBorderColor = Color(0x70FFFFFF),
            backgroundGradient = listOf(Color(0xFF090D1A), Color(0xFF1E1B4B), Color(0xFF162544), Color(0xFF0F172A)),
            keyAlpha = 0.85f,
            backdropType = "glassmorphism",
            themeStyle = ThemeStyle.GLASSMORPHISM,
            keyCornerRadius = 8f,
            keyElevation = 0f,
            category = "UI Trends",
            isPro = true
        )

        val NEUMORPHISM_LIGHT = KeyboardTheme(
            id = "neumorphism_light",
            name = "Soft Neumorphism Light",
            isDark = false,
            primaryColor = Color(0xFF3B82F6),
            backgroundColor = Color(0xFFE2E8F0),
            surfaceColor = Color(0xFFE2E8F0),
            keyColor = Color(0xFFE2E8F0),
            keySpecialColor = Color(0xFFD6DFEB),
            keyPressedColor = Color(0xFFCBD5E1),
            textColor = Color(0xFF1E293B),
            textSecondaryColor = Color(0xFF64748B),
            accentColor = Color(0xFF2563EB),
            keyBorderColor = Color(0xFFCBD5E1),
            backgroundGradient = listOf(Color(0xFFE8EEF5), Color(0xFFDFE6F0)),
            keyAlpha = 1.0f,
            backdropType = "neumorphism_light",
            themeStyle = ThemeStyle.NEUMORPHISM_LIGHT,
            keyCornerRadius = 8f,
            keyElevation = 3f,
            category = "UI Trends"
        )

        val NEUMORPHISM_DARK = KeyboardTheme(
            id = "neumorphism_dark",
            name = "Soft Neumorphism Dark",
            isDark = true,
            primaryColor = Color(0xFF10B981),
            backgroundColor = Color(0xFF1E2227),
            surfaceColor = Color(0xFF1E2227),
            keyColor = Color(0xFF23282E),
            keySpecialColor = Color(0xFF2B3138),
            keyPressedColor = Color(0xFF181B1E),
            textColor = Color(0xFFF1F5F9),
            textSecondaryColor = Color(0xFF94A3B8),
            accentColor = Color(0xFF34D399),
            keyBorderColor = Color(0xFF2E343D),
            backgroundGradient = listOf(Color(0xFF23282E), Color(0xFF191C20)),
            keyAlpha = 1.0f,
            backdropType = "neumorphism_dark",
            themeStyle = ThemeStyle.NEUMORPHISM_DARK,
            keyCornerRadius = 8f,
            keyElevation = 3f,
            category = "UI Trends",
            isPro = true
        )

        val FLAT_DESIGN = KeyboardTheme(
            id = "flat_design",
            name = "Modern Flat Design",
            isDark = true,
            primaryColor = Color(0xFF2563EB),
            backgroundColor = Color(0xFF0F172A),
            surfaceColor = Color(0xFF1E293B),
            keyColor = Color(0xFF1E293B),
            keySpecialColor = Color(0xFF334155),
            keyPressedColor = Color(0xFF475569),
            textColor = Color(0xFFF8FAFC),
            textSecondaryColor = Color(0xFF94A3B8),
            accentColor = Color(0xFFF97316),
            keyBorderColor = Color(0xFF334155),
            keyAlpha = 1.0f,
            backdropType = "solid",
            themeStyle = ThemeStyle.FLAT_DESIGN,
            keyCornerRadius = 4f,
            keyElevation = 0f,
            category = "UI Trends"
        )

        val MINIMALISM = KeyboardTheme(
            id = "minimalism",
            name = "Minimalist Editorial",
            isDark = true,
            primaryColor = Color(0xFFFFFFFF),
            backgroundColor = Color(0xFF09090B),
            surfaceColor = Color(0xFF121215),
            keyColor = Color(0x18FFFFFF),
            keySpecialColor = Color(0x28FFFFFF),
            keyPressedColor = Color(0x40FFFFFF),
            textColor = Color(0xFFFAFAFA),
            textSecondaryColor = Color(0xFF71717A),
            accentColor = Color(0xFFE4E4E7),
            keyBorderColor = Color(0x1AFFFFFF),
            keyAlpha = 1.0f,
            backdropType = "solid",
            themeStyle = ThemeStyle.MINIMALISM,
            keyCornerRadius = 5f,
            keyElevation = 0f,
            category = "UI Trends"
        )

        val SKEUOMORPHISM = KeyboardTheme(
            id = "skeuomorphism",
            name = "Tactile 3D Skeuomorphic",
            isDark = true,
            primaryColor = Color(0xFF3B82F6),
            backgroundColor = Color(0xFF181A1E),
            surfaceColor = Color(0xFF22252B),
            keyColor = Color(0xFF2D323A),
            keySpecialColor = Color(0xFF383E47),
            keyPressedColor = Color(0xFF202329),
            textColor = Color(0xFFF8FAFC),
            textSecondaryColor = Color(0xFFA0ABBA),
            accentColor = Color(0xFF60A5FA),
            keyBorderColor = Color(0xFF131518),
            backgroundGradient = listOf(Color(0xFF23272E), Color(0xFF141619)),
            keyAlpha = 1.0f,
            backdropType = "skeuomorphism",
            themeStyle = ThemeStyle.SKEUOMORPHISM,
            keyCornerRadius = 6f,
            keyElevation = 4f,
            category = "UI Trends",
            isPro = true
        )

        val MATERIAL_YOU = KeyboardTheme(
            id = "material_you",
            name = "Gboard Material You",
            isDark = true,
            primaryColor = Color(0xFFA8C7FA),
            backgroundColor = Color(0xFF191C1E),
            surfaceColor = Color(0xFF222528),
            keyColor = Color(0xFF2D3135),
            keySpecialColor = Color(0xFF373B40),
            keyPressedColor = Color(0xFF45494F),
            textColor = Color(0xFFE2E2E6),
            textSecondaryColor = Color(0xFFC4C7C5),
            accentColor = Color(0xFFA8C7FA),
            keyBorderColor = Color(0x1FFFFFFF),
            keyAlpha = 1.0f,
            backdropType = "solid",
            themeStyle = ThemeStyle.MATERIAL_YOU,
            keyCornerRadius = 6f,
            keyElevation = 1.5f,
            category = "UI Trends"
        )

        val ALL_THEMES = listOf(
            GLASSMORPHISM,
            NEUMORPHISM_LIGHT,
            NEUMORPHISM_DARK,
            FLAT_DESIGN,
            MINIMALISM,
            SKEUOMORPHISM,
            MATERIAL_YOU,
            MIDNIGHT,
            COSMIC,
            SUNSET,
            AURORA,
            CYBERPUNK,
            SAKURA,
            CARBON_GOLD,
            EMERALD,
            OCEAN,
            PURPLE,
            MINIMAL_LIGHT,
            AMOLED
        )

        fun getById(id: String): KeyboardTheme {
            return ALL_THEMES.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MIDNIGHT
        }
    }
}
