package com.ibrahim.dermai.data.repository

import android.content.Context
import android.net.Uri
import java.io.File
import com.ibrahim.dermai.data.model.AnalysisResponse
import com.ibrahim.dermai.data.remote.DermAIApiService
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import com.ibrahim.dermai.util.ImageUriUtil
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
        val imageBytes = ImageUriUtil.readBytes(context, imagePath)

        val mimeType = when {
            imagePath.startsWith("content://") || imagePath.startsWith("file://") -> {
                context.contentResolver.getType(Uri.parse(imagePath)) ?: "image/jpeg"
            }
            else -> "image/jpeg" // ImageOptimizer çıktısı her zaman JPEG
        }

        val requestBody = imageBytes.toRequestBody(mimeType.toMediaTypeOrNull())

        val fileName = when {
            imagePath.startsWith("content://") || imagePath.startsWith("file://") ->
                Uri.parse(imagePath).lastPathSegment ?: "image.jpg"
            else -> File(imagePath).name.ifBlank { "image.jpg" }
        }

        val multipartBody = MultipartBody.Part.createFormData(
            name = "file",        // backend/routers/predict.py'deki field adıyla eşleşmeli
            filename = fileName,
            body = requestBody
        )

        return apiService.analyzeImage(multipartBody)
    }
}
