package com.ibrahim.dermai.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ibrahim.dermai.data.model.AnalysisResponse
import com.ibrahim.dermai.ui.screens.analysis.AnalysisScreen
import com.ibrahim.dermai.ui.screens.bodymap.BodyMapScreen
import com.ibrahim.dermai.ui.screens.image_selection.ImageSelectionScreen
import com.ibrahim.dermai.ui.screens.metadata.MetadataFormScreen
import com.ibrahim.dermai.ui.screens.result.ResultScreen
import com.ibrahim.dermai.ui.screens.tracker.TrackerScreen
import java.net.URLDecoder

/**
 * Uygulamanın tüm navigasyon grafiğini yöneten NavHost.
 *
 * Akış: ImageSelection → Camera/Galeri → MetadataForm → BodyMap → Analysis → Result
 *   └─ İlk açılışta profil yoksa → MetadataForm (onboarding) → ImageSelection
 *   └─ Result'tan "Günlüğe Kaydet" ile Tracker'a kayıt yapılabilir
 *   └─ ImageSelection'dan "Geçmiş Analizler" ile Tracker'a gidilebilir
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val animDuration = 400

    // Profil kontrolü: ilk kez giriş yapan kullanıcıyı onboarding'e yönlendir
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("dermai_user_profile", android.content.Context.MODE_PRIVATE)
    val hasProfile = prefs.getString("patient_metadata", null) != null
    val startRoute = if (hasProfile) Screen.ImageSelection.route else Screen.MetadataForm.createRoute("onboarding")

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {

        // ── Görsel Seçim ekranı ──
        composable(
            route = Screen.ImageSelection.route,
            enterTransition = { fadeIn(tween(animDuration)) },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            }
        ) {
            ImageSelectionScreen(
                onImageSelectedFromGallery = { imagePath ->
                    navController.navigate(Screen.BodyMap.createRoute(imagePath))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.MetadataForm.createRoute("settings"))
                },
                onOpenTracker = {
                    navController.navigate(Screen.Tracker.route)
                }
            )
        }


        // ── Metadata Form ekranı ──
        composable(
            route = Screen.MetadataForm.route,
            arguments = listOf(
                navArgument("mode") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            }
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "onboarding"

            MetadataFormScreen(
                mode = mode,
                onComplete = {
                    if (mode == "onboarding") {
                        navController.navigate(Screen.ImageSelection.route) {
                            popUpTo(Screen.MetadataForm.route) { inclusive = true }
                        }
                    } else {
                        // Settings modunda ise geri dön
                        navController.popBackStack()
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Vücut Haritası ekranı ──
        composable(
            route = Screen.BodyMap.route,
            arguments = listOf(
                navArgument("imagePath") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            }
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("imagePath") ?: ""
            val imagePath = URLDecoder.decode(encodedPath, "UTF-8")

            // Metadata artık repository'den çekiliyor, Screen arası taşımaya gerek yok (isteğe bağlı)
            // Ama BodyMap'e geçiriyorduk. Artık gerekmez çünkü BodyMap veya Analysis ViewModel'i direkt UserProfileRepository'den o anki veriyi alabilir.
            BodyMapScreen(
                // Cinsiyetsiz 3D model kullanılıyor, gender parametresi kaldırıldı
                onContinue = { bodyRegion ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("bodyRegion", bodyRegion)
                    navController.navigate(Screen.Analysis.createRoute(imagePath))
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Analiz ekranı ──
        composable(
            route = Screen.Analysis.route,
            arguments = listOf(
                navArgument("imagePath") { type = NavType.StringType }
            ),
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            }
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString("imagePath") ?: ""
            val imagePath = URLDecoder.decode(encodedPath, "UTF-8")

            val bodyRegion = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("bodyRegion") ?: ""

            AnalysisScreen(
                imagePath = imagePath,
                onNavigateToResult = { result ->
                    navController.currentBackStackEntry?.savedStateHandle?.set("result", result)
                    navController.currentBackStackEntry?.savedStateHandle?.set("bodyRegion", bodyRegion)
                    navController.currentBackStackEntry?.savedStateHandle?.set("imagePath", imagePath)
                    navController.navigate(Screen.Result.route)
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // ── Sonuç ekranı ──
        composable(
            route = Screen.Result.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(500, easing = FastOutSlowInEasing)
                ) + fadeIn(tween(300))
            },
            exitTransition = { fadeOut(tween(animDuration)) }
        ) {
            val result = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<AnalysisResponse>("result")
            val bodyRegion = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("bodyRegion") ?: ""
            val imagePath = navController.previousBackStackEntry
                ?.savedStateHandle
                ?.get<String>("imagePath") ?: ""

            ResultScreen(
                analysisResult = result,
                imagePath = imagePath,
                bodyRegion = bodyRegion,
                onReanalyze = {
                    navController.navigate(Screen.ImageSelection.route) {
                        popUpTo(Screen.ImageSelection.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Hastalık Takip Günlüğü ekranı ──
        composable(
            route = Screen.Tracker.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            }
        ) {
            TrackerScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
