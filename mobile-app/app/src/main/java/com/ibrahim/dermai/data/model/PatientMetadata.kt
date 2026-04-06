package com.ibrahim.dermai.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Kullanıcının analiz öncesinde girdiği hasta/cilt bilgileri.
 * Backend hazır olduğunda API isteğiyle birlikte gönderilecektir.
 */
@Parcelize
data class PatientMetadata(
    val age: Int,
    val gender: Gender,
    val skinType: FitzpatrickType,
    val pastConditions: List<String> = emptyList()
) : Parcelable

/**
 * Cinsiyet seçenekleri
 */
enum class Gender(val label: String) {
    ERKEK("Erkek"),
    KADIN("Kadın"),
    DIGER("Belirtmek İstemiyorum")
}

/**
 * Fitzpatrick cilt tipi sınıflandırması.
 * Dermatolojide standart cilt tipi ölçeği.
 * colorHex: UI'da gösterilecek temsili cilt rengi.
 */
enum class FitzpatrickType(
    val label: String,
    val description: String,
    val colorHex: Long
) {
    TIP_1("Tip I", "Çok açık ten, her zaman yanar, bronzlaşmaz", 0xFFFDE8D0),
    TIP_2("Tip II", "Açık ten, kolay yanar, hafif bronzlaşır", 0xFFF5D0A9),
    TIP_3("Tip III", "Orta ten, bazen yanar, kademeli bronzlaşır", 0xFFD4A574),
    TIP_4("Tip IV", "Koyu buğday ten, nadiren yanar", 0xFFA67C52),
    TIP_5("Tip V", "Koyu kahverengi ten, çok nadir yanar", 0xFF6B4423),
    TIP_6("Tip VI", "Çok koyu ten, asla yanmaz", 0xFF3B2F2F)
}
