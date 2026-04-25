package com.ibrahim.dermai.data.repository

import android.content.Context
import android.net.Uri
import com.ibrahim.dermai.data.model.AnalysisResponse
import com.ibrahim.dermai.data.remote.DermAIApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject

/**
 * FastAPI backend'i ile gerçek API çağrılarını yapan Repository implementasyonu.
 *
 * Android 13+ ile gelen Photo Picker, gerçek dosya yolu değil
 * "content://" URI'ı döndürür. File() ile bu URI'lar açılamaz.
 * Bu yüzden ContentResolver kullanarak URI'dan byte okuyoruz.
 */
class ApiAnalysisRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: DermAIApiService
) : AnalysisRepository {

    override suspend fun analyzeImage(imagePath: String): AnalysisResponse {
        // imagePath bir "content://" URI'ı veya gerçek dosya yolu olabilir
        val uri = Uri.parse(imagePath)

        // ContentResolver aracılığıyla URI'dan byte dizisini oku
        // Bu yöntem hem content:// URI'larını hem de file:// yollarını destekler
        val imageBytes = context.contentResolver
            .openInputStream(uri)
            ?.use { it.readBytes() }
            ?: throw IllegalStateException("Görüntü dosyası açılamadı: $imagePath")

        // MIME tipini URI'dan otomatik al, bulunamazsa varsayılan JPEG kullan
        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"

        // Byte dizisinden multipart gövdesi oluştur
        val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())

        // Dosya adını URI'dan çıkar, bulunamazsa sabit isim kullan
        val fileName = uri.lastPathSegment ?: "image.jpg"

        val multipartBody = MultipartBody.Part.createFormData(
            name = "file",        // backend/routers/predict.py'deki field adıyla eşleşmeli
            filename = fileName,
            body = requestBody
        )

        return apiService.analyzeImage(multipartBody)
    }
}
