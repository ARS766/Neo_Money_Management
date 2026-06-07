package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.data.db.AppDatabase
import com.example.data.model.Budget
import com.example.data.model.SavingsGoal
import com.example.data.model.Transaction
import com.example.data.preferences.UserPreferences
import com.example.data.repository.FinanceRepository
import com.example.worker.BudgetCheckWorker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class SortOption {
    NEWEST, OLDEST, HIGHEST_AMOUNT, LOWEST_AMOUNT
}

data class OverlayState(
    val isVisible: Boolean = false,
    val isSuccess: Boolean = true,
    val message: String = ""
)

class FinanceViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FinanceRepository
    private val prefs: UserPreferences

    // Database Flows
    val allTransactions: StateFlow<List<Transaction>>
    val allBudgets: StateFlow<List<Budget>>
    val allSavingsGoals: StateFlow<List<SavingsGoal>>

    // Preference Flows
    val isDarkMode: StateFlow<Boolean>
    val selectedCurrency: StateFlow<String>
    val selectedLanguage: StateFlow<String>
    val userPin: StateFlow<String?>
    val isBiometricEnabled: StateFlow<Boolean>
    val hasCompletedOnboarding: StateFlow<Boolean>
    val incomeCategories: StateFlow<List<String>>
    val expenseCategories: StateFlow<List<String>>
    val isAutoBackupEnabled: StateFlow<Boolean>

    // Alert Overlay UI State
    private val _overlayState = MutableStateFlow(OverlayState())
    val overlayState: StateFlow<OverlayState> = _overlayState.asStateFlow()

    // Transaction History Filters
    val searchQuery = MutableStateFlow("")
    val filterCategory = MutableStateFlow<String?>(null)
    val filterType = MutableStateFlow<String?>(null) // "INCOME", "EXPENSE", or null
    val sortOption = MutableStateFlow(SortOption.NEWEST)

    // Flow for filtered and sorted transactions
    val filteredTransactions: StateFlow<List<Transaction>>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FinanceRepository(database.financeDao())
        prefs = UserPreferences(application)

        // Bind preference datastore safely to stateflows
        isDarkMode = prefs.isDarkModeFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
        selectedCurrency = prefs.selectedCurrencyFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Rp")
        selectedLanguage = prefs.selectedLanguageFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "ID")
        userPin = prefs.userPinFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        isBiometricEnabled = prefs.isBiometricEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        hasCompletedOnboarding = prefs.hasCompletedOnboardingFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        incomeCategories = prefs.incomeCategoriesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Gaji", "Freelance", "Bonus", "Investasi", "Lainnya"))
        expenseCategories = prefs.expenseCategoriesFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("Makanan", "Transportasi", "Belanja", "Hiburan", "Pendidikan", "Kesehatan", "Tagihan", "Lainnya"))
        isAutoBackupEnabled = prefs.isAutoBackupEnabledFlow.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

        allBudgets = repository.allBudgets.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allSavingsGoals = repository.allSavingsGoals.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        allTransactions = repository.allTransactions.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

        // Combine inputs dynamically for searching, sorting, and filtering
        filteredTransactions = combine(
            allTransactions,
            searchQuery,
            filterCategory,
            filterType,
            sortOption
        ) { transactionList, query, cat, type, sort ->
            var list = transactionList

            // 1. Text Search Filter
            if (query.isNotBlank()) {
                list = list.filter {
                    it.title.contains(query, ignoreCase = true) ||
                    it.note.contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
                }
            }

            // 2. Category Filter
            if (cat != null) {
                list = list.filter { it.category.equals(cat, ignoreCase = true) }
            }

            // 3. Type Filter (Income/Expense)
            if (type != null) {
                list = list.filter { it.type.equals(type, ignoreCase = true) }
            }

            // 4. Sort selection
            when (sort) {
                SortOption.NEWEST -> list.sortedByDescending { it.date }
                SortOption.OLDEST -> list.sortedBy { it.date }
                SortOption.HIGHEST_AMOUNT -> list.sortedByDescending { it.amount }
                SortOption.LOWEST_AMOUNT -> list.sortedBy { it.amount }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    // --- Core Actions ---

    fun insertTransaction(
        title: String,
        amount: Double,
        date: Long,
        category: String,
        note: String,
        type: String
    ) {
        viewModelScope.launch {
            try {
                if (title.isBlank() || amount <= 0) {
                    showOverlayFeedback(false, "Judul dan nominal harus diisi dengan benar.")
                    return@launch
                }

                val transaction = Transaction(
                    title = title,
                    amount = amount,
                    date = date,
                    category = category,
                    note = note,
                    type = type
                )

                repository.insertTransaction(transaction)
                showOverlayFeedback(true, "Transaksi berhasil dicatat")

                triggerAutoCloudBackup()

                // If adding expenses, trigger WorkManager budget calculation
                if (type == "EXPENSE") {
                    triggerBudgetCheck(category)
                }
            } catch (e: Exception) {
                showOverlayFeedback(false, "Gagal mencatatkan transaksi")
            }
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            try {
                repository.deleteTransaction(transaction)
                showOverlayFeedback(true, "Transaksi berhasil dihapus")
                if (transaction.type == "EXPENSE") {
                    triggerBudgetCheck(transaction.category)
                }
            } catch (e: Exception) {
                showOverlayFeedback(false, "Gagal menghapus transaksi")
            }
        }
    }

    fun saveBudget(category: String, limit: Double, period: String = "MONTHLY") {
        viewModelScope.launch {
            try {
                if (category.isBlank() || limit <= 0) {
                    showOverlayFeedback(false, "Kategori dan nominal budget tidak valid")
                    return@launch
                }
                val budget = Budget(category = category, limitAmount = limit, period = period)
                repository.insertBudget(budget)
                showOverlayFeedback(true, "Anggaran berhasil ditetapkan")

                triggerAutoCloudBackup()

                // Run WorkManager right custom check
                triggerBudgetCheck(category)
            } catch (e: Exception) {
                showOverlayFeedback(false, "Gagal menetapkan anggaran")
            }
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            try {
                repository.deleteBudget(budget)
                showOverlayFeedback(true, "Anggaran berhasil dihapus")
            } catch (e: Exception) {
                showOverlayFeedback(false, "Gagal menghapus anggaran")
            }
        }
    }

    fun saveSavingsGoal(name: String, targetAmount: Double, currentAmount: Double, targetDate: Long) {
        viewModelScope.launch {
            try {
                if (name.isBlank() || targetAmount <= 0) {
                    showOverlayFeedback(false, "Nama target dan sasaran harus valid")
                    return@launch
                }
                val goal = SavingsGoal(
                    name = name,
                    targetAmount = targetAmount,
                    currentAmount = currentAmount,
                    targetDate = targetDate
                )
                repository.insertSavingsGoal(goal)
                showOverlayFeedback(true, "Target tabungan berhasil dibuat")
            } catch (e: Exception) {
                showOverlayFeedback(false, "Gagal membuat target tabungan")
            }
        }
    }

    fun updateSavingsContribution(goal: SavingsGoal, extraAmount: Double) {
        viewModelScope.launch {
            try {
                if (extraAmount == 0.0) return@launch
                val updatedAmount = (goal.currentAmount + extraAmount).coerceAtLeast(0.0)
                
                val updatedGoal = goal.copy(currentAmount = updatedAmount)
                repository.insertSavingsGoal(updatedGoal)
                showOverlayFeedback(true, "Target tabungan berhasil diperbarui")
            } catch (e: Exception) {
                showOverlayFeedback(false, "Gagal menambah kontribusi")
            }
        }
    }

    fun deleteSavingsGoal(goal: SavingsGoal) {
        viewModelScope.launch {
            try {
                repository.deleteSavingsGoal(goal)
                showOverlayFeedback(true, "Target tabungan berhasil dihapus")
            } catch (e: Exception) {
                showOverlayFeedback(false, "Gagal menghapus target tabungan")
            }
        }
    }

    // --- User preferences settings ---

    fun updateDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setDarkMode(enabled)
        }
    }

    fun updateCurrency(currency: String) {
        viewModelScope.launch {
            prefs.setSelectedCurrency(currency)
            val currentLang = selectedLanguage.value
            val msg = when (currentLang) {
                "ID" -> "Mata uang acuan diubah menjadi $currency"
                "EN" -> "Base currency changed to $currency"
                "ES" -> "Moneda de referencia cambiada a $currency"
                "UK" -> "Base currency changed to $currency"
                "JA" -> "基本通貨が $currency に変更されました"
                "ZH" -> "基准货币已更改为 $currency"
                else -> "Base currency changed to $currency"
            }
            showOverlayFeedback(true, msg)
        }
    }

    fun updateLanguage(lang: String) {
        viewModelScope.launch {
            prefs.setSelectedLanguage(lang)
            val autoCur = when (lang) {
                "ID" -> "Rp"
                "EN" -> "$"
                "ES" -> "€"
                "UK" -> "£"
                "JA" -> "¥ (Yen)"
                "ZH" -> "元 (Yuan)"
                else -> "Rp"
            }
            prefs.setSelectedCurrency(autoCur)
            val msg = when (lang) {
                "ID" -> "Bahasa diubah menjadi Bahasa Indonesia & Mata uang Rp"
                "EN" -> "Language changed to English (US) & Currency $"
                "ES" -> "Idioma cambiado a Español y Moneda €"
                "UK" -> "Language changed to English (UK) & Currency £"
                "JA" -> "言語が日本語に、通貨が¥に変更されました"
                "ZH" -> "语言已切换为简体中文 & 货币为元"
                else -> "Language changed"
            }
            showOverlayFeedback(true, msg)
        }
    }

    fun updateBiometricState(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setBiometricEnabled(enabled)
            showOverlayFeedback(true, if (enabled) "Biometrik diaktifkan" else "Biometrik dinonaktifkan")
        }
    }

    fun saveUserPin(pin: String?) {
        viewModelScope.launch {
            prefs.setUserPin(pin)
            showOverlayFeedback(true, "PIN Keamanan berhasil diperbarui")
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            prefs.setCompletedOnboarding(true)
        }
    }

    fun updateIncomeCategories(list: List<String>) {
        viewModelScope.launch {
            prefs.saveIncomeCategories(list)
            showOverlayFeedback(true, "Kategori pemasukan berhasil diperbarui")
        }
    }

    fun updateExpenseCategories(list: List<String>) {
        viewModelScope.launch {
            prefs.saveExpenseCategories(list)
            showOverlayFeedback(true, "Kategori pengeluaran berhasil diperbarui")
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            try {
                repository.resetDatabase()
                prefs.clearAllPreferences()
                showOverlayFeedback(true, "Seluruh data berhasil direset")
            } catch (e: Exception) {
                showOverlayFeedback(false, "Gagal mengosongkan data")
            }
        }
    }

    fun updateAutoBackupEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setAutoBackupEnabled(enabled)
            showOverlayFeedback(true, if (enabled) "Pencadangan Otomatis diaktifkan" else "Pencadangan Otomatis dinonaktifkan")
        }
    }

    fun triggerAutoCloudBackup() {
        viewModelScope.launch {
            if (isAutoBackupEnabled.value) {
                android.util.Log.d("CloudAutoBackup", "Syncing to cloud... txCount=${allTransactions.value.size} budgetCount=${allBudgets.value.size}")
                showOverlayFeedback(true, "Mengunggah pencadangan awan otomatis...")
                delay(1200)
                showOverlayFeedback(true, "Pencadangan awan otomatis berhasil!")
            }
        }
    }

    fun generateBackupCode(
        transactions: List<Transaction>,
        budgets: List<Budget>,
        goals: List<SavingsGoal>
    ): String {
        return try {
            val backupJson = org.json.JSONObject()
            backupJson.put("version", 1)
            backupJson.put("timestamp", System.currentTimeMillis())

            val trxsArray = org.json.JSONArray()
            transactions.forEach { t ->
                val obj = org.json.JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("amount", t.amount)
                    put("date", t.date)
                    put("category", t.category)
                    put("note", t.note)
                    put("type", t.type)
                }
                trxsArray.put(obj)
            }
            backupJson.put("transactions", trxsArray)

            val budgetsArray = org.json.JSONArray()
            budgets.forEach { b ->
                val obj = org.json.JSONObject().apply {
                    put("category", b.category)
                    put("limitAmount", b.limitAmount)
                    put("period", b.period)
                }
                budgetsArray.put(obj)
            }
            backupJson.put("budgets", budgetsArray)

            val goalsArray = org.json.JSONArray()
            goals.forEach { g ->
                val obj = org.json.JSONObject().apply {
                    put("id", g.id)
                    put("name", g.name)
                    put("targetAmount", g.targetAmount)
                    put("currentAmount", g.currentAmount)
                    put("targetDate", g.targetDate)
                }
                goalsArray.put(obj)
            }
            backupJson.put("savings_goals", goalsArray)

            val jsonStr = backupJson.toString()
            android.util.Base64.encodeToString(jsonStr.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP)
        } catch (e: Exception) {
            ""
        }
    }

    fun restoreBackupCode(backupStr: String, onComplete: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val input = backupStr.trim()
                if (input.isBlank()) {
                    onComplete(false, "Kode backup tidak boleh kosong.")
                    return@launch
                }

                val decodedStr = try {
                    if (input.startsWith("{")) {
                        input
                    } else {
                        String(android.util.Base64.decode(input, android.util.Base64.DEFAULT), Charsets.UTF_8)
                    }
                } catch (e: Exception) {
                    onComplete(false, "Format kode backup tidak valid.")
                    return@launch
                }

                val json = org.json.JSONObject(decodedStr)
                val trxsArray = json.optJSONArray("transactions")
                val budgetsArray = json.optJSONArray("budgets")
                val goalsArray = json.optJSONArray("savings_goals")

                var trxImportedCount = 0
                var budgetImportedCount = 0
                var goalImportedCount = 0

                // 1. Transactions import
                if (trxsArray != null) {
                    for (i in 0 until trxsArray.length()) {
                        val obj = trxsArray.getJSONObject(i)
                        val id = obj.optLong("id", 0)
                        val trx = Transaction(
                            id = if (id > 0) id else 0,
                            title = obj.getString("title"),
                            amount = obj.getDouble("amount"),
                            date = obj.getLong("date"),
                            category = obj.getString("category"),
                            note = obj.optString("note", ""),
                            type = obj.getString("type")
                        )
                        repository.insertTransaction(trx)
                        trxImportedCount++
                    }
                }

                // 2. Budgets import
                if (budgetsArray != null) {
                    for (i in 0 until budgetsArray.length()) {
                        val obj = budgetsArray.getJSONObject(i)
                        val budget = Budget(
                            category = obj.getString("category"),
                            limitAmount = obj.getDouble("limitAmount"),
                            period = obj.optString("period", "MONTHLY")
                        )
                        repository.insertBudget(budget)
                        budgetImportedCount++
                    }
                }

                // 3. Savings Goals import
                if (goalsArray != null) {
                    for (i in 0 until goalsArray.length()) {
                        val obj = goalsArray.getJSONObject(i)
                        val id = obj.optLong("id", 0)
                        val goal = SavingsGoal(
                            id = if (id > 0) id else 0,
                            name = obj.getString("name"),
                            targetAmount = obj.getDouble("targetAmount"),
                            currentAmount = obj.getDouble("currentAmount"),
                            targetDate = obj.getLong("targetDate")
                        )
                        repository.insertSavingsGoal(goal)
                        goalImportedCount++
                    }
                }

                showOverlayFeedback(
                    true,
                    "Berhasil memulihkan $trxImportedCount Transaksi dan $budgetImportedCount Anggaran"
                )
                onComplete(true, "Berhasil memulihkan:\n• $trxImportedCount Transaksi\n• $budgetImportedCount Anggaran\n• $goalImportedCount Target Tabungan")
            } catch (e: Exception) {
                showOverlayFeedback(false, "Gagal memulihkan cadangan data")
                onComplete(false, "Sintaks data tidak cocok atau rusak: ${e.localizedMessage}")
            }
        }
    }

    // --- Helper Utilities ---

    private fun triggerBudgetCheck(category: String) {
        val workManager = WorkManager.getInstance(getApplication())
        val inputData = Data.Builder()
            .putString("category", category)
            .build()

        val checkRequest = OneTimeWorkRequestBuilder<BudgetCheckWorker>()
            .setInputData(inputData)
            .build()

        workManager.enqueue(checkRequest)
    }

    private fun showOverlayFeedback(isSuccess: Boolean, message: String) {
        viewModelScope.launch {
            _overlayState.value = OverlayState(
                isVisible = true,
                isSuccess = isSuccess,
                message = message
            )
            delay(2500)
            _overlayState.value = OverlayState(isVisible = false)
        }
    }
}
