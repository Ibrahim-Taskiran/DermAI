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

    /** Analiz ekranı - seçilen görseli analiz eder */
    data object Analysis : Screen("analysis/{imagePath}") {
        fun createRoute(imagePath: String): String {
            // URI encode ederek path'i güvenli hale getiriyoruz
            val encoded = java.net.URLEncoder.encode(imagePath, "UTF-8")
            return "analysis/$encoded"
        }
    }

    /** Sonuç ekranı - analiz tamamlanınca buraya yönlendirilir */
    data object Result : Screen("result")
}
