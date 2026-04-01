package com.ibrahim.dermai.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ibrahim.dermai.ui.screens.analysis.AnalysisScreen
import com.ibrahim.dermai.ui.screens.image_selection.ImageSelectionScreen
import com.ibrahim.dermai.ui.screens.result.ResultScreen
import com.ibrahim.dermai.ui.screens.splash.SplashScreen
import java.net.URLDecoder

/**
 * Uygulamanın tüm navigasyon grafiğini yöneten NavHost.
 *
 * Akış: Splash → ImageSelection → Analysis → Result
 *   └─ Result'tan "Yeniden Analiz Et" butonuyla ImageSelection'a dönülebilir
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // Açılış ekranı
        composable(route = Screen.Splash.route) {
            SplashScreen(
                onNavigateToImageSelection = {
                    navController.navigate(Screen.ImageSelection.route) {
                        // Splash'ı geri stack'ten temizle
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Kamera / Galeri seçim ekranı
        composable(route = Screen.ImageSelection.route) {
            ImageSelectionScreen(
                onImageSelected = { imagePath ->
                    navController.navigate(Screen.Analysis.createRoute(imagePath))
                }
            )
        }

        // Analiz ekranı
        composable(
            route = Screen.Analysis.route,
            arguments = listOf(
                navArgument("imagePath") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("imagePath") ?: ""
            val imagePath = URLDecoder.decode(encodedPath, "UTF-8")

            AnalysisScreen(
                imagePath = imagePath,
                onNavigateToResult = { result ->
                    // Analiz sonucunu Result ekranına aktarmak için NavController'a key-value olarak koy
                    navController.currentBackStackEntry?.savedStateHandle?.set("result", result)
                    navController.navigate(Screen.Result.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Sonuç ekranı
        composable(route = Screen.Result.route) {
            val result = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<com.ibrahim.dermai.data.model.AnalysisResponse>("result")

            ResultScreen(
                analysisResult = result,
                onReanalyze = {
                    // Yeniden analiz için ImageSelection'a dön, Result ve Analysis'i temizle
                    navController.navigate(Screen.ImageSelection.route) {
                        popUpTo(Screen.ImageSelection.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
