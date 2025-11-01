package com.appshub.sipcalculator_financeplanner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "savings",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["goalId"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Saving(
    @PrimaryKey
    val savingId: String,
    val goalId: String,
    val category: SavingCategory,
    val customCategoryName: String = "",
    val amount: Double,
    val notes: String = "",
    val lastUpdated: Date,
    val createdDate: Date
)

enum class SavingCategory {
    BANK_SAVINGS,
    FIXED_DEPOSITS,
    MUTUAL_FUNDS,
    STOCKS,
    PPF_ELSS,
    REAL_ESTATE,
    GOLD_SILVER,
    CRYPTO,
    PF_GRATUITY,
    CUSTOM
}

fun SavingCategory.getDisplayName(): String {
    return when (this) {
        SavingCategory.BANK_SAVINGS -> "Bank Savings"
        SavingCategory.FIXED_DEPOSITS -> "Fixed Deposits"
        SavingCategory.MUTUAL_FUNDS -> "Mutual Funds"
        SavingCategory.STOCKS -> "Stocks"
        SavingCategory.PPF_ELSS -> "PPF/ELSS"
        SavingCategory.REAL_ESTATE -> "Real Estate"
        SavingCategory.GOLD_SILVER -> "Gold/Silver"
        SavingCategory.CRYPTO -> "Cryptocurrency"
        SavingCategory.PF_GRATUITY -> "PF/Gratuity"
        SavingCategory.CUSTOM -> "Other Investments"
    }
}

fun SavingCategory.getIcon(): String {
    return when (this) {
        SavingCategory.BANK_SAVINGS -> "🏦"
        SavingCategory.FIXED_DEPOSITS -> "💰"
        SavingCategory.MUTUAL_FUNDS -> "📈"
        SavingCategory.STOCKS -> "📊"
        SavingCategory.PPF_ELSS -> "🛡️"
        SavingCategory.REAL_ESTATE -> "🏠"
        SavingCategory.GOLD_SILVER -> "🥇"
        SavingCategory.CRYPTO -> "₿"
        SavingCategory.PF_GRATUITY -> "💼"
        SavingCategory.CUSTOM -> "🎯"
    }
}