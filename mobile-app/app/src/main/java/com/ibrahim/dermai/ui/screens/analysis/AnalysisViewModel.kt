package com.ibrahim.dermai.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.dermai.data.repository.AnalysisRepository
import com.ibrahim.dermai.data.repository.MockAnalysisRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AnalysisViewModel : ViewModel() {

    private val repository: AnalysisRepository = MockAnalysisRepository()

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState

    fun analyzeImage(imagePath: String) {
        viewModelScope.launch {
            _uiState.value = AnalysisUiState(isLoading = true)

            try {
                val result = repository.analyzeImage(imagePath)
                _uiState.value = AnalysisUiState(result = result)
            } catch (e: Exception) {
                _uiState.value = AnalysisUiState(error = e.message)
            }
        }
    }
}