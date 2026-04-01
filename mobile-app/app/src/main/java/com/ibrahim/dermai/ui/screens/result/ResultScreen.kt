package com.ibrahim.dermai.ui.screens.result

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ibrahim.dermai.data.model.AnalysisResponse
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    analysisResult: AnalysisResponse?,
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = hiltViewModel(),
    onReanalyze: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analiz Sonucu", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        if (analysisResult == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sonuç bulunamadı.")
            }
            return@Scaffold
        }

        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Başlık Kartı
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "En Olası Hastalık",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = analysisResult.topPrediction.disease,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    val probabilityPercent = (analysisResult.topPrediction.probability * 100).roundToInt()
                    Text(
                        text = "Olasılık: %$probabilityPercent",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // İlk 3 Tahmin Listesi
            Text(
                text = "Diğer Olasılıklar",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            analysisResult.top3Predictions.drop(1).forEach { prediction ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = prediction.disease, fontSize = 16.sp)
                    Text(
                        text = "%${(prediction.probability * 100).roundToInt()}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Divider(color = Color.LightGray.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Bakım Önerileri Kartı
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Bakım Önerisi", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(analysisResult.advice.care, color = MaterialTheme.colorScheme.onSecondaryContainer)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(analysisResult.advice.recommendation, color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Doktor Uyarısı Kartı
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Uyarı",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = analysisResult.advice.doctorWarning,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Yeniden Analiz Butonu
            Button(
                onClick = onReanalyze,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Yeni Görsel Analiz Et", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

