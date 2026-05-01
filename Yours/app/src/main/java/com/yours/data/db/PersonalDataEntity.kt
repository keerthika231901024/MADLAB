package com.yours.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "personal_data")
data class PersonalDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val tag: String,        // normalized key e.g. "aadhaar number"
    val rawKey: String,     // original user phrase e.g. "Aadhaar card number"
    val value: String,      // the stored value
    val timestamp: Long = System.currentTimeMillis()
)
