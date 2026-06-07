import SwiftUI

struct SavingsGoalScreen: View {
    let theme: NMMTheme
    let language: String
    let currency: String
    let goals: [SavingsGoal]
    let onSaveGoal: (String, Double, Double, Date) -> Void
    let onUpdateContribution: (SavingsGoal, Double) -> Void
    let onDeleteGoal: (SavingsGoal) -> Void

    @State private var showAddDialog = false
    @State private var title = ""
    @State private var targetText = ""
    @State private var currentText = ""
    @State private var deadline = Date()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                Text(L("savings_goal", lang: language))
                    .font(.title.bold())
                    .foregroundStyle(theme.primaryText)
                Text(L("savings_subtitle", lang: language))
                    .font(.caption)
                    .foregroundStyle(theme.secondaryText)

                ForEach(goals, id: \.id) { goal in
                    goalCard(goal)
                }

                Button {
                    title = ""
                    targetText = ""
                    currentText = "0"
                    deadline = Date().addingTimeInterval(86400 * 365)
                    showAddDialog = true
                } label: {
                    Label(L("add_savings_dialog_title", lang: language), systemImage: "plus.circle.fill")
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
        .sheet(isPresented: $showAddDialog) { addGoalSheet }
    }

    private func goalCard(_ goal: SavingsGoal) -> some View {
        VStack(spacing: 12) {
            HStack {
                Text(goal.title)
                    .font(.headline)
                    .foregroundStyle(theme.primaryText)
                Spacer()
                Button(role: .destructive) { onDeleteGoal(goal) } label: {
                    Image(systemName: "trash")
                }
            }

            ZStack {
                Circle()
                    .stroke(theme.secondaryText.opacity(0.2), lineWidth: 12)
                Circle()
                    .trim(from: 0, to: goal.progress)
                    .stroke(goal.isComplete ? NMColors.successGreen : theme.accent, style: StrokeStyle(lineWidth: 12, lineCap: .round))
                    .rotationEffect(.degrees(-90))
                VStack {
                    Text("\(Int(goal.progress * 100))%")
                        .font(.title2.bold())
                        .foregroundStyle(theme.primaryText)
                    if goal.isComplete {
                        Text(L("saving_reached", lang: language))
                            .font(.caption2)
                            .foregroundStyle(NMColors.successGreen)
                    }
                }
            }
            .frame(width: 120, height: 120)

            Text("\(CurrencyFormatter.format(goal.currentAmount, symbol: currency)) / \(CurrencyFormatter.format(goal.targetAmount, symbol: currency))")
                .font(.system(.subheadline, design: .monospaced))
                .foregroundStyle(theme.secondaryText)

            HStack {
                Button {
                    onUpdateContribution(goal, 10000)
                } label: {
                    Label(L("saving_add", lang: language), systemImage: "plus")
                        .font(.caption)
                }
                .buttonStyle(.borderedProminent)
                .tint(NMColors.successGreen)

                Button {
                    onUpdateContribution(goal, -10000)
                } label: {
                    Image(systemName: "minus")
                }
                .buttonStyle(.bordered)
            }
        }
        .cosmicCard(theme)
    }

    private var addGoalSheet: some View {
        NavigationStack {
            Form {
                TextField(L("saving_target", lang: language), text: $title)
                TextField(L("transaction_amount", lang: language), text: $targetText)
                    .keyboardType(.decimalPad)
                TextField(L("saving_initial", lang: language), text: $currentText)
                    .keyboardType(.decimalPad)
                DatePicker(L("saving_date", lang: language), selection: $deadline, displayedComponents: .date)
            }
            .navigationTitle(L("add_savings_dialog_title", lang: language))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(L("button_cancel", lang: language)) { showAddDialog = false }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L("button_save", lang: language)) {
                        onSaveGoal(title, Double(targetText) ?? 0, Double(currentText) ?? 0, deadline)
                        showAddDialog = false
                    }
                }
            }
        }
        .presentationDetents([.medium])
    }
}
