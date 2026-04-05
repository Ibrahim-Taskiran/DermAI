package com.ibrahim.dermai.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ibrahim.dermai.R
import com.ibrahim.dermai.ui.theme.DermPrimary
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Marka açılış ekranı: tam `ic_launcher_app` görseli, daire içinde kırpılmadan (Fit ölçek).
 */
@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel = hiltViewModel(),
    onNavigateToImageSelection: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    LaunchedEffect(key1 = Unit) {
        viewModel.navigateEvent.collectLatest { dest ->
            if (dest == "home") {
                onNavigateToImageSelection()
            } else {
                onNavigateToOnboarding()
            }
        }
    }

    val logoAlpha = remember { Animatable(0f) }
    val logoScale = remember { Animatable(0.96f) }

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(1f, tween(850, easing = FastOutSlowInEasing))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DermPrimary),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_app),
            contentDescription = "DermAI",
            modifier = Modifier
                .padding(horizontal = 28.dp)
                .widthIn(max = 320.dp)
                .fillMaxWidth()
                .heightIn(max = 400.dp)
                .scale(logoScale.value)
                .alpha(logoAlpha.value),
            contentScale = ContentScale.Fit
        )
    }
}
