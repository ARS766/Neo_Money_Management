package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val date: Long, // Milliseconds timestamp
    val category: String,
    val note: String,
    val type: String // "INCOME" or "EXPENSE"
)

@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val category: String,
    val limitAmount: Double,
    val period: String = "MONTHLY" // "DAILY", "WEEKLY", "MONTHLY"
)

fun Transaction.isInPeriod(periodType: String): Boolean {
    val transCal = java.util.Calendar.getInstance().apply { timeInMillis = this@isInPeriod.date }
    val nowCal = java.util.Calendar.getInstance()
    
    return when (periodType.uppercase()) {
        "DAILY" -> {
            transCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
            transCal.get(java.util.Calendar.DAY_OF_YEAR) == nowCal.get(java.util.Calendar.DAY_OF_YEAR)
        }
        "WEEKLY" -> {
            transCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
            transCal.get(java.util.Calendar.WEEK_OF_YEAR) == nowCal.get(java.util.Calendar.WEEK_OF_YEAR)
        }
        "MONTHLY" -> {
            transCal.get(java.util.Calendar.YEAR) == nowCal.get(java.util.Calendar.YEAR) &&
            transCal.get(java.util.Calendar.MONTH) == nowCal.get(java.util.Calendar.MONTH)
        }
        else -> true
    }
}

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val targetDate: Long // Milliseconds timestamp
)
