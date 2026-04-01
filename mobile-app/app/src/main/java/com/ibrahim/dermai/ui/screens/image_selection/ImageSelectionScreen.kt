package com.ibrahim.dermai.ui.screens.image_selection

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSelectionScreen(
    modifier: Modifier = Modifier,
    viewModel: ImageSelectionViewModel = hiltViewModel(),
    onImageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val selectedUri by viewModel.selectedImageUri.collectAsState()

    // Kamera için geçici dosya URI'sini tutacak state (camera launcher çalışmadan önce oluşturulmalı)
    var tempCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Galeri seçici
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let {
                viewModel.onImageSelected(it)
                // Gerçek sistem yoluna veya Content URI stringine dönüştür
                onImageSelected(it.toString())
            }
        }
    )

    // Kamera çekicisi
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && tempCameraUri != null) {
                viewModel.onImageSelected(tempCameraUri)
                onImageSelected(tempCameraUri.toString())
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Görsel Seçimi", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Daha önce seçilen resmi göstermek içn opsiyonel alan (tasarım açısından)
            if (selectedUri != null) {
                Image(
                    painter = rememberAsyncImagePainter(model = selectedUri),
                    contentDescription = "Seçilen Görsel",
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(32.dp))
            } else {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Gray.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Henüz bir alan seçilmedi")
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            Text(
                text = "Lütfen analiz edilecek bölgenin fotoğrafını çekin veya galeriden yükleyin.",
                fontSize = 16.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Kamera Butonu
            Button(
                onClick = {
                    val uri = createTempImageUri(context)
                    tempCameraUri = uri
                    cameraLauncher.launch(uri)
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CameraAlt, contentDescription = "Kamera")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Kamera ile Çek", fontSize = 16.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Galeri Butonu
            OutlinedButton(
                onClick = { galleryLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = "Galeri")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Galeriden Seç", fontSize = 16.sp)
            }
        }
    }
}

/**
 * Kamera için geçici bir dosya ve onun URI'sini oluşturan yardımcı fonksiyon.
 */
private fun createTempImageUri(context: Context): Uri {
    val tempFile = File.createTempFile("camera_image_", ".jpg", context.cacheDir).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        tempFile
    )
}
