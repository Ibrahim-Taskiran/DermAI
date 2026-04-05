package com.ibrahim.dermai.ui.screens.tracker

import androidx.lifecycle.ViewModel
import com.ibrahim.dermai.data.model.AnalysisRecord
import com.ibrahim.dermai.data.repository.TrackerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/**
 * Hastalık takip günlüğü ekranının state yönetimi.
 */
@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val trackerRepository: TrackerRepository
) : ViewModel() {

    private val _records = MutableStateFlow<List<AnalysisRecord>>(emptyList())
    val records: StateFlow<List<AnalysisRecord>> = _records

    private val _selectedFilter = MutableStateFlow<String?>(null)
    val selectedFilter: StateFlow<String?> = _selectedFilter

    init {
        loadRecords()
    }

    fun loadRecords() {
        _records.value = if (_selectedFilter.value != null) {
            trackerRepository.getRecordsByDisease(_selectedFilter.value!!)
        } else {
            trackerRepository.getAllRecords()
        }
    }

    fun setFilter(disease: String?) {
        _selectedFilter.value = disease
        loadRecords()
    }

    fun deleteRecord(id: String) {
        trackerRepository.deleteRecord(id)
        loadRecords()
    }

    /**
     * Tüm kayıtlardaki benzersiz hastalık isimlerini döndürür (filtre için).
     */
    fun getUniqueDiseses(): List<String> {
        return trackerRepository.getAllRecords()
            .map { it.topDisease }
            .distinct()
    }

    fun saveRecord(record: AnalysisRecord) {
        trackerRepository.saveRecord(record)
        loadRecords()
    }
}
