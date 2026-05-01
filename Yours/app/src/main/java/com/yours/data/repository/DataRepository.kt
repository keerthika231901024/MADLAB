package com.yours.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.yours.data.db.AppDatabase
import com.yours.data.db.PersonalDataEntity

class DataRepository(context: Context) {

    private val dao = AppDatabase.getDatabase(context).personalDataDao()

    val allData: LiveData<List<PersonalDataEntity>> = dao.getAllData()

    suspend fun saveData(tag: String, value: String, rawKey: String) {
        val entity = PersonalDataEntity(
            tag = tag.lowercase().trim(),
            rawKey = rawKey.trim(),
            value = value.trim()
        )
        dao.insert(entity)
    }

    suspend fun search(query: String): List<PersonalDataEntity> {
        return dao.search(query.lowercase().trim())
    }

    suspend fun getAllDataSync(): List<PersonalDataEntity> {
        return dao.getAllDataSync()
    }

    suspend fun deleteByTag(tag: String) {
        dao.deleteByTag(tag.lowercase().trim())
    }

    suspend fun deleteById(id: Int) {
        dao.deleteById(id)
    }
}
