package com.ibrahim.dermai.ui.screens.analysis

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import com.ibrahim.dermai.data.model.AnalysisResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalysisScreen(
    imagePath: String,
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = hiltViewModel(),
    onNavigateToResult: (AnalysisResponse) -> Unit,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Sonuç geldiğinde otomatik Result ekranına geç
    LaunchedEffect(uiState.result) {
        uiState.result?.let {
            onNavigateToResult(it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Görseli Analiz Et", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Text(
                text = "Seçilen Alan",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Seçilen görselin büyük önizlemesi
            Image(
                painter = rememberAsyncImagePainter(model = Uri.parse(imagePath)),
                contentDescription = "Analiz edilecek görsel",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(32.dp))

            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Yapay zeka analiz ediyor, lütfen bekleyin...")
                }
                uiState.error != null -> {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Hata Oluştu", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(uiState.error!!, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = { viewModel.analyzeImage(imagePath) },
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("Tekrar Dene")
                    }
                }
                else -> {
                    Button(
                        onClick = { viewModel.analyzeImage(imagePath) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Analizi Başlat", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}