import SwiftUI

struct BudgetPlannerScreen: View {
    let theme: NMMTheme
    let language: String
    let currency: String
    let budgets: [Budget]
    let transactions: [Transaction]
    let expenseCategories: [String]
    let onSaveBudget: (String, Double) -> Void
    let onDeleteBudget: (Budget) -> Void

    @State private var showAddDialog = false
    @State private var selectedCategory = ""
    @State private var limitText = ""

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(L("budget_planner", lang: language))
                    .font(.title.bold())
                    .foregroundStyle(theme.primaryText)
                Text(L("budget_subtitle", lang: language))
                    .font(.caption)
                    .foregroundStyle(theme.secondaryText)

                if budgets.isEmpty {
                    Text(L("budget_unset", lang: language))
                        .foregroundStyle(theme.secondaryText)
                        .cosmicCard(theme)
                } else {
                    ForEach(budgets, id: \.id) { budget in
                        budgetCard(budget)
                    }
                }

                Button {
                    selectedCategory = expenseCategories.first ?? ""
                    limitText = ""
                    showAddDialog = true
                } label: {
                    Label(L("save_budget_dialog_title", lang: language), systemImage: "plus.circle.fill")
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(theme.accent)
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                }
            }
            .padding()
        }
        .background(theme.background)
        .sheet(isPresented: $showAddDialog) { addBudgetSheet }
    }

    private func budgetCard(_ budget: Budget) -> some View {
        let spent = budget.spentAmount(from: transactions)
        let ratio = budget.limitAmount > 0 ? spent / budget.limitAmount : 0
        let progressColor: Color = ratio >= 1 ? NMColors.dangerRed : ratio >= 0.8 ? NMColors.warningGold : NMColors.successGreen

        return VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(budget.category)
                    .font(.headline)
                    .foregroundStyle(theme.primaryText)
                Spacer()
                Button(role: .destructive) { onDeleteBudget(budget) } label: {
                    Image(systemName: "trash")
                }
            }
            ProgressView(value: min(ratio, 1.0))
                .tint(progressColor)
            HStack {
                Text("\(CurrencyFormatter.format(spent, symbol: currency)) / \(CurrencyFormatter.format(budget.limitAmount, symbol: currency))")
                    .font(.system(.caption, design: .monospaced))
                    .foregroundStyle(theme.secondaryText)
                Spacer()
                Text("\(Int(ratio * 100))%")
                    .font(.caption.bold())
                    .foregroundStyle(progressColor)
            }
        }
        .cosmicCard(theme)
    }

    private var addBudgetSheet: some View {
        NavigationStack {
            Form {
                Picker(L("transaction_category", lang: language), selection: $selectedCategory) {
                    ForEach(expenseCategories, id: \.self) { Text($0).tag($0) }
                }
                TextField(L("transaction_amount", lang: language), text: $limitText)
                    .keyboardType(.decimalPad)
            }
            .navigationTitle(L("save_budget_dialog_title", lang: language))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(L("button_cancel", lang: language)) { showAddDialog = false }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L("button_save", lang: language)) {
                        let limit = Double(limitText) ?? 0
                        onSaveBudget(selectedCategory, limit)
                        showAddDialog = false
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
