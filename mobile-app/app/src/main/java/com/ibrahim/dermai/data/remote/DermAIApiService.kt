package com.ibrahim.dermai.data.remote

import com.ibrahim.dermai.data.model.AnalysisResponse
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Kerem'in FastAPI backend'iyle iletişim kuracak Retrofit API arayüzü.
 *
 * TODO(BACKEND): Bu interface şu an kullanılmıyor (MockAnalysisRepository aktif).
 *   Kerem'in API'si hazır olduğunda:
 *   1. Endpoint yolunu ("/predict") Kerem'in gerçek endpoint'iyle kontrol et
 *   2. AppModule'deki provideAnalysisRepository() fonksiyonunu güncelle
 *   3. ApiAnalysisRepository'i injection için aktif et
 *
 * Kerem'in API çıktı formatı (Görev Dağılımı belgesinden):
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
