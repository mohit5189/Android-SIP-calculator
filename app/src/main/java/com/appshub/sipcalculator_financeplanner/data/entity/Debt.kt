package com.appshub.sipcalculator_financeplanner.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import java.util.Date

@Entity(
    tableName = "debts",
    foreignKeys = [
        ForeignKey(
            entity = Goal::class,
            parentColumns = ["goalId"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Debt(
    @PrimaryKey
    val debtId: String,
    val goalId: String,
    val debtType: DebtType,
    val customDebtName: String = "",
    val amount: Double,
    val emiAmount: Double = 0.0,
    val interestRate: Double = 0.0,
    val notes: String = "",
    val lastUpdated: Date,
    val createdDate: Date
)

enum class DebtType {
    HOME_LOAN,
    CAR_LOAN,
    PERSONAL_LOAN,
    CREDIT_CARD,
    EDUCATION_LOAN,
    BUSINESS_LOAN,
    CUSTOM
}

fun DebtType.getDisplayName(): String {
    return when (this) {
        DebtType.HOME_LOAN -> "Home Loan"
        DebtType.CAR_LOAN -> "Car Loan"
        DebtType.PERSONAL_LOAN -> "Personal Loan"
        DebtType.CREDIT_CARD -> "Credit Card Debt"
        DebtType.EDUCATION_LOAN -> "Education Loan"
        DebtType.BUSINESS_LOAN -> "Business Loan"
        DebtType.CUSTOM -> "Other Debts"
    }
}

fun DebtType.getIcon(): String {
    return when (this) {
        DebtType.HOME_LOAN -> "🏠"
        DebtType.CAR_LOAN -> "🚗"
        DebtType.PERSONAL_LOAN -> "👤"
        DebtType.CREDIT_CARD -> "💳"
        DebtType.EDUCATION_LOAN -> "📚"
        DebtType.BUSINESS_LOAN -> "💼"
        DebtType.CUSTOM -> "👥"
    }
}