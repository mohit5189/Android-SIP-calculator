package com.appshub.sipcalculator_financeplanner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey
    val goalId: String,
    val goalName: String,
    val targetAmount: Double,
    val targetDate: Date,
    val monthlySipNeeded: Double,
    val createdDate: Date,
    val status: GoalStatus,
    val goalType: GoalType,
    val description: String = "",
    val currentAmount: Double = 0.0,
    val lastUpdated: Date = Date()
)

enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    PAUSED
}

enum class GoalType {
    HOUSE,
    CAR,
    EDUCATION,
    RETIREMENT,
    MARRIAGE,
    VACATION,
    EMERGENCY_FUND,
    CUSTOM
}