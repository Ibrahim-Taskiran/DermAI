package com.ibrahim.dermai.ui.screens.bodymap

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

private const val TAG = "DermAI.BodyMap"

/**
 * Vücut haritası ekranının state yönetimi.
 * GLB içindeki mesh / node adları [regionMapping] ile Türkçe etikete çevrilir.
 * Kendi modelindeki isimler farklıysa map'e ekle veya Blender'da obje adlarını eşleştir.
 */
@HiltViewModel
class BodyMapViewModel @Inject constructor() : ViewModel() {

    val bodyRegions = listOf(
        "Baş", "Gövde", "Sol Kol", "Sağ Kol", "Bacaklar"
    )

    /** İngilizce / Blender çıktısı yaygın isimler → uygulama metni */
    private val regionMapping = mapOf(
        "Head" to "Baş",
        "head" to "Baş",
        "Body" to "Gövde",
        "body" to "Gövde",
        "Torso" to "Gövde",
        "torso" to "Gövde",
        "Chest" to "Gövde",
        "Arm_L" to "Sol Kol",
        "Arm.L" to "Sol Kol",
        "LeftArm" to "Sol Kol",
        "arm_l" to "Sol Kol",
        "Arm_R" to "Sağ Kol",
        "Arm.R" to "Sağ Kol",
        "RightArm" to "Sağ Kol",
        "arm_r" to "Sağ Kol",
        "Legs" to "Bacaklar",
        "legs" to "Bacaklar",
        "Leg" to "Bacaklar",
        "LowerBody" to "Bacaklar"
    )

    private val _selectedRegion = MutableStateFlow<String?>(null)
    val selectedRegion: StateFlow<String?> = _selectedRegion

    fun selectRegion(regionId: String) {
        val raw = regionId.trim()
        if (raw.isEmpty()) return

        val stripped = raw.removeSuffix(".001").removeSuffix("_001")
        val mapped = regionMapping[stripped]
            ?: regionMapping[raw]
            ?: regionMapping.entries.firstOrNull { (k, _) ->
                stripped.equals(k, ignoreCase = true) || raw.equals(k, ignoreCase = true)
            }?.value
            ?: mapByPrefix(stripped)
            ?: mapByPrefix(raw)

        val label = mapped ?: raw
        if (mapped == null) {
            Log.d(TAG, "Eşleşmeyen mesh/node adı (map'e ekleyebilirsin): $raw")
        }
        _selectedRegion.value = label
    }

    private fun mapByPrefix(name: String): String? {
        val n = name.lowercase()
        return when {
            n.startsWith("head") || n.contains("kafa") || n.contains("yuz") || n.contains("yüz") -> "Baş"
            n.startsWith("body") || n.startsWith("torso") || n.contains("gogus") || n.contains("göğüs") ||
                n.contains("karin") || n.contains("karın") -> "Gövde"
            n.contains("arm_l") || n.contains("arm.l") || n.contains("leftarm") || n.endsWith("_l") && n.contains("arm") -> "Sol Kol"
            n.contains("arm_r") || n.contains("arm.r") || n.contains("rightarm") || n.endsWith("_r") && n.contains("arm") -> "Sağ Kol"
            n.startsWith("leg") || n.contains("bacak") || n.contains("foot") || n.contains("ayak") -> "Bacaklar"
            else -> null
        }
    }
}
