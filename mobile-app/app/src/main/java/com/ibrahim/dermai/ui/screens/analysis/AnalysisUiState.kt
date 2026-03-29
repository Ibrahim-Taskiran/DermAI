package com.ibrahim.dermai.ui.screens.analysis

import com.ibrahim.dermai.data.model.AnalysisResponse

data class AnalysisUiState(
    val isLoading: Boolean = false,
    val result: AnalysisResponse? = null,
    val error: String? = null
)