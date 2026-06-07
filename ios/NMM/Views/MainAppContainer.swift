import SwiftUI
import UIKit

enum AppStage {
    case splash, onboarding, auth, main
}

enum MainRoute: String {
    case dashboard, history, budget, savings, settings, addTransaction, analytics
}

struct MainAppContainer: View {
    @Environment(\.modelContext) private var modelContext
    @Environment(\.scenePhase) private var scenePhase
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    @State private var viewModel: FinanceViewModel?
    @State private var stage: AppStage = .splash
    @State private var route: MainRoute = .dashboard
    @State private var lastRoute: MainRoute = .dashboard
    @State private var transactionType = "EXPENSE"
    @State private var showPrivacyOverlay = false
    @State private var showAddMenu = false

    private var theme: NMMTheme {
        NMMTheme(isDark: viewModel?.preferences.isDarkMode ?? true)
    }

    private var language: String {
        viewModel?.preferences.selectedLanguage ?? "ID"
    }

    var body: some View {
        Group {
            if let viewModel {
                mainContent(viewModel: viewModel)
            } else {
                ProgressView()
                    .onAppear {
                        viewModel = FinanceViewModel(modelContext: modelContext)
                        NotificationHelper.requestAuthorization()
                    }
            }
        }
        .preferredColorScheme(theme.isDark ? .dark : .light)
        .onChange(of: scenePhase) { _, newPhase in
            showPrivacyOverlay = newPhase == .inactive || newPhase == .background
        }
    }

    @ViewBuilder
    private func mainContent(viewModel: FinanceViewModel) -> some View {
        ZStack {
            switch stage {
            case .splash:
                SplashScreen(theme: theme) {
                    stage = resolveStageAfterSplash(viewModel)
                }

            case .onboarding:
                OnboardingScreen(theme: theme, language: language) { lang, currency in
                    viewModel.preferences.selectedLanguage = lang
                    viewModel.preferences.selectedCurrency = currency
                    viewModel.preferences.hasCompletedOnboarding = true
                    stage = viewModel.preferences.userPin != nil ? .auth : .main
                }

            case .auth:
                LoginScreen(
                    theme: theme,
                    language: language,
                    savedPin: viewModel.preferences.userPin,
                    isBiometricEnabled: viewModel.preferences.isBiometricEnabled,
                    onSuccess: { stage = .main },
                    onSetupPin: { viewModel.preferences.userPin = $0 }
                )

            case .main:
                mainScaffold(viewModel: viewModel)
            }

            if viewModel.overlayState.isVisible {
                overlayFeedback(viewModel.overlayState)
            }

            if showPrivacyOverlay {
                PrivacyOverlay(language: language, theme: theme)
                    .transition(.opacity)
                    .zIndex(100)
            }
        }
    }

    private func mainScaffold(viewModel: FinanceViewModel) -> some View {
        VStack(spacing: 0) {
            contentArea(viewModel: viewModel)
                .multitaskingPrivacyBlur(shouldBlurCompactOniPad)

            if route != .addTransaction && route != .analytics {
                bottomBar(viewModel: viewModel)
            }
        }
        .background(theme.background)
    }

    @ViewBuilder
    private func contentArea(viewModel: FinanceViewModel) -> some View {
        switch route {
        case .dashboard:
            DashboardScreen(
                theme: theme,
                language: language,
                currency: viewModel.preferences.selectedCurrency,
                transactions: viewModel.transactions,
                totalBalance: viewModel.totalBalance,
                monthlyIncome: viewModel.monthlyIncome,
                monthlyExpense: viewModel.monthlyExpense,
                onNavigateToHistory: { route = .history },
                onNavigateToAdd: { type in
                    transactionType = type
                    lastRoute = route
                    route = .addTransaction
                },
                onNavigateToAnalytics: { route = .analytics }
            )

        case .history:
            HistoryScreen(
                theme: theme,
                language: language,
                currency: viewModel.preferences.selectedCurrency,
                transactions: viewModel.filteredTransactions,
                searchQuery: Bindable(viewModel).searchQuery,
                filterCategory: Bindable(viewModel).filterCategory,
                filterType: Bindable(viewModel).filterType,
                sortOption: Bindable(viewModel).sortOption,
                categories: viewModel.preferences.incomeCategories + viewModel.preferences.expenseCategories,
                onDelete: { viewModel.deleteTransaction($0) }
            )

        case .budget:
            BudgetPlannerScreen(
                theme: theme,
                language: language,
                currency: viewModel.preferences.selectedCurrency,
                budgets: viewModel.budgets,
                transactions: viewModel.transactions,
                expenseCategories: viewModel.preferences.expenseCategories,
                onSaveBudget: { viewModel.saveBudget(category: $0, limit: $1) },
                onDeleteBudget: { viewModel.deleteBudget($0) }
            )

        case .savings:
            SavingsGoalScreen(
                theme: theme,
                language: language,
                currency: viewModel.preferences.selectedCurrency,
                goals: viewModel.savingsGoals,
                onSaveGoal: { viewModel.saveSavingsGoal(title: $0, target: $1, current: $2, deadline: $3) },
                onUpdateContribution: { viewModel.updateSavingsContribution($0, amount: $1) },
                onDeleteGoal: { viewModel.deleteSavingsGoal($0) }
            )

        case .settings:
            SettingsScreen(
                theme: theme,
                language: language,
                viewModel: viewModel,
                onReset: { stage = .splash }
            )

        case .addTransaction:
            AddTransactionScreen(
                theme: theme,
                language: language,
                currency: viewModel.preferences.selectedCurrency,
                incomeCategories: viewModel.preferences.incomeCategories,
                expenseCategories: viewModel.preferences.expenseCategories,
                initialType: transactionType,
                onSave: { viewModel.insertTransaction(title: $0, amount: $1, date: $2, category: $3, note: $4, type: $5) },
                onBack: { route = lastRoute }
            )

        case .analytics:
            AnalyticsScreen(
                theme: theme,
                language: language,
                currency: viewModel.preferences.selectedCurrency,
                transactions: viewModel.transactions,
                onBack: { route = lastRoute }
            )
        }
    }

    private func bottomBar(viewModel: FinanceViewModel) -> some View {
        ZStack(alignment: .top) {
            HStack {
                tabButton(.dashboard, icon: "square.grid.2x2", label: L("nav_home", lang: language))
                tabButton(.history, icon: "clock.arrow.circlepath", label: L("nav_history", lang: language))
                tabButton(.budget, icon: "chart.pie", label: L("nav_budget", lang: language))
                Spacer().frame(width: 56)
                tabButton(.savings, icon: "banknote", label: L("nav_savings", lang: language))
                tabButton(.settings, icon: "gearshape", label: L("nav_settings", lang: language))
            }
            .padding(.horizontal, 8)
            .padding(.top, 8)
            .padding(.bottom, 4)
            .background(theme.card)

            Button {
                withAnimation { showAddMenu.toggle() }
            } label: {
                Image(systemName: showAddMenu ? "xmark" : "plus")
                    .font(.title3.bold())
                    .foregroundStyle(.white)
                    .frame(width: 46, height: 46)
                    .background(showAddMenu ? theme.card : theme.accent)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
                    .shadow(radius: 4)
            }
            .offset(y: -16)

            if showAddMenu {
                HStack(spacing: 16) {
                    quickAddButton(L("add_expense", lang: language), color: NMColors.dangerRed, type: "EXPENSE")
                    quickAddButton(L("add_income", lang: language), color: NMColors.successGreen, type: "INCOME")
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(theme.card)
                .clipShape(Capsule())
                .shadow(radius: 8)
                .offset(y: -72)
            }
        }
    }

    private func tabButton(_ target: MainRoute, icon: String, label: String) -> some View {
        Button {
            showAddMenu = false
            route = target
        } label: {
            VStack(spacing: 4) {
                Image(systemName: icon)
                    .font(.system(size: 18))
                Text(label)
                    .font(.system(size: 10))
            }
            .foregroundStyle(route == target ? theme.accent : theme.secondaryText)
            .frame(maxWidth: .infinity)
        }
    }

    private func quickAddButton(_ label: String, color: Color, type: String) -> some View {
        Button {
            showAddMenu = false
            transactionType = type
            lastRoute = route
            route = .addTransaction
        } label: {
            Text(label)
                .font(.caption.bold())
                .foregroundStyle(color)
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(color.opacity(0.1))
                .clipShape(Capsule())
        }
    }

    private func overlayFeedback(_ state: OverlayState) -> some View {
        VStack(spacing: 16) {
            Image(systemName: state.isSuccess ? "checkmark.circle.fill" : "xmark.circle.fill")
                .font(.system(size: 56))
                .foregroundStyle(state.isSuccess ? NMColors.successGreen : NMColors.dangerRed)
            Text(state.isSuccess ? L("success", lang: language) : L("failed", lang: language))
                .font(.headline)
                .foregroundStyle(theme.primaryText)
            Text(state.message)
                .font(.caption)
                .foregroundStyle(theme.secondaryText)
        }
        .padding(32)
        .background(theme.card)
        .clipShape(RoundedRectangle(cornerRadius: 24))
        .shadow(radius: 20)
    }

    private func resolveStageAfterSplash(_ viewModel: FinanceViewModel) -> AppStage {
        if !viewModel.preferences.hasCompletedOnboarding { return .onboarding }
        if viewModel.preferences.userPin != nil { return .auth }
        return .main
    }

    private var shouldBlurCompactOniPad: Bool {
        horizontalSizeClass == .compact && UIDevice.current.userInterfaceIdiom == .pad
    }
}
