import SwiftUI

struct HistoryScreen: View {
    let theme: NMMTheme
    let language: String
    let currency: String
    let transactions: [Transaction]
    @Binding var searchQuery: String
    @Binding var filterCategory: String?
    @Binding var filterType: String?
    @Binding var sortOption: SortOption
    let categories: [String]
    let onDelete: (Transaction) -> Void

    var body: some View {
        VStack(spacing: 0) {
            VStack(alignment: .leading, spacing: 8) {
                Text(L("history_title", lang: language))
                    .font(.title.bold())
                    .foregroundStyle(theme.primaryText)
                Text(L("history_subtitle", lang: language))
                    .font(.caption)
                    .foregroundStyle(theme.secondaryText)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding()

            searchAndFilters

            if transactions.isEmpty {
                Spacer()
                Text(L("no_transactions_registered", lang: language))
                    .foregroundStyle(theme.secondaryText)
                Spacer()
            } else {
                List {
                    ForEach(transactions, id: \.id) { tx in
                        TransactionRow(transaction: tx, currency: currency, theme: theme)
                            .listRowBackground(theme.card)
                            .listRowSeparatorTint(theme.secondaryText.opacity(0.2))
                    }
                    .onDelete { indexSet in
                        indexSet.forEach { onDelete(transactions[$0]) }
                    }
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
            }
        }
        .background(theme.background)
    }

    private var searchAndFilters: some View {
        VStack(spacing: 8) {
            HStack {
                Image(systemName: "magnifyingglass")
                    .foregroundStyle(theme.secondaryText)
                TextField(L("search_hint", lang: language), text: $searchQuery)
                    .foregroundStyle(theme.primaryText)
            }
            .padding(10)
            .background(theme.card)
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .padding(.horizontal)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack {
                    filterChip("All", isSelected: filterType == nil) { filterType = nil }
                    filterChip(L("add_income", lang: language), isSelected: filterType == "INCOME") { filterType = "INCOME" }
                    filterChip(L("add_expense", lang: language), isSelected: filterType == "EXPENSE") { filterType = "EXPENSE" }
                    ForEach(categories, id: \.self) { cat in
                        filterChip(cat, isSelected: filterCategory == cat) {
                            filterCategory = filterCategory == cat ? nil : cat
                        }
                    }
                }
                .padding(.horizontal)
            }

            Picker("Sort", selection: $sortOption) {
                Text("Newest").tag(SortOption.newest)
                Text("Oldest").tag(SortOption.oldest)
                Text("Highest").tag(SortOption.highestAmount)
                Text("Lowest").tag(SortOption.lowestAmount)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal)
        }
        .padding(.bottom, 8)
    }

    private func filterChip(_ label: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(label)
                .font(.caption)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(isSelected ? theme.accent : theme.card)
                .foregroundStyle(isSelected ? .white : theme.primaryText)
                .clipShape(Capsule())
        }
    }
}
