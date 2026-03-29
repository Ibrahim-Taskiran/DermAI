package com.ibrahim.dermai.ui.screens.analysis

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalysisViewModel = viewModel()
) {

    val uiState = viewModel.uiState.collectAsState().value

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        when {
            uiState.isLoading -> {
                Text(text = "Analyzing...")
            }

            uiState.result != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Top Prediction: ${uiState.result.topPrediction.disease}")
                    Text("Probability: ${uiState.result.topPrediction.probability}")
                    Text("Advice: ${uiState.result.advice.recommendation}")
                }
            }

            uiState.error != null -> {
                Text(text = "Error: ${uiState.error}")
            }

            else -> {
                Button(onClick = {
                    viewModel.analyzeImage("dummy_path")
                }) {
                    Text("Start Analysis")
                }
            }
        }
    }
}