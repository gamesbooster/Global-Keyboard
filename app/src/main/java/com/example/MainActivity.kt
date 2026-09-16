package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ads.AdMobConfig
import com.example.data.LingoKeyPreferences
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var preferences: LingoKeyPreferences
    private var pendingDestination = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        AdMobConfig.initialize(this)
        preferences = LingoKeyPreferences.getInstance(this)
        pendingDestination.value = intent?.getStringExtra("destination")

        setContent {
            val isDarkMode by preferences.isDarkMode.collectAsState()
            val destination by pendingDestination
            MyApplicationTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LingoKeyApp(preferences = preferences, targetDestination = destination)
                }
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val dest = intent.getStringExtra("destination")
        if (dest != null) {
            pendingDestination.value = dest
        }
    }
}

@Composable
fun LingoKeyApp(preferences: LingoKeyPreferences, targetDestination: String? = null) {
    val navController = rememberNavController()

    LaunchedEffect(targetDestination) {
        if (!targetDestination.isNullOrBlank()) {
            navController.navigate(targetDestination)
        }
    }

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(
                preferences = preferences,
                onNavigateNext = { destination ->
                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("onboarding") {
            OnboardingScreen(
                preferences = preferences,
                onComplete = {
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        composable("dashboard") {
            DashboardScreen(
                preferences = preferences,
                onNavigateToLanguages = { navController.navigate("languages") },
                onNavigateToThemes = { navController.navigate("themes") },
                onNavigateToVoiceSettings = { navController.navigate("voice_settings") },
                onNavigateToAISettings = { navController.navigate("ai_settings") },
                onNavigateToPrivacy = { navController.navigate("privacy") },
                onNavigateToPro = { navController.navigate("pro") },
                onRestartOnboarding = { navController.navigate("onboarding") },
                onNavigateToSmartReply = { navController.navigate("smart_reply_settings") },
                onNavigateToSpinAndWin = { navController.navigate("spin_and_win") },
                onNavigateToStore = { navController.navigate("store") },
                onNavigateToTranslate = { navController.navigate("translate") }
            )
        }

        composable("translate") {
            TranslateScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() }
            )
        }

        composable("store") {
            StoreScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() },
                onNavigateToSpinAndWin = { navController.navigate("spin_and_win") },
                onNavigateToPro = { navController.navigate("pro") },
                onNavigateToThemes = { navController.navigate("themes") }
            )
        }

        composable("spin_and_win") {
            SpinAndWinScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() },
                onNavigateToPro = { navController.navigate("pro") }
            )
        }

        composable("languages") {
            LanguagesScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() }
            )
        }

        composable("themes") {
            ThemesScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() },
                onNavigateToPro = { navController.navigate("pro") },
                onNavigateToStore = { navController.navigate("store") },
                onNavigateToSpinAndWin = { navController.navigate("spin_and_win") }
            )
        }

        composable("voice_settings") {
            VoiceSettingsScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() }
            )
        }

        composable("ai_settings") {
            AISettingsScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() },
                onNavigateToPro = { navController.navigate("pro") },
                onNavigateToSmartReply = { navController.navigate("smart_reply_settings") },
                onNavigateToSpinAndWin = { navController.navigate("spin_and_win") }
            )
        }

        composable("smart_reply_settings") {
            SmartReplySettingsScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() },
                onNavigateToPro = { navController.navigate("pro") },
                onNavigateToSpinAndWin = { navController.navigate("spin_and_win") }
            )
        }

        composable("privacy") {
            PrivacyScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() }
            )
        }

        composable("pro") {
            ProScreen(
                preferences = preferences,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

