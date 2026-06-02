package com.ibrahim.dermai.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AnalysisResponse(
    val success: Boolean,

    @SerializedName("top_prediction")
    val topPrediction: Prediction,

    @SerializedName("top3_predictions")
    val top3Predictions: List<Prediction>,

    val advice: Advice
) : Parcelable

@Parcelize
data class Prediction(
    val disease: String,
    val probability: Double
) : Parcelable

@Parcelize
data class Advice(
    @SerializedName("display_name")
    val displayName: String? = null,

    val care: String,
    val recommendation: String,

    @SerializedName("doctor_warning")
    val doctorWarning: String,

    @SerializedName("risk_level")
    val riskLevel: String? = null,

    val reference: String? = null
) : Parcelable