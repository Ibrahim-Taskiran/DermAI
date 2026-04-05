package com.ibrahim.dermai.ui.navigation

/**
 * Uygulama içindeki tüm ekran rotalarını tanımlar.
 * Navigation argümanları buradan yönetilir.
 */
sealed class Screen(val route: String) {

    /** Açılış ekranı - otomatik olarak ImageSelection'a geçer */
    data object Splash : Screen("splash")

    /** Kamera veya galeriden fotoğraf seçme ekranı */
    data object ImageSelection : Screen("image_selection")

    /** CameraX ile kılavuzlu fotoğraf çekme ekranı */
    data object Camera : Screen("camera")

    /** Hasta bilgi formu (yaş, cinsiyet, cilt tipi) - Mod: onboarding veya settings */
    data object MetadataForm : Screen("metadata_form/{mode}") {
        fun createRoute(mode: String): String {
            return "metadata_form/$mode"
        }
    }

    /** 2D vücut haritası - lezyon bölgesi seçimi */
    data object BodyMap : Screen("body_map/{imagePath}") {
        fun createRoute(imagePath: String): String {
            val encoded = java.net.URLEncoder.encode(imagePath, "UTF-8")
            return "body_map/$encoded"
        }
    }

    /** Analiz ekranı - seçilen görseli analiz eder */
    data object Analysis : Screen("analysis/{imagePath}") {
        fun createRoute(imagePath: String): String {
            val encoded = java.net.URLEncoder.encode(imagePath, "UTF-8")
            return "analysis/$encoded"
        }
    }

    /** Sonuç ekranı - analiz tamamlanınca buraya yönlendirilir */
    data object Result : Screen("result")

    /** Hastalık takip günlüğü - geçmiş analizler */
    data object Tracker : Screen("tracker")
}
