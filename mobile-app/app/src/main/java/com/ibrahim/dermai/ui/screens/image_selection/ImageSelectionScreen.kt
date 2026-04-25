package com.ibrahim.dermai.ui.screens.image_selection

import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.ImageSearch
import androidx.compose.material.icons.outlined.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.ibrahim.dermai.ui.theme.DermPrimary
import com.ibrahim.dermai.ui.theme.DermSecondary

import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.IconButton

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import android.widget.Toast
import kotlinx.coroutines.launch
import com.ibrahim.dermai.util.ImageOptimizer
import java.io.File
import java.util.UUID

/**
 * Kamera veya galeriden fotoğraf seçme ekranı.
 * Varsayılan sistem kamerasını kullanır.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionScreen(
    modifier: Modifier = Modifier,
    viewModel: ImageSelectionViewModel = hiltViewModel(),
    onImageSelectedFromGallery: (String) -> Unit, // actually handles both camera and gallery now
    onNavigateToSettings: () -> Unit,
    onOpenTracker: () -> Unit
) {
    val selectedUri by viewModel.selectedImageUri.collectAsState()
    val context = LocalContext.current
    var showCameraWarningDialog by remember { mutableStateOf(false) }
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Galeri seçici
    val galleryLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                coroutineScope.launch {
                    val optimizedPath = ImageOptimizer.optimizeImage(context, it)
                    if (optimizedPath != null) {
                        val optimizedUri = Uri.fromFile(File(optimizedPath))
                        viewModel.onImageSelected(optimizedUri)
                        onImageSelectedFromGallery(optimizedPath)
                    } else {
                        Toast.makeText(context, "Fotoğraf yüklenemedi veya okunamadı. Lütfen başka bir fotoğraf seçin.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    // Kamera seçici
    val cameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                coroutineScope.launch {
                    val optimizedPath = ImageOptimizer.optimizeImage(context, tempCameraUri!!)
                    if (optimizedPath != null) {
                        val optimizedUri = Uri.fromFile(File(optimizedPath))
                        viewModel.onImageSelected(optimizedUri)
                        onImageSelectedFromGallery(optimizedPath)
                    } else {
                        Toast.makeText(context, "Fotoğraf kaydedilemedi. Lütfen tekrar deneyin.", Toast.LENGTH_LONG).show()
                    }
                }
            } else if (!success) {
                // Kamera iptal edildi veya fotoğraf çekilemedi
            }
        }
    )

    fun openCamera() {
        val cacheDir = context.cacheDir
        val tempFile = File(cacheDir, "temp_camera_${UUID.randomUUID()}.jpg")
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tempFile)
        tempCameraUri = uri
        cameraLauncher.launch(uri)
    }

    if (showCameraWarningDialog) {
        AlertDialog(
            onDismissRequest = { showCameraWarningDialog = false },
            title = { Text("Kamera Çekimi") },
            text = { Text("Cilt bölgesini net, yakın ve iyi ışıkta çekmeye çalışın.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showCameraWarningDialog = false
                        openCamera()
                    }
                ) {
                    Text("Anladım")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCameraWarningDialog = false }) {
                    Text("İptal")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Görsel Seçimi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                        Text(
                            text = "Analiz için bir fotoğraf seçin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Profil Ayarları"
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ── Görsel Önizleme Alanı ──
            ImagePreviewArea(selectedUri)

            Spacer(modifier = Modifier.height(28.dp))

            // ── Aksiyon Butonları (Kamera, Galeri, Geçmiş) ──
            ActionButtons(
                onCameraClick = { showCameraWarningDialog = true },
                onGalleryClick = { galleryLauncher.launch("image/*") },
                onHistoryClick = onOpenTracker
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ── Bilgi Kartı ──
            TipsCard()

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ImagePreviewArea(selectedUri: Uri?) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (selectedUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = selectedUri),
                    contentDescription = "Seçilen Görsel",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(24.dp)),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(
                                bottomStart = 24.dp,
                                bottomEnd = 24.dp
                            )
                        )
                )
                Text(
                    text = "Fotoğraf seçildi ✓",
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ImageSearch,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Henüz bir görsel seçilmedi",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Aşağıdaki seçenekleri kullanın",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButtons(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    val buttonHeight = 60.dp
    val buttonShape = RoundedCornerShape(16.dp)

    // ── Kamera Butonu ──
    Button(
        onClick = onCameraClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight),
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(containerColor = DermPrimary),
        contentPadding = PaddingValues(horizontal = 24.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Icon(
            Icons.Default.CameraAlt,
            contentDescription = "Kamera",
            modifier = Modifier.size(22.dp),
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Fotoğraf Çek",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // ── Galeri Butonu ──
    Button(
        onClick = onGalleryClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .border(
                width = 1.5.dp,
                brush = Brush.horizontalGradient(
                    colors = listOf(DermPrimary, DermSecondary)
                ),
                shape = buttonShape
            ),
        shape = buttonShape,
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(horizontal = 24.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Icon(
            Icons.Default.PhotoLibrary,
            contentDescription = "Galeri",
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Galeriden Yükle",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // ── Geçmiş Analizler Butonu ──
    OutlinedButton(
        onClick = onHistoryClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(buttonHeight),
        shape = buttonShape
    ) {
        Icon(
            Icons.Outlined.History,
            contentDescription = "Geçmiş",
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            "Geçmiş Analizler",
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.TipsAndUpdates,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "İpuçları",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                TipItem("Cilt bölgesinin net ve yakın çekim fotoğrafını kullanın")
                TipItem("İyi aydınlatılmış bir ortamda çekim yapın")
                TipItem("Fotoğrafın bulanık olmamasına dikkat edin")
                TipItem("Sadece analiz edilecek bölgeyi kadrajlayın")
            }
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp, top = 1.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
            lineHeight = 18.sp
        )
    }
}
