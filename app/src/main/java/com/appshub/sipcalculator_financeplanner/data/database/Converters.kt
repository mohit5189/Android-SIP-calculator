package com.appshub.sipcalculator_financeplanner.data.database

import androidx.room.TypeConverter
import com.appshub.sipcalculator_financeplanner.data.entity.*
import java.util.Date

class Converters {
    
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }
    
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
    
    @TypeConverter
    fun fromGoalStatus(status: GoalStatus): String {
        return status.name
    }
    
    @TypeConverter
    fun toGoalStatus(status: String): GoalStatus {
        return GoalStatus.valueOf(status)
    }
    
    @TypeConverter
    fun fromGoalType(type: GoalType): String {
        return type.name
    }
    
    @TypeConverter
    fun toGoalType(type: String): GoalType {
        return GoalType.valueOf(type)
    }
    
    @TypeConverter
    fun fromSavingCategory(category: SavingCategory): String {
        return category.name
    }
    
    @TypeConverter
    fun toSavingCategory(category: String): SavingCategory {
        return SavingCategory.valueOf(category)
    }
    
    @TypeConverter
    fun fromDebtType(type: DebtType): String {
        return type.name
    }
    
    @TypeConverter
    fun toDebtType(type: String): DebtType {
        return DebtType.valueOf(type)
    }
}