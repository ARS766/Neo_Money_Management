import Foundation
import SwiftUI

@MainActor
@Observable
final class UserPreferences {
    private enum Keys {
        static let userPin = "user_pin"
        static let isBiometricEnabled = "is_biometric_enabled"
        static let isDarkMode = "is_dark_mode"
        static let selectedCurrency = "selected_currency"
        static let selectedLanguage = "selected_language"
        static let hasCompletedOnboarding = "has_completed_onboarding"
        static let incomeCategories = "income_categories"
        static let expenseCategories = "expense_categories"
        static let isAutoBackupEnabled = "is_auto_backup_enabled"
    }

    var userPin: String? {
        didSet { UserDefaults.standard.set(userPin, forKey: Keys.userPin) }
    }

    var isBiometricEnabled: Bool {
        didSet { UserDefaults.standard.set(isBiometricEnabled, forKey: Keys.isBiometricEnabled) }
    }

    var isDarkMode: Bool {
        didSet { UserDefaults.standard.set(isDarkMode, forKey: Keys.isDarkMode) }
    }

    var selectedCurrency: String {
        didSet { UserDefaults.standard.set(selectedCurrency, forKey: Keys.selectedCurrency) }
    }

    var selectedLanguage: String {
        didSet { UserDefaults.standard.set(selectedLanguage, forKey: Keys.selectedLanguage) }
    }

    var hasCompletedOnboarding: Bool {
        didSet { UserDefaults.standard.set(hasCompletedOnboarding, forKey: Keys.hasCompletedOnboarding) }
    }

    var incomeCategories: [String] {
        didSet { UserDefaults.standard.set(incomeCategories.joined(separator: ","), forKey: Keys.incomeCategories) }
    }

    var expenseCategories: [String] {
        didSet { UserDefaults.standard.set(expenseCategories.joined(separator: ","), forKey: Keys.expenseCategories) }
    }

    var isAutoBackupEnabled: Bool {
        didSet { UserDefaults.standard.set(isAutoBackupEnabled, forKey: Keys.isAutoBackupEnabled) }
    }

    init() {
        let defaults = UserDefaults.standard
        userPin = defaults.string(forKey: Keys.userPin)
        isBiometricEnabled = defaults.bool(forKey: Keys.isBiometricEnabled)
        isDarkMode = defaults.object(forKey: Keys.isDarkMode) as? Bool ?? true
        selectedCurrency = defaults.string(forKey: Keys.selectedCurrency) ?? "Rp"
        selectedLanguage = defaults.string(forKey: Keys.selectedLanguage) ?? "ID"
        hasCompletedOnboarding = defaults.bool(forKey: Keys.hasCompletedOnboarding)
        isAutoBackupEnabled = defaults.bool(forKey: Keys.isAutoBackupEnabled)

        let incomeStr = defaults.string(forKey: Keys.incomeCategories)
            ?? "Gaji,Freelance,Bonus,Investasi,Lainnya"
        incomeCategories = incomeStr.split(separator: ",").map(String.init).filter { !$0.isEmpty }

        let expenseStr = defaults.string(forKey: Keys.expenseCategories)
            ?? "Makanan,Transportasi,Belanja,Hiburan,Pendidikan,Kesehatan,Tagihan,Lainnya"
        expenseCategories = expenseStr.split(separator: ",").map(String.init).filter { !$0.isEmpty }
    }

    func clearAll() {
        let domain = Bundle.main.bundleIdentifier ?? ""
        UserDefaults.standard.removePersistentDomain(forName: domain)
        userPin = nil
        isBiometricEnabled = false
        isDarkMode = true
        selectedCurrency = "Rp"
        selectedLanguage = "ID"
        hasCompletedOnboarding = false
        isAutoBackupEnabled = false
        incomeCategories = ["Gaji", "Freelance", "Bonus", "Investasi", "Lainnya"]
        expenseCategories = ["Makanan", "Transportasi", "Belanja", "Hiburan", "Pendidikan", "Kesehatan", "Tagihan", "Lainnya"]
    }
}
