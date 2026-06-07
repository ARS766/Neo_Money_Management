import Foundation
import SwiftData

@Model
final class Transaction {
    @Attribute(.unique) var id: UUID
    var title: String
    var amount: Double
    var type: String
    var category: String
    var date: Date
    var note: String?
    var receiptImagePath: String?

    init(
        id: UUID = UUID(),
        title: String,
        amount: Double,
        type: String,
        category: String,
        date: Date = Date(),
        note: String? = nil,
        receiptImagePath: String? = nil
    ) {
        self.id = id
        self.title = title
        self.amount = amount
        self.type = type
        self.category = category
        self.date = date
        self.note = note
        self.receiptImagePath = receiptImagePath
    }

    var isIncome: Bool { type.uppercased() == "INCOME" }
    var isExpense: Bool { type.uppercased() == "EXPENSE" }
}

extension Transaction {
    func isInPeriod(_ periodType: String) -> Bool {
        let calendar = Calendar.current
        let now = Date()
        switch periodType.uppercased() {
        case "DAILY":
            return calendar.isDate(date, inSameDayAs: now)
        case "WEEKLY":
            return calendar.component(.weekOfYear, from: date) == calendar.component(.weekOfYear, from: now)
                && calendar.component(.yearForWeekOfYear, from: date) == calendar.component(.yearForWeekOfYear, from: now)
        case "MONTHLY":
            return calendar.component(.month, from: date) == calendar.component(.month, from: now)
                && calendar.component(.year, from: date) == calendar.component(.year, from: now)
        default:
            return true
        }
    }
}
