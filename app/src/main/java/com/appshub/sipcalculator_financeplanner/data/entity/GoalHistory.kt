package com.appshub.sipcalculator_financeplanner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "goal_history",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["goalId"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GoalHistory(
    @PrimaryKey
    val historyId: String,
    val goalId: String,
    val monthYear: String, // "Jan 2024"
    val totalSavings: Double,
    val totalDebt: Double,
    val netWorth: Double,
    val progressPercentage: Double,
    val recordedDate: Date
)

data class GoalProgress(
    val goal: Goal,
    val totalSavings: Double,
    val totalDebts: Double,
    val netWorth: Double,
    val progressPercentage: Double,
    val remainingAmount: Double,
    val monthsToGo: Int,
    val isOnTrack: Boolean,
    val monthlyProgressNeeded: Double
)