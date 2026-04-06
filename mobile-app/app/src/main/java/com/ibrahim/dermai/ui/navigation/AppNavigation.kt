package com.ibrahim.dermai.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ibrahim.dermai.data.model.AnalysisResponse
import com.ibrahim.dermai.ui.screens.analysis.AnalysisScreen
import com.ibrahim.dermai.ui.screens.bodymap.BodyMapScreen
import com.ibrahim.dermai.ui.screens.camera.CameraScreen
import com.ibrahim.dermai.ui.screens.image_selection.ImageSelectionScreen
import com.ibrahim.dermai.ui.screens.metadata.MetadataFormScreen
import com.ibrahim.dermai.ui.screens.result.ResultScreen
import com.ibrahim.dermai.ui.screens.splash.SplashScreen
import com.ibrahim.dermai.ui.screens.tracker.TrackerScreen
import java.net.URLDecoder

/**
 * Uygulamanın tüm navigasyon grafiğini yöneten NavHost.
 *
 * Akış: Splash → ImageSelection → Camera/Galeri → MetadataForm → BodyMap → Analysis → Result
 *   └─ Result'tan "Günlüğe Kaydet" ile Tracker'a kayıt yapılabilir
 *   └─ ImageSelection'dan "Geçmiş Analizler" ile Tracker'a gidilebilir
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val animDuration = 400

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {

        // ── Açılış ekranı ──
        composable(
            route = Screen.Splash.route,
            exitTransition = { fadeOut(tween(animDuration)) }
        ) {
            SplashScreen(
                onNavigateToImageSelection = {
                    navController.navigate(Screen.ImageSelection.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate(Screen.MetadataForm.createRoute("onboarding")) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

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
                onOpenCamera = {
                    navController.navigate(Screen.Camera.route)
                },
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

        // ── CameraX Kılavuzlu Kamera ekranı ──
        composable(
            route = Screen.Camera.route,
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Up,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    tween(animDuration, easing = FastOutSlowInEasing)
                )
            }
        ) {
            CameraScreen(
                onPhotoCaptured = { imagePath ->
                    navController.navigate(Screen.BodyMap.createRoute(imagePath)) {
                        popUpTo(Screen.Camera.route) { inclusive = true }
                    }
                },
                onNavigateBack = {
                    navController.popBackStack()
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
                gender = com.ibrahim.dermai.data.model.Gender.ERKEK, // Eğer gerekiyorsa ViewModel'den çekeceğiz. Ama artık cinsiyetsiz 3D model var.
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
