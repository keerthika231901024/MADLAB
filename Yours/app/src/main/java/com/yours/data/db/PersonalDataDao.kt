package com.yours.data.db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface PersonalDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(data: PersonalDataEntity): Long

    @Query("SELECT * FROM personal_data ORDER BY timestamp DESC")
    fun getAllData(): LiveData<List<PersonalDataEntity>>

    @Query("SELECT * FROM personal_data ORDER BY timestamp DESC")
    suspend fun getAllDataSync(): List<PersonalDataEntity>

    @Query("""
        SELECT * FROM personal_data 
        WHERE tag LIKE '%' || :query || '%' 
           OR rawKey LIKE '%' || :query || '%'
           OR value LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    suspend fun search(query: String): List<PersonalDataEntity>

    @Query("DELETE FROM personal_data WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("DELETE FROM personal_data WHERE tag LIKE '%' || :tag || '%'")
    suspend fun deleteByTag(tag: String)
}
