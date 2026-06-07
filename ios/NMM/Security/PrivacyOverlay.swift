import SwiftUI

struct PrivacyOverlay: View {
    let language: String
    let theme: NMMTheme

    var body: some View {
        ZStack {
            theme.background.ignoresSafeArea()
            VStack(spacing: 16) {
                Image(systemName: "lock.shield.fill")
                    .font(.system(size: 64))
                    .foregroundStyle(theme.accent)
                Text(L("privacy_locked", lang: language))
                    .font(.title2.bold())
                    .foregroundStyle(theme.primaryText)
                Text(L("privacy_desc", lang: language))
                    .font(.caption)
                    .foregroundStyle(theme.secondaryText)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }
        }
    }
}

struct MultitaskingBlurModifier: ViewModifier {
    let shouldBlur: Bool

    func body(content: Content) -> some View {
        content.blur(radius: shouldBlur ? 8 : 0)
    }
}

extension View {
    func multitaskingPrivacyBlur(_ shouldBlur: Bool) -> some View {
        modifier(MultitaskingBlurModifier(shouldBlur: shouldBlur))
    }
}
