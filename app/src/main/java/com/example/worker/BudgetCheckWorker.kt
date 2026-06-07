package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.db.AppDatabase
import com.example.data.repository.FinanceRepository
import com.example.util.NotificationHelper
import com.example.data.model.isInPeriod
import kotlinx.coroutines.flow.first

class BudgetCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val category = inputData.getString("category") ?: return Result.failure()
        
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = FinanceRepository(database.financeDao())

        // Fetch current states
        val budgets = repository.allBudgets.first()
        val transactions = repository.allTransactions.first()

        val budget = budgets.find { it.category.equals(category, ignoreCase = true) } ?: return Result.success()
        val limit = budget.limitAmount
        if (limit <= 0) return Result.success()

        // Calculate cumulative expenses based on budget period type
        val currentExpenses = transactions
            .filter { 
                it.type == "EXPENSE" && 
                it.category.equals(category, ignoreCase = true) &&
                it.isInPeriod(budget.period)
            }
            .sumOf { it.amount }

        val ratio = currentExpenses / limit
        val currencyFormatter = "Rp "
        
        val periodLabel = when (budget.period.uppercase()) {
            "DAILY" -> "harian"
            "WEEKLY" -> "mingguan"
            else -> "bulanan"
        }

        when {
            ratio > 1.0 -> {
                val excessAmount = currentExpenses - limit
                val formattedExcess = String.format("%,.0f", excessAmount).replace(",", ".")
                NotificationHelper.showBudgetNotification(
                    applicationContext,
                    "Peringatan Anggaran Terlampaui!",
                    "Pengeluaran $periodLabel kategori $category telah melebihi anggaran sebesar $currencyFormatter$formattedExcess."
                )
            }
            ratio == 1.0 -> {
                NotificationHelper.showBudgetNotification(
                    applicationContext,
                    "Anggaran Habis!",
                    "Budget $periodLabel $category telah mencapai batas maksimum."
                )
            }
            ratio >= 0.8 -> {
                val remainingAmount = limit - currentExpenses
                val formattedRemaining = String.format("%,.0f", remainingAmount).replace(",", ".")
                NotificationHelper.showBudgetNotification(
                    applicationContext,
                    "Anggaran Hampir Habis!",
                    "Budget $periodLabel $category hampir habis. Sisa anggaran $currencyFormatter$formattedRemaining."
                )
            }
        }

        return Result.success()
    }
}
