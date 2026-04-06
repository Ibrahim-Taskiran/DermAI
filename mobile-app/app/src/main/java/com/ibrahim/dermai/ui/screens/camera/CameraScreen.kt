package com.ibrahim.dermai.ui.screens.camera

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.ibrahim.dermai.ui.theme.DermPrimary
import kotlinx.coroutines.delay
import java.io.File
import java.util.concurrent.Executors

/**
 * CameraX ile kılavuzlu fotoğraf çekme ekranı.
 * Ortam ışığı algılama, overlay kılavuz ve ipuçları içerir.
 */
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    viewModel: CameraViewModel = hiltViewModel(),
    onPhotoCaptured: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val flashEnabled by viewModel.flashEnabled.collectAsState()
    val isLowLight by viewModel.isLowLight.collectAsState()

    val previewView = remember { PreviewView(context) }
    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    // İpucu metinleri
    val tips = listOf(
        "Cilt lezyonunu çerçevenin içine hizalayın",
        "Fotoğraf alanındaki saçları veya kılları temizleyin",
        "Doğal ışık tercih edin, gölge olmasın",
        "Cildi yakından ve net çekin",
        "Kamerayı sabit tutun, bulanıklık olmasın"
    )
    var currentTipIndex by remember { mutableIntStateOf(0) }

    // İpuçlarını döngüsel göster
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            currentTipIndex = (currentTipIndex + 1) % tips.size
        }
    }

    // Kamera başlatma
    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    LaunchedEffect(Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            cameraProvider = providerFuture.get()
        }, ContextCompat.getMainExecutor(context))
    }

    // Kamera bağlama
    LaunchedEffect(cameraProvider, flashEnabled) {
        cameraProvider?.let { provider ->
            provider.unbindAll()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Ortam ışığı: düşük çözünürlük + örneklenmiş luma (kare başına ByteArray/map yok → GC azalır)
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setTargetResolution(android.util.Size(320, 240))
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        try {
                            val luma = computeSampledAverageLuma(imageProxy)
                            viewModel.updateLightLevel(luma < 40.0)
                        } finally {
                            imageProxy.close()
                        }
                    }
                }

            imageCapture.flashMode = if (flashEnabled) {
                ImageCapture.FLASH_MODE_ON
            } else {
                ImageCapture.FLASH_MODE_OFF
            }

            try {
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture,
                    imageAnalysis
                )
            } catch (_: Exception) { }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            cameraProvider?.unbindAll()
        }
    }

    // Pulsing animasyonu (kılavuz çerçeve)
    val infiniteTransition = rememberInfiniteTransition(label = "guide")
    val guideAlpha = infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "guideAlpha"
    )

    // ── UI ──
    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Kamera önizleme
        AndroidView(
            factory = {
                previewView.apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay: Yarı karanlık arka plan + oval kılavuz
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    // Yarı saydam karanlık overlay
                    drawRect(Color.Black.copy(alpha = 0.4f))

                    // Ortada oval "pencere" aç
                    val ovalWidth = size.width * 0.7f
                    val ovalHeight = size.height * 0.38f
                    val ovalLeft = (size.width - ovalWidth) / 2
                    val ovalTop = (size.height - ovalHeight) / 2 - size.height * 0.05f

                    drawOval(
                        color = Color.Transparent,
                        topLeft = Offset(ovalLeft, ovalTop),
                        size = Size(ovalWidth, ovalHeight),
                        blendMode = BlendMode.Clear
                    )

                    // Kılavuz oval çerçevesi
                    drawOval(
                        color = Color.White.copy(alpha = guideAlpha.value),
                        topLeft = Offset(ovalLeft, ovalTop),
                        size = Size(ovalWidth, ovalHeight),
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
        )

        // Kılavuz metni
        Text(
            text = "Lezyonu buraya hizalayın",
            color = Color.White.copy(alpha = guideAlpha.value),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 220.dp)
        )

        // ── Üst Bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Geri butonu
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        Color.Black.copy(alpha = 0.4f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Geri",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Flaş toggle
            IconButton(
                onClick = { viewModel.toggleFlash() },
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (flashEnabled) DermPrimary.copy(alpha = 0.8f)
                        else Color.Black.copy(alpha = 0.4f),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = if (flashEnabled) Icons.Default.FlashOn
                    else Icons.Default.FlashOff,
                    contentDescription = "Flaş",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // ── Düşük ışık uyarısı ──
        AnimatedVisibility(
            visible = isLowLight && !flashEnabled,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 100.dp)
        ) {
            Row(
                modifier = Modifier
                    .background(
                        Color(0xFFF59E0B).copy(alpha = 0.9f),
                        RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.LightMode,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ortam ışığı düşük – flaşı açın",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // ── İpuçları (altta) ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 140.dp)
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = tips[currentTipIndex],
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // ── Çekim Butonu ──
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
        ) {
            // Dış halka
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .border(4.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                // İç buton
                FloatingActionButton(
                    onClick = {
                        capturePhoto(
                            context = context,
                            imageCapture = imageCapture,
                            onCaptured = { uri ->
                                onPhotoCaptured(uri.toString())
                            }
                        )
                    },
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    containerColor = Color.White
                ) {
                    Icon(
                        Icons.Outlined.CameraAlt,
                        contentDescription = "Fotoğraf Çek",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/** Y düzlemi üzerinde atlama örneklemesi; tam kopya / map / average yok. */
private fun computeSampledAverageLuma(imageProxy: ImageProxy): Double {
    val plane = imageProxy.planes[0]
    val buffer = plane.buffer.duplicate().apply { rewind() }
    val rowStride = plane.rowStride
    val pixelStride = plane.pixelStride
    val width = imageProxy.width
    val height = imageProxy.height
    val stepX = 8
    val stepY = 8
    var sum = 0L
    var count = 0
    var y = 0
    while (y < height) {
        val rowStart = y * rowStride
        var x = 0
        while (x < width) {
            val idx = rowStart + x * pixelStride
            if (idx in 0 until buffer.limit()) {
                sum += (buffer.get(idx).toInt() and 0xFF)
                count++
            }
            x += stepX
        }
        y += stepY
    }
    return if (count > 0) sum.toDouble() / count else 0.0
}

/**
 * CameraX ile fotoğraf çekme
 */
private fun capturePhoto(
    context: Context,
    imageCapture: ImageCapture,
    onCaptured: (Uri) -> Unit
) {
    val tempFile = File.createTempFile("dermai_camera_", ".jpg", context.cacheDir).apply {
        deleteOnExit()
    }
    val outputOptions = ImageCapture.OutputFileOptions.Builder(tempFile).build()

    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    tempFile
                )
                onCaptured(uri)
            }

            override fun onError(exception: ImageCaptureException) {
                // Hata durumunda sessizce devam et
            }
        }
    )
}
