package com.ibrahim.dermai.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash ekranının ViewModel'i.
 * 2 saniye bekleyip navigate event yayınlar.
 */
@HiltViewModel
class SplashViewModel @Inject constructor() : ViewModel() {

    private val _navigateToHome = MutableSharedFlow<Unit>()
    val navigateToHome: SharedFlow<Unit> = _navigateToHome

    init {
        viewModelScope.launch {
            delay(2000)
            _navigateToHome.emit(Unit)
        }
    }
}
