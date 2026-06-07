import SwiftUI
import Charts

struct DashboardScreen: View {
    let theme: NMMTheme
    let language: String
    let currency: String
    let transactions: [Transaction]
    let totalBalance: Double
    let monthlyIncome: Double
    let monthlyExpense: Double
    let onNavigateToHistory: () -> Void
    let onNavigateToAdd: (String) -> Void
    let onNavigateToAnalytics: () -> Void

    private var recentTransactions: [Transaction] {
        Array(transactions.prefix(5))
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                headerSection
                summaryCards
                miniChart
                recentSection
            }
            .padding()
        }
        .background(theme.background)
    }

    private var headerSection: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(L("hello_user", lang: language))
                .font(.subheadline)
                .foregroundStyle(theme.secondaryText)
            Text(L("dashboard_title", lang: language))
                .font(.title.bold())
                .foregroundStyle(theme.primaryText)
        }
    }

    private var summaryCards: some View {
        VStack(spacing: 12) {
            SecureContentView {
                VStack(alignment: .leading, spacing: 4) {
                    Text(L("current_balance_cap", lang: language))
                        .font(.caption)
                        .foregroundStyle(theme.secondaryText)
                    Text(CurrencyFormatter.format(totalBalance, symbol: currency))
                        .font(.system(.title, design: .monospaced).bold())
                        .foregroundStyle(theme.primaryText)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                .cosmicCard(theme)
            }

            HStack(spacing: 12) {
                summaryMiniCard(
                    title: L("total_income", lang: language),
                    amount: monthlyIncome,
                    color: NMColors.successGreen
                )
                summaryMiniCard(
                    title: L("total_expense", lang: language),
                    amount: monthlyExpense,
                    color: NMColors.dangerRed
                )
            }
        }
    }

    private func summaryMiniCard(title: String, amount: Double, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption2)
                .foregroundStyle(theme.secondaryText)
            Text(CurrencyFormatter.format(amount, symbol: currency))
                .font(.system(.subheadline, design: .monospaced).bold())
                .foregroundStyle(color)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .cosmicCard(theme)
    }

    private var miniChart: some View {
        let dailyData = dailyCashFlow
        return VStack(alignment: .leading, spacing: 8) {
            Text(L("analytics", lang: language))
                .font(.headline)
                .foregroundStyle(theme.primaryText)
            if dailyData.isEmpty {
                Text(L("no_month_expenses", lang: language))
                    .font(.caption)
                    .foregroundStyle(theme.secondaryText)
                    .cosmicCard(theme)
            } else {
                Chart(dailyData, id: \.day) { item in
                    LineMark(x: .value("Day", item.day), y: .value("Amount", item.amount))
                        .foregroundStyle(theme.accent)
                }
                .frame(height: 120)
                .cosmicCard(theme)
            }
        }
        .onTapGesture { onNavigateToAnalytics() }
    }

    private var dailyCashFlow: [(day: Int, amount: Double)] {
        let calendar = Calendar.current
        let grouped = Dictionary(grouping: transactions.filter { $0.isInPeriod("MONTHLY") }) {
            calendar.component(.day, from: $0.date)
        }
        return grouped.map { day, txs in
            let net = txs.reduce(0.0) { $0 + ($1.isIncome ? $1.amount : -$1.amount) }
            return (day, net)
        }.sorted { $0.day < $1.day }
    }

    private var recentSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(L("recent_transactions", lang: language))
                    .font(.headline)
                    .foregroundStyle(theme.primaryText)
                Spacer()
                Button(L("view_all", lang: language)) { onNavigateToHistory() }
                    .font(.caption)
                    .foregroundStyle(theme.accent)
            }

            if recentTransactions.isEmpty {
                Text(L("no_transactions_registered", lang: language))
                    .font(.caption)
                    .foregroundStyle(theme.secondaryText)
                    .cosmicCard(theme)
            } else {
                ForEach(recentTransactions, id: \.id) { tx in
                    TransactionRow(transaction: tx, currency: currency, theme: theme)
                }
            }
        }
    }
}

struct TransactionRow: View {
    let transaction: Transaction
    let currency: String
    let theme: NMMTheme

    var body: some View {
        HStack {
            Image(systemName: transaction.isIncome ? "arrow.up.circle.fill" : "arrow.down.circle.fill")
                .foregroundStyle(transaction.isIncome ? NMColors.successGreen : NMColors.dangerRed)
            VStack(alignment: .leading) {
                Text(transaction.title)
                    .font(.subheadline.bold())
                    .foregroundStyle(theme.primaryText)
                Text(transaction.category)
                    .font(.caption)
                    .foregroundStyle(theme.secondaryText)
            }
            Spacer()
            Text("\(transaction.isIncome ? "+" : "-")\(CurrencyFormatter.format(transaction.amount, symbol: currency))")
                .font(.system(.subheadline, design: .monospaced))
                .foregroundStyle(transaction.isIncome ? NMColors.successGreen : NMColors.dangerRed)
        }
        .cosmicCard(theme)
    }
}
