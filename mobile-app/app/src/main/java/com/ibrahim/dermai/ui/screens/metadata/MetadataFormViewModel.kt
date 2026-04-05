package com.ibrahim.dermai.ui.screens.metadata

import androidx.lifecycle.ViewModel
import com.ibrahim.dermai.data.model.FitzpatrickType
import com.ibrahim.dermai.data.model.Gender
import com.ibrahim.dermai.data.model.PatientMetadata
import com.ibrahim.dermai.data.repository.UserProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Metadata form ekranının state yönetimi.
 */
@HiltViewModel
class MetadataFormViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository
) : ViewModel() {

    private val _age = MutableStateFlow(30)
    val age: StateFlow<Int> = _age

    private val _gender = MutableStateFlow(Gender.ERKEK)
    val gender: StateFlow<Gender> = _gender

    private val _skinType = MutableStateFlow(FitzpatrickType.TIP_3)
    val skinType: StateFlow<FitzpatrickType> = _skinType

    private val _pastConditions = MutableStateFlow<List<String>>(emptyList())
    val pastConditions: StateFlow<List<String>> = _pastConditions

    init {
        // Form ilk açıldığında varsa yerel (eski) kayıtları yükle
        userProfileRepository.getUserProfile()?.let { profile ->
            _age.value = profile.age
            _gender.value = profile.gender
            _skinType.value = profile.skinType
            _pastConditions.value = profile.pastConditions
        }
    }

    fun updateAge(value: Int) {
        _age.value = value
    }

    fun updateGender(value: Gender) {
        _gender.value = value
    }

    fun updateSkinType(value: FitzpatrickType) {
        _skinType.value = value
    }

    fun toggleCondition(condition: String) {
        val currentList = _pastConditions.value.toMutableList()
        if (currentList.contains(condition)) {
            currentList.remove(condition)
        } else {
            // "Yok (Sağlıklı)" seçilirse diğerlerini temizle
            if (condition == "Yok (Sağlıklı)") {
                currentList.clear()
            } else {
                currentList.remove("Yok (Sağlıklı)")
            }
            currentList.add(condition)
        }
        _pastConditions.value = currentList
    }

    /**
     * Form verilerini PatientMetadata objesine dönüştürür ve sonraki açılış için yerel hafızaya kaydeder.
     */
    fun buildMetadata(): PatientMetadata {
        val metadata = PatientMetadata(
            age = _age.value,
            gender = _gender.value,
            skinType = _skinType.value,
            pastConditions = _pastConditions.value
        )
        userProfileRepository.saveUserProfile(metadata)
        return metadata
    }
}
