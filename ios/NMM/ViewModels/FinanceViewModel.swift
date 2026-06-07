import Foundation
import SwiftData
import SwiftUI

enum SortOption: String, CaseIterable {
    case newest, oldest, highestAmount, lowestAmount
}

struct OverlayState {
    var isVisible = false
    var isSuccess = true
    var message = ""
}

@MainActor
@Observable
final class FinanceViewModel {
    private let repository: FinanceRepository
    let preferences: UserPreferences

    var transactions: [Transaction] = []
    var budgets: [Budget] = []
    var savingsGoals: [SavingsGoal] = []

    var searchQuery = ""
    var filterCategory: String?
    var filterType: String?
    var sortOption: SortOption = .newest

    var overlayState = OverlayState()

    init(modelContext: ModelContext) {
        repository = FinanceRepository(modelContext: modelContext)
        preferences = UserPreferences()
        reloadData()
    }

    var filteredTransactions: [Transaction] {
        var list = transactions

        if !searchQuery.isEmpty {
            list = list.filter {
                $0.title.localizedCaseInsensitiveContains(searchQuery)
                || ($0.note ?? "").localizedCaseInsensitiveContains(searchQuery)
                || $0.category.localizedCaseInsensitiveContains(searchQuery)
            }
        }
        if let cat = filterCategory {
            list = list.filter { $0.category.caseInsensitiveCompare(cat) == .orderedSame }
        }
        if let type = filterType {
            list = list.filter { $0.type.caseInsensitiveCompare(type) == .orderedSame }
        }

        switch sortOption {
        case .newest: return list.sorted { $0.date > $1.date }
        case .oldest: return list.sorted { $0.date < $1.date }
        case .highestAmount: return list.sorted { $0.amount > $1.amount }
        case .lowestAmount: return list.sorted { $0.amount < $1.amount }
        }
    }

    var totalBalance: Double {
        transactions.reduce(0) { sum, t in
            t.isIncome ? sum + t.amount : sum - t.amount
        }
    }

    var monthlyIncome: Double {
        transactions.filter { $0.isIncome && $0.isInPeriod("MONTHLY") }.reduce(0) { $0 + $1.amount }
    }

    var monthlyExpense: Double {
        transactions.filter { $0.isExpense && $0.isInPeriod("MONTHLY") }.reduce(0) { $0 + $1.amount }
    }

    func reloadData() {
        transactions = (try? repository.fetchAllTransactions()) ?? []
        budgets = (try? repository.fetchAllBudgets()) ?? []
        savingsGoals = (try? repository.fetchAllSavingsGoals()) ?? []
    }

    func insertTransaction(title: String, amount: Double, date: Date, category: String, note: String, type: String) {
        guard !title.isEmpty, amount > 0 else {
            showOverlay(success: false, message: "Judul dan nominal harus diisi dengan benar.")
            return
        }
        let transaction = Transaction(title: title, amount: amount, type: type, category: category, date: date, note: note)
        do {
            try repository.insertTransaction(transaction)
            reloadData()
            checkBudgetAfterExpense(transaction)
            showOverlay(success: true, message: "Transaksi berhasil dicatat")
        } catch {
            showOverlay(success: false, message: error.localizedDescription)
        }
    }

    func deleteTransaction(_ transaction: Transaction) {
        do {
            try repository.deleteTransaction(transaction)
            reloadData()
        } catch {
            showOverlay(success: false, message: error.localizedDescription)
        }
    }

    func saveBudget(category: String, limit: Double, period: String = "MONTHLY") {
        guard limit > 0 else { return }
        let budget = Budget(category: category, limitAmount: limit, period: period)
        do {
            try repository.insertBudget(budget)
            reloadData()
            showOverlay(success: true, message: L("save_budget_dialog_title", lang: preferences.selectedLanguage))
        } catch {
            showOverlay(success: false, message: error.localizedDescription)
        }
    }

    func deleteBudget(_ budget: Budget) {
        do {
            try repository.deleteBudget(budget)
            reloadData()
        } catch {
            showOverlay(success: false, message: error.localizedDescription)
        }
    }

    func saveSavingsGoal(title: String, target: Double, current: Double, deadline: Date) {
        guard !title.isEmpty, target > 0 else { return }
        let goal = SavingsGoal(title: title, targetAmount: target, currentAmount: current, deadline: deadline)
        do {
            try repository.insertSavingsGoal(goal)
            reloadData()
            showOverlay(success: true, message: L("add_savings_dialog_title", lang: preferences.selectedLanguage))
        } catch {
            showOverlay(success: false, message: error.localizedDescription)
        }
    }

    func updateSavingsContribution(_ goal: SavingsGoal, amount: Double) {
        goal.currentAmount = max(0, goal.currentAmount + amount)
        do {
            try repository.insertSavingsGoal(goal)
            reloadData()
        } catch {
            showOverlay(success: false, message: error.localizedDescription)
        }
    }

    func deleteSavingsGoal(_ goal: SavingsGoal) {
        do {
            try repository.deleteSavingsGoal(goal)
            reloadData()
        } catch {
            showOverlay(success: false, message: error.localizedDescription)
        }
    }

    func resetAllData() {
        do {
            try repository.resetDatabase()
            preferences.clearAll()
            reloadData()
        } catch {
            showOverlay(success: false, message: error.localizedDescription)
        }
    }

    func exportCSV() -> URL? {
        var csv = "Title,Amount,Type,Category,Date,Note\n"
        for t in transactions {
            let dateStr = ISO8601DateFormatter().string(from: t.date)
            csv += "\"\(t.title)\",\(t.amount),\(t.type),\(t.category),\(dateStr),\"\(t.note ?? "")\"\n"
        }
        let url = FileManager.default.temporaryDirectory.appendingPathComponent("nmm_export.csv")
        try? csv.write(to: url, atomically: true, encoding: .utf8)
        return url
    }

    private func checkBudgetAfterExpense(_ transaction: Transaction) {
        guard transaction.isExpense else { return }
        for budget in budgets where budget.category == transaction.category {
            let spent = budget.spentAmount(from: transactions)
            if spent > budget.limitAmount {
                NotificationHelper.notifyBudgetExceeded(category: budget.category, language: preferences.selectedLanguage)
            }
        }
    }

    private func showOverlay(success: Bool, message: String) {
        overlayState = OverlayState(isVisible: true, isSuccess: success, message: message)
        Task {
            try? await Task.sleep(for: .seconds(2))
            overlayState.isVisible = false
        }
    }
}
