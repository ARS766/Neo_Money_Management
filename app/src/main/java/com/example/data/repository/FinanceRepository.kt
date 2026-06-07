package com.example.data.repository

import com.example.data.db.FinanceDao
import com.example.data.model.Budget
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import kotlinx.coroutines.flow.Flow

class FinanceRepository(private val financeDao: FinanceDao) {

    // Transactions
    val allTransactions: Flow<List<Transaction>> = financeDao.getAllTransactions()

    suspend fun getTransactionById(id: Long): Transaction? {
        return financeDao.getTransactionById(id)
    }

    suspend fun insertTransaction(transaction: Transaction) {
        financeDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        financeDao.deleteTransaction(transaction)
    }

    suspend fun resetDatabase() {
        financeDao.clearAllTransactions()
        financeDao.clearAllBudgets()
        financeDao.clearAllSavingsGoals()
    }

    // Budgets
    val allBudgets: Flow<List<Budget>> = financeDao.getAllBudgets()

    suspend fun getBudgetByCategory(category: String): Budget? {
        return financeDao.getBudgetByCategory(category)
    }

    suspend fun insertBudget(budget: Budget) {
        financeDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: Budget) {
        financeDao.deleteBudget(budget)
    }

    // Savings Goals
    val allSavingsGoals: Flow<List<SavingsGoal>> = financeDao.getAllSavingsGoals()

    suspend fun getSavingsGoalById(id: Long): SavingsGoal? {
        return financeDao.getSavingsGoalById(id)
    }

    suspend fun insertSavingsGoal(savingsGoal: SavingsGoal) {
        financeDao.insertSavingsGoal(savingsGoal)
    }

    suspend fun deleteSavingsGoal(savingsGoal: SavingsGoal) {
        financeDao.deleteSavingsGoal(savingsGoal)
    }
}
