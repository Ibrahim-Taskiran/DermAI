package com.ibrahim.dermai.ui.screens.camera

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Kamera ekranının state yönetimi.
 */
@HiltViewModel
class CameraViewModel @Inject constructor() : ViewModel() {

    private val _flashEnabled = MutableStateFlow(false)
    val flashEnabled: StateFlow<Boolean> = _flashEnabled

    private val _isLowLight = MutableStateFlow(false)
    val isLowLight: StateFlow<Boolean> = _isLowLight

    private val _capturedImageUri = MutableStateFlow<Uri?>(null)
    val capturedImageUri: StateFlow<Uri?> = _capturedImageUri

    fun toggleFlash() {
        _flashEnabled.value = !_flashEnabled.value
    }

    fun setFlashEnabled(enabled: Boolean) {
        _flashEnabled.value = enabled
    }

    fun updateLightLevel(isLow: Boolean) {
        if (_isLowLight.value != isLow) {
            _isLowLight.value = isLow
        }
    }

    fun onPhotoCaptured(uri: Uri) {
        _capturedImageUri.value = uri
    }
}
