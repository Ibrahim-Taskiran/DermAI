package com.ibrahim.dermai.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahim.dermai.data.repository.AnalysisRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val repository: AnalysisRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState

    fun analyzeImage(imagePath: String) {
        if (imagePath.isBlank()) {
            _uiState.value = AnalysisUiState(
                error = "Görsel yolu geçersiz. Lütfen fotoğrafı yeniden seçin."
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = AnalysisUiState(isLoading = true, result = null, error = null)

            try {
                val result = withContext(Dispatchers.IO) {
                    repository.analyzeImage(imagePath)
                }
                _uiState.value = AnalysisUiState(result = result)
            } catch (e: java.io.IOException) {
                _uiState.value = AnalysisUiState(
                    error = "İnternet bağlantısı yok veya sunucuya ulaşılamadı. " +
                        "Telefon ve bilgisayarın aynı ağda olduğundan emin olun."
                )
            } catch (e: retrofit2.HttpException) {
                val message = when (e.code()) {
                    503 -> "AI modeli henüz yüklenmedi. Sunucuda checkpoint dosyasını kontrol edin."
                    400 -> "Geçersiz görüntü formatı veya dosya çok büyük (en fazla 10 MB)."
                    500 -> "Görüntü işlenirken sunucu hatası oluştu. Başka bir fotoğrafla deneyin."
                    else -> "Sunucu yanıt vermedi (HTTP ${e.code()}). Lütfen daha sonra tekrar deneyin."
                }
                _uiState.value = AnalysisUiState(error = message)
            } catch (e: IllegalStateException) {
                _uiState.value = AnalysisUiState(
                    error = e.message ?: "Görüntü dosyası okunamadı. Lütfen fotoğrafı yeniden seçin."
                )
            } catch (e: Exception) {
                _uiState.value = AnalysisUiState(error = "Fotoğraf işlenirken beklenmeyen bir hata oluştu. Lütfen tekrar deneyin.")
            }
        }
    }

    /** Sonuç ekranına geçildikten sonra tekrar navigate tetiklenmesin diye */
    fun consumeResult() {
        _uiState.value = _uiState.value.copy(result = null)
    }
}
