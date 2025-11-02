package com.appshub.sipcalculator_financeplanner.data.dao

import androidx.room.*
import com.appshub.sipcalculator_financeplanner.data.entity.AppSettings

@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettings?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
    
    @Update
    suspend fun updateSettings(settings: AppSettings)
    
    @Query("UPDATE app_settings SET isPinEnabled = :enabled, pinHash = :pinHash, lastUpdated = :date WHERE id = 1")
    suspend fun updatePinSettings(enabled: Boolean, pinHash: String, date: java.util.Date)
    
    @Query("UPDATE app_settings SET isPinEnabled = 0, pinHash = '', lastUpdated = :date WHERE id = 1")
    suspend fun disablePin(date: java.util.Date)
}