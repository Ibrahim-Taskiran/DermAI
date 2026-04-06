package com.ibrahim.dermai.ui.screens.bodymap

import android.view.MotionEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.TouchApp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ibrahim.dermai.data.model.Gender
import com.ibrahim.dermai.ui.theme.DermPrimary


import com.google.android.filament.EntityManager
import com.google.android.filament.IndirectLight
import com.google.android.filament.LightManager

// Sceneview (3D) kütüphanesi
import io.github.sceneview.node.ModelNode
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation


/**
 * 3D vücut modeli üzerinden lezyon bölgesi seçimi.
 * Ekranın tamamı 3D model ile kaplıdır, altta sadece "Analize Başla" butonu bulunur.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMapScreen(
    modifier: Modifier = Modifier,
    gender: Gender? = null,
    viewModel: BodyMapViewModel = hiltViewModel(),
    onContinue: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val selectedRegion by viewModel.selectedRegion.collectAsState()
    val context = LocalContext.current

    // İstediğiniz özel koordinat (X:0, Z:5.0)
    var currentRotation by remember { androidx.compose.runtime.mutableStateOf(0f) }
    val currentPosition = remember { Position(x = 0.0f, y = 0.0f, z = 5.0f) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // ── 3D Model (AndroidView kullanarak Lifecycle NPE bug'ını çözer) ──
        val modelPath = "models/body.glb"

        // SADECE AndroidView BLOĞUNU BU İLE DEĞİŞTİR

androidx.compose.ui.viewinterop.AndroidView(
    modifier = Modifier.fillMaxSize(),
    factory = { ctx ->
        val vm = viewModel

        object : io.github.sceneview.SceneView(ctx) {

            private var lastPanX = 0f
            private var currentRotation = 0f
            private var modelNode: ModelNode? = null
            private val entityRegionMap = mutableMapOf<Int, String>()

            init {
                val lightEntity = EntityManager.get().create()
                LightManager.Builder(LightManager.Type.DIRECTIONAL)
                    .color(1.0f, 1.0f, 1.0f)
                    .intensity(100_000f)
                    .direction(0.0f, -1.0f, -1.0f)   // yukarıdan önden vurur
                    .castShadows(false)
                    .build(engine, lightEntity)
                scene.addEntity(lightEntity)

                // 2) Ambient / IBL (tüm yönlerden yumuşak dolgu ışığı)
                val ibl = IndirectLight.Builder()
                    .intensity(30_000f)
                    .build(engine)
                scene.indirectLight = ibl
                // Kamera sabit
                cameraNode.apply {
                    position = Position(x = 0f, y = 0f, z = 0f)
                    isEditable = false
                    isPositionEditable = false
                    isRotationEditable = false
                }

                // ✅ MODEL LOAD (EN STABLE HAL)
                modelLoader.loadModelInstanceAsync("models/body.glb") { modelInstance ->
                    modelInstance?.let { instance ->

                        modelNode = ModelNode(modelInstance = instance).apply {
                            isTouchable = true
                            position = Position(0f, 0.3f, -2f)
                            scale = Position(0.3f, 0.3f, 0.3f)
                            rotation = Rotation(y = 0f)
                            isPositionEditable = false
                            isRotationEditable = false
                        }

                        // ✅ Entity isimlerini map'e kaydet
                        val asset = instance.asset
                        asset.entities.forEach { entity ->
                            val nodeName = asset.getName(entity) ?: return@forEach
                            android.util.Log.d("DermAI", "Entity: $nodeName") // TEST LOG
                            val turkishName = when {
                                nodeName.contains("Head", ignoreCase = true)  -> "Baş"
                                nodeName.contains("Body", ignoreCase = true)  -> "Gövde"
                                nodeName.contains("Arm_L", ignoreCase = true) -> "Sol Kol"
                                nodeName.contains("Arm_R", ignoreCase = true) -> "Sağ Kol"
                                nodeName.contains("Legs", ignoreCase = true)  -> "Bacaklar"
                                else -> null
                            }
                            turkishName?.let { entityRegionMap[entity] = it }
                        }

                        addChildNode(modelNode!!)
                    }
                }
                setOnTouchListener { _, event ->
                    when (event.actionMasked) {
                        MotionEvent.ACTION_DOWN -> {
                            lastPanX = event.x
                        }

                        MotionEvent.ACTION_MOVE -> {
                            val dx = event.x - lastPanX
                            lastPanX = event.x
                            currentRotation += dx * 0.5f
                            modelNode?.rotation = Rotation(y = currentRotation)
                        }

                        MotionEvent.ACTION_UP -> {
                            performClick()

                            val tapX = event.x
                            val tapY = event.y
                            val totalDx = kotlin.math.abs(tapX - lastPanX)

                            if (totalDx < 15f && modelNode != null) {

                                val cx = width * 0.5f   // ekranın yatay merkezi
                                val region = when {
                                    tapX < cx * 0.84f -> "Sağ Kol"
                                    tapX > cx * 1.13f -> "Sol Kol"
                                    tapY < height * 0.58f -> "Baş"
                                    tapY > height * 0.65f -> "Bacaklar"
                                    else -> "Gövde"
                                }

                                android.util.Log.d("DermAI_COORD", "tapX=$tapX, tapY=$tapY, w=$width, h=$height, bölge=$region")
                                vm.selectRegion(region)
                            }
                        }
                    }
                    true
                }
            }



            override fun onDetachedFromWindow() {
                try {
                    childNodes.forEach { it.destroy() }
                    clearChildNodes()
                    super.onDetachedFromWindow()
                } catch (_: Exception) {}
            }
        }
    },
    update = {},
    onRelease = { sceneView ->
        try {
            sceneView.destroy()
        } catch (_: Exception) {}
    }
)

        // ── Üst bar: Geri butonu + Başlık ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 16.dp, end = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Lezyon Bölgesi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // ── İpucu (model yoksa veya bölge seçilmediyse) ──
        val modelExists = remember {
            try { context.assets.open(modelPath).use { true } } catch (e: Exception) { false }
        }
        if (!modelExists) {
            // Model bulunamadığında bilgi göster
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            Color.White.copy(alpha = 0.15f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.TouchApp,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "3D model yüklenmedi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "assets/models/body.glb dosyasını projeye ekleyin",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        } else if (selectedRegion == null) {
            // Model var ama bölge seçilmedi - Sayfanın üstüne taşındı
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {


                Box(
                    modifier = Modifier
                        .background(
                            Color.Black.copy(alpha = 0.5f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "Modeli döndürün ve lezyon bölgesine dokunun",
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // ── Alt kısım: Seçili bölge + Devam butonu ──
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            // Seçili bölge gösterimi
            AnimatedVisibility(
                visible = selectedRegion != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Color.White.copy(alpha = 0.9f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.CheckCircle,
                        contentDescription = null,
                        tint = DermPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Seçilen bölge: ${selectedRegion ?: ""}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Devam butonu
            Button(
                onClick = { selectedRegion?.let { onContinue(it) } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DermPrimary,
                    disabledContainerColor = DermPrimary.copy(alpha = 0.3f)
                ),
                enabled = selectedRegion != null,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Text(
                    "Analize Başla",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    Icons.Outlined.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
