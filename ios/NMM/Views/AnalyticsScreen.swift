import SwiftUI
import Charts

enum AnalyticsPeriod: String, CaseIterable {
    case week, month, year
}

struct AnalyticsScreen: View {
    let theme: NMMTheme
    let language: String
    let currency: String
    let transactions: [Transaction]
    let onBack: () -> Void

    @State private var period: AnalyticsPeriod = .month

    private var filteredTransactions: [Transaction] {
        switch period {
        case .week: return transactions.filter { $0.isInPeriod("WEEKLY") }
        case .month: return transactions.filter { $0.isInPeriod("MONTHLY") }
        case .year: return transactions.filter {
            Calendar.current.component(.year, from: $0.date) == Calendar.current.component(.year, from: Date())
        }
        }
    }

    private var categoryData: [(category: String, amount: Double)] {
        Dictionary(grouping: filteredTransactions.filter { $0.isExpense }) { $0.category }
            .map { ($0.key, $0.value.reduce(0) { $0 + $1.amount }) }
            .sorted { $0.amount > $1.amount }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    Picker("Period", selection: $period) {
                        Text(L("filter_week", lang: language)).tag(AnalyticsPeriod.week)
                        Text(L("filter_month", lang: language)).tag(AnalyticsPeriod.month)
                        Text(L("filter_year", lang: language)).tag(AnalyticsPeriod.year)
                    }
                    .pickerStyle(.segmented)
                    .padding(.horizontal)

                    if categoryData.isEmpty {
                        Text(L("no_month_expenses", lang: language))
                            .foregroundStyle(theme.secondaryText)
                            .cosmicCard(theme)
                            .padding(.horizontal)
                    } else {
                        categoryPieChart
                        monthlyLineChart
                    }
                }
                .padding(.vertical)
            }
            .background(theme.background)
            .navigationTitle(L("analytics", lang: language))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(L("button_cancel", lang: language)) { onBack() }
                }
            }
        }
    }

    private var categoryPieChart: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(L("category_expenses", lang: language))
                .font(.headline)
                .foregroundStyle(theme.primaryText)
            Chart(categoryData, id: \.category) { item in
                SectorMark(angle: .value("Amount", item.amount), innerRadius: .ratio(0.5))
                    .foregroundStyle(by: .value("Category", item.category))
            }
            .frame(height: 220)
        }
        .cosmicCard(theme)
        .padding(.horizontal)
    }

    private var monthlyLineChart: some View {
        let monthly = Dictionary(grouping: filteredTransactions) {
            Calendar.current.component(.month, from: $0.date)
        }.map { month, txs -> (month: Int, net: Double) in
            let net = txs.reduce(0.0) { $0 + ($1.isIncome ? $1.amount : -$1.amount) }
            return (month, net)
        }.sorted { $0.month < $1.month }

        return VStack(alignment: .leading, spacing: 8) {
            Text(L("analytics_subtitle", lang: language))
                .font(.headline)
                .foregroundStyle(theme.primaryText)
            Chart(monthly, id: \.month) { item in
                LineMark(x: .value("Month", item.month), y: .value("Net", item.net))
                    .foregroundStyle(theme.accent)
                AreaMark(x: .value("Month", item.month), y: .value("Net", item.net))
                    .foregroundStyle(theme.accent.opacity(0.2))
            }
            .frame(height: 180)
        }
        .cosmicCard(theme)
        .padding(.horizontal)
    }
}
