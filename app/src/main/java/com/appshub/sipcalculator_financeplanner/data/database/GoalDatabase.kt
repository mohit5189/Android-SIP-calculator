package com.appshub.sipcalculator_financeplanner.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.appshub.sipcalculator_financeplanner.data.entity.*
import com.appshub.sipcalculator_financeplanner.data.dao.*

@Database(
    entities = [Goal::class, Saving::class, Debt::class, GoalHistory::class, AppSettings::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GoalDatabase : RoomDatabase() {
    
    abstract fun goalDao(): GoalDao
    abstract fun savingDao(): SavingDao
    abstract fun debtDao(): DebtDao
    abstract fun goalHistoryDao(): GoalHistoryDao
    abstract fun appSettingsDao(): AppSettingsDao
    
    companion object {
        @Volatile
        private var INSTANCE: GoalDatabase? = null
        
        fun getDatabase(context: Context): GoalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoalDatabase::class.java,
                    "goal_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}