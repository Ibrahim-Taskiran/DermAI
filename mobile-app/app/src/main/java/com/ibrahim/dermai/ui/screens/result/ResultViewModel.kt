package com.ibrahim.dermai.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.dermai.data.model.AnalysisRecord
import com.ibrahim.dermai.data.model.AnalysisResponse
import com.ibrahim.dermai.data.repository.TrackerRepository
import com.ibrahim.dermai.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val trackerRepository: TrackerRepository,
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState

    fun initializeResult(
        analysisResult: AnalysisResponse?,
        imagePath: String,
        bodyRegion: String
    ) {
        // Ekran recomposition'larında state'i gereksiz resetlememek için bir kez kur.
        if (_uiState.value.analysisResult != null || _uiState.value.errorMessage != null) return

        if (analysisResult == null) {
            _uiState.value = ResultUiState(errorMessage = "Sonuç bulunamadı.")
            return
        }

        _uiState.value = ResultUiState(
            analysisResult = analysisResult,
            imagePath = imagePath,
            bodyRegion = bodyRegion
        )
    }

    fun saveToTracker() {
        val currentState = _uiState.value
        val result = currentState.analysisResult ?: return

        if (currentState.isSaving || currentState.isSaved) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSaving = true)
            try {
                val metadata = userProfileRepository.getUserProfile()
                val record = AnalysisRecord(
                    id = UUID.randomUUID().toString(),
                    timestamp = System.currentTimeMillis(),
                    imagePath = currentState.imagePath,
                    topDisease = result.topPrediction.disease,
                    probability = result.topPrediction.probability,
                    bodyRegion = currentState.bodyRegion,
                    age = metadata?.age,
                    gender = metadata?.gender?.name,
                    skinType = metadata?.skinType?.name
                )
                trackerRepository.saveRecord(record)
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.localizedMessage ?: "Kayıt sırasında bir hata oluştu."
                )
            }
        }
    }
}

