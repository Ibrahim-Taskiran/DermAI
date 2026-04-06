package com.ibrahim.dermai

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * DermAI ana Application sınıfı.
 * @HiltAndroidApp ile Hilt'in dependency injection mekanizması başlatılır.
 */
@HiltAndroidApp
class DermAIApp : Application()
