package com.ibrahim.dermai.ui.screens.image_selection

import android.net.Uri
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * ImageSelectionScreen ViewModel'i.
 * Seçilen veya çekilen fotoğrafın Uri bilgisini tutar.
 */
@HiltViewModel
class ImageSelectionViewModel @Inject constructor() : ViewModel() {

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri

    fun onImageSelected(uri: Uri?) {
        _selectedImageUri.value = uri
    }
}

