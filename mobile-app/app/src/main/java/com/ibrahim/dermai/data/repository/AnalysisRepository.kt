package com.ibrahim.dermai.data.repository

import com.ibrahim.dermai.data.model.AnalysisResponse

/**
 * Interface defining the methods for communicating with our backend API.
 * This ensures the UI only depends on abstractions.
 */
interface AnalysisRepository {
    suspend fun analyzeImage(imagePath: String): AnalysisResponse
}
