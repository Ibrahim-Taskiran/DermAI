package com.ibrahim.dermai.util

import android.util.Base64

/**
 * Navigasyon argümanında dosya yolu güvenli taşıma (Windows/Android yol karakterleri).
 */
object NavPathEncoder {

    /** NO_PADDING: '=' karakteri nav route'ta kırılmayı önler */
    private const val ENCODE_FLAGS = Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING

    fun encode(path: String): String =
        Base64.encodeToString(path.toByteArray(Charsets.UTF_8), ENCODE_FLAGS)

    fun decode(encoded: String): String {
        if (encoded.isBlank()) return ""
        return try {
            String(Base64.decode(encoded, ENCODE_FLAGS), Charsets.UTF_8)
        } catch (_: IllegalArgumentException) {
            // Eski derlemelerde padding'li Base64 ile kayıtlı rotalar
            String(Base64.decode(encoded, Base64.URL_SAFE or Base64.NO_WRAP), Charsets.UTF_8)
        }
    }
}
