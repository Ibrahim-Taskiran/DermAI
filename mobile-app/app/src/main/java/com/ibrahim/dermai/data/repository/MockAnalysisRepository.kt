package com.ibrahim.dermai.data.repository

import com.ibrahim.dermai.data.model.Advice
import com.ibrahim.dermai.data.model.AnalysisResponse
import com.ibrahim.dermai.data.model.Prediction
import kotlinx.coroutines.delay

/**
 * A mock implementation of [AnalysisRepository] using fake data.
 * Useful for building the UI while the backend is not ready.
 */
class MockAnalysisRepository : AnalysisRepository {
     override suspend fun analyzeImage(imagePath: String): AnalysisResponse{
        // Simulate network delay
        delay(2000)
        
        return AnalysisResponse(
            success = true,
            topPrediction = Prediction(
                disease = "Egzama",
                probability = 0.78
            ),
            top3Predictions = listOf(
                Prediction("Egzama", 0.78),
                Prediction("Sedef Hastalığı", 0.14),
                Prediction("Mantar", 0.05)
            ),
            advice = Advice(
                care = "Avoid irritants",
                recommendation = "Use moisturizer",
                doctorWarning = "Consult a dermatologist"
            )
        )
    }
}
