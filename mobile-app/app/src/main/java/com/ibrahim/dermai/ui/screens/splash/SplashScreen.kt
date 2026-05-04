package com.ibrahim.dermai.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ibrahim.dermai.R
import com.ibrahim.dermai.ui.theme.SplashGradientCenter
import com.ibrahim.dermai.ui.theme.SplashGradientEdge
import kotlinx.coroutines.delay

/**
 * DermAI Compose Splash Ekranı
 *
 * Animasyonlar:
 *  - Logo 0.8f → 1.0f scale (800ms, ease-out)
 *  - Tagline fade-in (800ms, 300ms gecikme)
 *  - Glow halkaları pulse efekti
 *  - Alt bölümde 3 nokta "yükleniyor" animasyonu
 *
 * 2 saniye sonra [onSplashFinished] callback ile ana ekrana yönlendirir.
 */
@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {

    // ── Animasyon durumları ──
    val logoScale = remember { Animatable(0.8f) }
    val taglineAlpha = remember { Animatable(0f) }

    // Glow pulse efekti
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )

    LaunchedEffect(Unit) {
        // Logo scale animasyonu: 0.8f → 1.0f
        logoScale.animateTo(
            targetValue = 1.0f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
        // Tagline fade-in
        taglineAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
        // Animasyonlar bittikten sonra 1.2 saniye daha kal (toplam ~2.8s)
        delay(1200)
        onSplashFinished()
    }

    // ── UI ──
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        SplashGradientCenter,  // merkez: açık teal
                        SplashGradientEdge     // kenar: marka teal
                    ),
                    radius = 900f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))

            // ── Glow Halkaları + Logo ──
            Box(contentAlignment = Alignment.Center) {
                // Dış halka: 260dp, %7 beyaz
                Box(
                    modifier = Modifier
                        .size(260.dp)
                        .scale(glowScale)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.07f))
                )
                // İç halka: 220dp, %15 beyaz
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .scale(glowScale * 0.98f)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                )
                // Logo: 200dp
                Image(
                    painter = painterResource(id = R.drawable.ic_splash_logo),
                    contentDescription = "DermAI Logo",
                    modifier = Modifier
                        .size(200.dp)
                        .scale(logoScale.value)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Tagline ──
            Text(
                text = "Cilt Analizi ve Karar Destek",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(modifier = Modifier.weight(1f))

            // ── Yükleniyor Noktaları (3 dot pulse) ──
            LoadingDots(
                modifier = Modifier.padding(bottom = 48.dp)
            )
        }
    }
}

/**
 * 3 Noktalı "Yükleniyor" Animasyonu
 * Her nokta sırayla yukarı/aşağı hareket eder.
 */
@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val dotCount = 3
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(dotCount) { index ->
            val offsetY by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = -8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 400,
                        delayMillis = index * 150,
                        easing = FastOutSlowInEasing
                    ),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "dot_$index"
            )
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .offset(y = offsetY.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.8f))
            )
        }
    }
}
