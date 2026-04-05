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
                care = "• Cildi tahriş eden sabun, deterjan ve parfümlü ürünlerden kaçının\n• Günde en az 2 kez nemlendirici krem uygulayın\n• Duş süresini 10 dakikanın altında tutun ve ılık su kullanın\n• Pamuklu ve nefes alan kıyafetler tercih edin\n• Kaşıntı durumunda cildi kaşımaktan kaçının",
                recommendation = "• Dermatoloji uzmanı onaylı nemlendirici kremler kullanın\n• Stres yönetimi için düzenli egzersiz yapın\n• Alerjen olabilecek gıdaları tespit etmek için beslenme günlüğü tutun\n• Uykudan önce etkilenen bölgeye nemlendirici uygulayın",
                doctorWarning = "Bu analiz yapay zeka tarafından yapılmıştır ve kesin bir tıbbi teşhis niteliği taşımamaktadır. En kısa sürede bir dermatoloji uzmanına başvurmanız önerilir."
            )
        )
    }
}
