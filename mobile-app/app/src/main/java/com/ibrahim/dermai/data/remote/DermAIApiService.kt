package com.ibrahim.dermai.data.remote

import com.ibrahim.dermai.data.model.AnalysisResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * FastAPI backend'iyle iletişim kuran Retrofit API arayüzü.
 *
 * Backend API çıktı formatı:
 *   - hastalık adı
 *   - tahmin olasılığı
 *   - ilk 3 tahmin
 *   - bakım önerisi
 *   - doktor uyarısı
 */
interface DermAIApiService {

    /**
     * Cilt görselini backend'e gönderir, AI analiz sonucunu döndürür.
     * @param image Multipart formatında görsel dosyası
     * @return AnalysisResponse - hastalık tahmini ve öneriler
     */
    @Multipart
    @POST("predict")
    suspend fun analyzeImage(
        @Part image: MultipartBody.Part
    ): AnalysisResponse
}
