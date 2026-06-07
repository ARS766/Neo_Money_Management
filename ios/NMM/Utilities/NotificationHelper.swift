import Foundation
import UserNotifications

enum NotificationHelper {
    static func requestAuthorization() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
    }

    static func notifyBudgetExceeded(category: String, language: String) {
        let content = UNMutableNotificationContent()
        content.title = L("budget_planner", lang: language)
        content.body = "\(category): \(L("budget_usage", lang: language)) > 100%"
        content.sound = .default

        let request = UNNotificationRequest(
            identifier: "budget-\(category)-\(UUID().uuidString)",
            content: content,
            trigger: nil
        )
        UNUserNotificationCenter.current().add(request)
    }
}
