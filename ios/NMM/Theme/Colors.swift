import SwiftUI

enum NMColors {
    static let primaryBlue = Color(red: 0.145, green: 0.388, blue: 0.922)
    static let secondaryBlue = Color(red: 0.220, green: 0.741, blue: 0.973)
    static let successGreen = Color(red: 0.133, green: 0.773, blue: 0.369)
    static let warningGold = Color(red: 0.961, green: 0.620, blue: 0.043)
    static let dangerRed = Color(red: 0.937, green: 0.267, blue: 0.267)

    static let backgroundDark = Color(red: 0.059, green: 0.090, blue: 0.165)
    static let cardDark = Color(red: 0.118, green: 0.161, blue: 0.231)
    static let textLight = Color(red: 0.973, green: 0.980, blue: 0.988)
    static let greyText = Color(red: 0.580, green: 0.639, blue: 0.722)

    static let backgroundLight = Color(red: 0.973, green: 0.980, blue: 0.988)
    static let cardLight = Color.white
    static let textDark = Color(red: 0.059, green: 0.090, blue: 0.165)
    static let greyTextLight = Color(red: 0.392, green: 0.455, blue: 0.545)
}

struct NMMTheme {
    let isDark: Bool

    var background: Color { isDark ? NMColors.backgroundDark : NMColors.backgroundLight }
    var card: Color { isDark ? NMColors.cardDark : NMColors.cardLight }
    var primaryText: Color { isDark ? NMColors.textLight : NMColors.textDark }
    var secondaryText: Color { isDark ? NMColors.greyText : NMColors.greyTextLight }
    var accent: Color { NMColors.primaryBlue }
}

struct CosmicCardStyle: ViewModifier {
    let theme: NMMTheme

    func body(content: Content) -> some View {
        content
            .padding()
            .background(theme.card)
            .clipShape(RoundedRectangle(cornerRadius: 16))
    }
}

extension View {
    func cosmicCard(_ theme: NMMTheme) -> some View {
        modifier(CosmicCardStyle(theme: theme))
    }
}

struct CurrencyFormatter {
    static func format(_ amount: Double, symbol: String) -> String {
        let formatter = NumberFormatter()
        formatter.numberStyle = .decimal
        formatter.maximumFractionDigits = 0
        formatter.groupingSeparator = "."
        let formatted = formatter.string(from: NSNumber(value: amount)) ?? "\(Int(amount))"
        return "\(symbol) \(formatted)"
    }
}
