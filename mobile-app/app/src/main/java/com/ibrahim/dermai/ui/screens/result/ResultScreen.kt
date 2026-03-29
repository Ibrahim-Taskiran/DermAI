package com.ibrahim.dermai.ui.screens.result

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ResultScreen(
    modifier: Modifier = Modifier,
    viewModel: ResultViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Result Screen (Predictions and Advice)")
    }
}
