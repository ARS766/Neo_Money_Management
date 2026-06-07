import Foundation
import SwiftData

@MainActor
final class FinanceRepository {
    private let modelContext: ModelContext

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    // MARK: - Transactions

    func fetchAllTransactions() throws -> [Transaction] {
        let descriptor = FetchDescriptor<Transaction>(sortBy: [SortDescriptor(\.date, order: .reverse)])
        return try modelContext.fetch(descriptor)
    }

    func insertTransaction(_ transaction: Transaction) throws {
        modelContext.insert(transaction)
        try modelContext.save()
    }

    func deleteTransaction(_ transaction: Transaction) throws {
        modelContext.delete(transaction)
        try modelContext.save()
    }

    // MARK: - Budgets

    func fetchAllBudgets() throws -> [Budget] {
        let descriptor = FetchDescriptor<Budget>(sortBy: [SortDescriptor(\.category)])
        return try modelContext.fetch(descriptor)
    }

    func insertBudget(_ budget: Budget) throws {
        modelContext.insert(budget)
        try modelContext.save()
    }

    func deleteBudget(_ budget: Budget) throws {
        modelContext.delete(budget)
        try modelContext.save()
    }

    func budget(for category: String) throws -> Budget? {
        let descriptor = FetchDescriptor<Budget>(
            predicate: #Predicate { $0.category == category }
        )
        return try modelContext.fetch(descriptor).first
    }

    // MARK: - Savings Goals

    func fetchAllSavingsGoals() throws -> [SavingsGoal] {
        let descriptor = FetchDescriptor<SavingsGoal>(sortBy: [SortDescriptor(\.deadline)])
        return try modelContext.fetch(descriptor)
    }

    func insertSavingsGoal(_ goal: SavingsGoal) throws {
        modelContext.insert(goal)
        try modelContext.save()
    }

    func deleteSavingsGoal(_ goal: SavingsGoal) throws {
        modelContext.delete(goal)
        try modelContext.save()
    }

    // MARK: - Reset

    func resetDatabase() throws {
        try fetchAllTransactions().forEach { modelContext.delete($0) }
        try fetchAllBudgets().forEach { modelContext.delete($0) }
        try fetchAllSavingsGoals().forEach { modelContext.delete($0) }
        try modelContext.save()
    }
}
