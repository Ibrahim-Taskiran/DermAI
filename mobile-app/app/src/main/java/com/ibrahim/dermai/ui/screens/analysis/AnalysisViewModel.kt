package com.ibrahim.dermai.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.dermai.data.repository.AnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val repository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState

    fun analyzeImage(imagePath: String) {
        viewModelScope.launch {
            _uiState.value = AnalysisUiState(isLoading = true)

            try {
                // TODO(BACKEND): imagePath burada repository'e gerçek API isteği için gönderiliyor.
                val result = repository.analyzeImage(imagePath)
                _uiState.value = AnalysisUiState(result = result)
            } catch (e: java.io.IOException) {
                _uiState.value = AnalysisUiState(error = "İnternet bağlantısı yok veya sunucuya ulaşılamadı. Lütfen bağlantınızı kontrol edip tekrar deneyin.")
            } catch (e: retrofit2.HttpException) {
                _uiState.value = AnalysisUiState(error = "Sunucu yanıt vermedi. Lütfen daha sonra tekrar deneyin.")
            } catch (e: Exception) {
                _uiState.value = AnalysisUiState(error = "Fotoğraf işlenirken beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.")
            }
        }
    }
}