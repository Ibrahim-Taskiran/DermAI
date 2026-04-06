package com.ibrahim.dermai.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Yerel Tracker'da saklanan bir analiz kaydı.
 * SharedPreferences + Gson ile serileştirilir.
 */
@Parcelize
data class AnalysisRecord(
    val id: String,
    val timestamp: Long,
    val imagePath: String,
    val topDisease: String,
    val probability: Double,
    val bodyRegion: String,
    val age: Int?,
    val gender: String?,
    val skinType: String?
) : Parcelable
