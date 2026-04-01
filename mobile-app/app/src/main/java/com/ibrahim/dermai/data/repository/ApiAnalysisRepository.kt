package com.ibrahim.dermai.data.repository

import com.ibrahim.dermai.data.model.AnalysisResponse
import com.ibrahim.dermai.data.remote.DermAIApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Kerem'in FastAPI backend'i ile gerçek API çağrılarını yapan Repository implementasyonu.
 *
 * TODO(BACKEND): Bu sınıf hazır ve implement edilmiş durumda.
 *   Aktif etmek için AppModule.kt'deki provideAnalysisRepository() fonksiyonunu güncelle:
 *
 *   @Provides
 *   @Singleton
 *   fun provideAnalysisRepository(apiService: DermAIApiService): AnalysisRepository {
 *       return ApiAnalysisRepository(apiService)  // Bu satırı kullan
 *   }
 *
 *   MockAnalysisRepository satırını sil veya yoruma al.
 */
class ApiAnalysisRepository @Inject constructor(
    private val apiService: DermAIApiService
) : AnalysisRepository {

    override suspend fun analyzeImage(imagePath: String): AnalysisResponse {
        val file = File(imagePath)
        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData(
            name = "file",  // TODO(BACKEND): Kerem'in API'sindeki form field adını kontrol et
            filename = file.name,
            body = requestBody
        )
        return apiService.analyzeImage(multipartBody)
    }
}
