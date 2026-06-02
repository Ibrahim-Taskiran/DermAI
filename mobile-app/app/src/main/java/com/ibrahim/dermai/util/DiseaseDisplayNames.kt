package com.ibrahim.dermai.util

/**
 * API'den gelen İngilizce sınıf adlarını (ai-model/export/config.py EXPERT_CLASSES)
 * kullanıcı arayüzünde gösterilecek Türkçe kısa adlara çevirir.
 *
 * Backend ileride advice.display_name gönderirse o değer önceliklidir.
 */
object DiseaseDisplayNames {

    private val labels: Map<String, String> = mapOf(
        // ai-model/export/config.py — güncel 12 sınıf
        "Acne or Rosacea" to "Akne veya Rozasea",
        "Atopic Dermatitis" to "Atopik Dermatit",
        "Basal Cell Carcinoma" to "Bazal Hücre Karsinomu",
        "Benign Keratosis-like Lesions" to "Benign Keratoz Benzeri Lezyonlar",
        "Eczema" to "Egzama",
        "Melanocytic Nevi" to "Melanositik Nevus",
        "Melanoma" to "Melanom",
        "Normal" to "Sağlıklı Cilt",
        "Psoriasis pictures Lichen Planus and related diseases" to "Psoriasis / Liken Planus ve İlişkili Hastalıklar",
        "Seborrheic Keratoses and other Benign Tumors" to "Seboreik Keratozlar ve Diğer Benign Tümörler",
        "Tinea Ringworm Candidiasis and other Fungal Infections" to "Tinea, Kırmızı Kurdeşen, Kandidiyaz ve Diğer Mantar Enfeksiyonları",
        "Warts Molluscum and other Viral Infections" to "Siğil, Molluscum ve Diğer Viral Enfeksiyonlar",

        // Önceki model sürümü (takip günlüğündeki eski kayıtlar için)
        "Acne and Rosacea" to "Akne ve Rosacea",
        "Eczema (Atopic Dermatitis)" to "Egzama (Atopik Dermatit)",
        "Acne and Rosacea Photos" to "Akne ve Rosacea",
        "Actinic Keratosis Basal Cell Carcinoma and other Malignant Lesions" to "Kötü Huylu / Pre-Malign Lezyonlar",
        "Light Diseases and Disorders of Pigmentation" to "Pigmentasyon Bozuklukları",
        "Warts Molluscum and other Viral Infections" to "Siğil ve Viral Enfeksiyonlar",
        "Malignant Lesions" to "Kötü Huylu Lezyonlar",
        "Other" to "Diğer",
        "Pigmentation Disorders" to "Pigmentasyon Bozuklukları",
        "Viral Infections" to "Viral Enfeksiyonlar"
    )

    fun displayName(apiDisease: String, adviceDisplayName: String? = null): String {
        if (!adviceDisplayName.isNullOrBlank()) return adviceDisplayName
        return labels[apiDisease] ?: apiDisease
    }
}
