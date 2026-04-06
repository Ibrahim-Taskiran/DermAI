package com.ibrahim.dermai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.ibrahim.dermai.ui.navigation.AppNavigation
import com.ibrahim.dermai.ui.theme.DermAITheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Uygulamanın tek Activity'si.
 * @AndroidEntryPoint ile Hilt'in inject mekanizması bu Activity için aktif edilir.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DermAITheme {
                // Tüm navigasyon AppNavigation'dan yönetiliyor
                AppNavigation()
            }
        }
    }
}
