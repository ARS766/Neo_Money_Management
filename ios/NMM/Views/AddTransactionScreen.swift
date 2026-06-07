import SwiftUI
import PhotosUI

struct AddTransactionScreen: View {
    let theme: NMMTheme
    let language: String
    let currency: String
    let incomeCategories: [String]
    let expenseCategories: [String]
    let initialType: String
    let onSave: (String, Double, Date, String, String, String) -> Void
    let onBack: () -> Void

    @State private var type: String
    @State private var title = ""
    @State private var amountText = ""
    @State private var category = ""
    @State private var date = Date()
    @State private var note = ""
    @State private var selectedPhoto: PhotosPickerItem?

    init(
        theme: NMMTheme,
        language: String,
        currency: String,
        incomeCategories: [String],
        expenseCategories: [String],
        initialType: String,
        onSave: @escaping (String, Double, Date, String, String, String) -> Void,
        onBack: @escaping () -> Void
    ) {
        self.theme = theme
        self.language = language
        self.currency = currency
        self.incomeCategories = incomeCategories
        self.expenseCategories = expenseCategories
        self.initialType = initialType
        self.onSave = onSave
        self.onBack = onBack
        _type = State(initialValue: initialType)
        _category = State(initialValue: initialType == "INCOME" ? (incomeCategories.first ?? "") : (expenseCategories.first ?? ""))
    }

    private var categories: [String] {
        type == "INCOME" ? incomeCategories : expenseCategories
    }

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    Picker(L("transaction_type", lang: language), selection: $type) {
                        Text(L("add_income", lang: language)).tag("INCOME")
                        Text(L("add_expense", lang: language)).tag("EXPENSE")
                    }
                    .pickerStyle(.segmented)
                    .onChange(of: type) { _, newType in
                        category = newType == "INCOME" ? (incomeCategories.first ?? "") : (expenseCategories.first ?? "")
                    }
                }

                Section {
                    TextField(L("transaction_title", lang: language), text: $title)
                    TextField(L("transaction_amount", lang: language), text: $amountText)
                        .keyboardType(.decimalPad)
                    Picker(L("transaction_category", lang: language), selection: $category) {
                        ForEach(categories, id: \.self) { Text($0).tag($0) }
                    }
                    DatePicker(L("transaction_date", lang: language), selection: $date, displayedComponents: .date)
                    TextField(L("transaction_note", lang: language), text: $note)
                }

                Section(L("receipt_photo", lang: language)) {
                    PhotosPicker(selection: $selectedPhoto, matching: .images) {
                        Label(L("receipt_photo", lang: language), systemImage: "camera.fill")
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .background(theme.background)
            .navigationTitle(L("add_transaction", lang: language))
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(L("button_cancel", lang: language)) { onBack() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(L("button_save", lang: language)) { save() }
                        .fontWeight(.bold)
                }
            }
        }
    }

    private func save() {
        let amount = Double(amountText.replacingOccurrences(of: ".", with: "").replacingOccurrences(of: ",", with: "")) ?? 0
        onSave(title, amount, date, category, note, type)
        onBack()
    }
}
