package com.ibrahim.dermai.ui.screens.image_selection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun ImageSelectionScreen(
    modifier: Modifier = Modifier,
    viewModel: ImageSelectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Image Selection Screen")
    }
}
