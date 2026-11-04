package com.upb.petcare

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.upb.petcare.data.ThemeMode
import com.upb.petcare.ui.navigation.PetCareNavHost
import com.upb.petcare.ui.theme.PetCareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as PetCareApp).container
        setContent {
            val themeMode by container.settings.themeMode.collectAsStateWithLifecycle(ThemeMode.SYSTEM)
            val dark = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            PetCareTheme(darkTheme = dark) {
                // Los íconos de la barra de estado se ajustan a cada pantalla (ver StatusBarStyle)
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightNavigationBars = !dark
                }
                PetCareNavHost(container.settings, dark)
            }
        }
    }
}
