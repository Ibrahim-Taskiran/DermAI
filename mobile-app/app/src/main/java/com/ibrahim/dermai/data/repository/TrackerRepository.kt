package com.ibrahim.dermai.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ibrahim.dermai.data.model.AnalysisRecord

/**
 * Analiz kayıtlarını SharedPreferences ile yerel olarak saklayan repository.
 * Veritabanı kullanmadan basit ve hafif bir çözüm sağlar.
 */
class TrackerRepository(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    /**
     * Yeni bir analiz kaydını kaydeder.
     */
    fun saveRecord(record: AnalysisRecord) {
        val records = getAllRecords().toMutableList()
        records.add(0, record) // En yeni kayıt en başa
        val json = gson.toJson(records)
        prefs.edit().putString(KEY_RECORDS, json).apply()
    }

    /**
     * Tüm kayıtları tarih sırasına göre (en yeni önce) döndürür.
     */
    fun getAllRecords(): List<AnalysisRecord> {
        val json = prefs.getString(KEY_RECORDS, null) ?: return emptyList()
        val type = object : TypeToken<List<AnalysisRecord>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Belirli bir hastalığa ait kayıtları filtreler.
     */
    fun getRecordsByDisease(disease: String): List<AnalysisRecord> {
        return getAllRecords().filter { it.topDisease == disease }
    }

    /**
     * Belirli bir kaydı siler.
     */
    fun deleteRecord(id: String) {
        val records = getAllRecords().toMutableList()
        records.removeAll { it.id == id }
        val json = gson.toJson(records)
        prefs.edit().putString(KEY_RECORDS, json).apply()
    }

    companion object {
        private const val PREFS_NAME = "dermai_tracker"
        private const val KEY_RECORDS = "analysis_records"
    }
}
