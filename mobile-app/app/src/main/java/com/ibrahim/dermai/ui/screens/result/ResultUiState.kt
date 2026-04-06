package com.ibrahim.dermai.ui.screens.result

import com.ibrahim.dermai.data.model.AnalysisResponse

data class ResultUiState(
    val analysisResult: AnalysisResponse? = null,
    val imagePath: String = "",
    val bodyRegion: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val errorMessage: String? = null
)
