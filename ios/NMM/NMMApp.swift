import SwiftUI
import SwiftData

@main
struct NMMApp: App {
    var sharedModelContainer: ModelContainer = {
        let schema = Schema([Transaction.self, Budget.self, SavingsGoal.self])
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)
        do {
            return try ModelContainer(for: schema, configurations: [config])
        } catch {
            fatalError("Could not create ModelContainer: \(error)")
        }
    }()

    var body: some Scene {
        WindowGroup {
            MainAppContainer()
        }
        .modelContainer(sharedModelContainer)
    }
}
