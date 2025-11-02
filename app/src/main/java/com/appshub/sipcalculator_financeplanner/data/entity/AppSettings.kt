package com.appshub.sipcalculator_financeplanner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1, // Always use ID 1 for singleton settings
    val isPinEnabled: Boolean = false,
    val pinHash: String = "",
    val pinPlaintext: String = "", // For development/recovery purposes only
    val createdDate: java.util.Date = java.util.Date(),
    val lastUpdated: java.util.Date = java.util.Date()
)