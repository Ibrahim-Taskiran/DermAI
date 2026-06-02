package com.ibrahim.dermai.util

import android.content.Context
import android.net.Uri
import java.io.File

/**
 * Disk yolu, content:// ve file:// URI'larını güvenli şekilde normalize eder.
 * Uri.parse("/data/...") Coil ve ContentResolver için hatalıdır.
 */
object ImageUriUtil {

    fun toUri(pathOrUri: String): Uri = when {
        pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://") ->
            Uri.parse(pathOrUri)
        else ->
            Uri.fromFile(File(pathOrUri))
    }

    /** Coil ImageRequest.data için — mutlak yollar doğrudan File ile verilir */
    fun toCoilData(pathOrUri: String): Any = when {
        pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://") ->
            Uri.parse(pathOrUri)
        else ->
            File(pathOrUri)
    }

    /**
     * API yükleme için görsel baytlarını okur.
     * Cache'deki mutlak yollar File ile okunur; file:// + ContentResolver bazı cihazlarda çöker.
     */
    fun readBytes(context: Context, pathOrUri: String): ByteArray {
        val bytes = when {
            pathOrUri.startsWith("content://") || pathOrUri.startsWith("file://") -> {
                val uri = Uri.parse(pathOrUri)
                context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            }
            else -> {
                val file = File(pathOrUri)
                if (file.isFile && file.canRead()) file.readBytes() else null
            }
        }
        return bytes ?: throw IllegalStateException("Görüntü dosyası açılamadı: $pathOrUri")
    }
}
