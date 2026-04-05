package com.ibrahim.dermai.ui.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.ibrahim.dermai.data.repository.UserProfileRepository

/**
 * Splash ekranının ViewModel'i.
 * 2 saniye bekleyip navigate event yayınlar.
 */
@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _navigateEvent = MutableSharedFlow<String>()
    val navigateEvent: SharedFlow<String> = _navigateEvent

    init {
        viewModelScope.launch {
            delay(2000)
            val hasProfile = userProfileRepository.getUserProfile() != null
            if (hasProfile) {
                _navigateEvent.emit("home") // Resim seçim ekranına
            } else {
                _navigateEvent.emit("onboarding") // Metadata formuna
            }
        }
    }
}
