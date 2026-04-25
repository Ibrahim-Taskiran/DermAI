package com.ibrahim.dermai.data.repository

import android.content.Context
import android.net.Uri
import com.ibrahim.dermai.data.model.AnalysisResponse
import com.ibrahim.dermai.data.remote.DermAIApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

/**
 * FastAPI backend'i ile gerçek API çağrılarını yapan Repository implementasyonu.
 *
 * imagePath üç farklı formatta gelebilir:
 *   1. "content://..." → Galeri / Photo Picker URI (Android 13+)
 *   2. "file:///..."   → Dosya URI'ı
 *   3. "/data/..."     → ImageOptimizer'dan gelen gerçek disk yolu
 */
class ApiAnalysisRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: DermAIApiService
) : AnalysisRepository {

    override suspend fun analyzeImage(imagePath: String): AnalysisResponse {
        // Gelen yolu doğru URI tipine çevir
        val uri: Uri = when {
            imagePath.startsWith("content://") || imagePath.startsWith("file://") ->
                Uri.parse(imagePath)
            else ->
                // Düz dosya yolu → file:// URI'a çevir ki ContentResolver tanısın
                Uri.fromFile(File(imagePath))
        }

        // ContentResolver ile URI'dan byte dizisi oku (tüm URI tipleri desteklenir)
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
