package com.ibrahim.dermai.data.model

import com.google.gson.annotations.SerializedName

data class AnalysisResponse(
    val success: Boolean,

    @SerializedName("top_prediction")
    val topPrediction: Prediction,

    @SerializedName("top3_predictions")
    val top3Predictions: List<Prediction>,

    val advice: Advice
)

data class Prediction(
    val disease: String,
    val probability: Double
)

data class Advice(
    val care: String,
    val recommendation: String,

    @SerializedName("doctor_warning")
    val doctorWarning: String
)