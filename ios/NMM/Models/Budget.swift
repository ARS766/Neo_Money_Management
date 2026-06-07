import Foundation
import SwiftData

@Model
final class Budget {
    @Attribute(.unique) var id: UUID
    var category: String
    var limitAmount: Double
    var period: String
    var monthYear: String

    init(
        id: UUID = UUID(),
        category: String,
        limitAmount: Double,
        period: String = "MONTHLY",
        monthYear: String = Budget.currentMonthYear
    ) {
        self.id = id
        self.category = category
        self.limitAmount = limitAmount
        self.period = period
        self.monthYear = monthYear
    }

    static var currentMonthYear: String {
        let formatter = DateFormatter()
        formatter.dateFormat = "MM-yyyy"
        return formatter.string(from: Date())
    }

    func spentAmount(from transactions: [Transaction]) -> Double {
        transactions
            .filter { $0.isExpense && $0.category == category && $0.isInPeriod(period) }
            .reduce(0) { $0 + $1.amount }
    }

    var usageRatio: Double {
        guard limitAmount > 0 else { return 0 }
        return min(spentAmount(from: []), limitAmount) / limitAmount
    }
}
