import SwiftUI

struct SplashScreen: View {
    let theme: NMMTheme
    let onFinished: () -> Void

    @State private var opacity: Double = 0

    var body: some View {
        ZStack {
            theme.background.ignoresSafeArea()
            VStack(spacing: 12) {
                Image(systemName: "dollarsign.circle.fill")
                    .font(.system(size: 80))
                    .foregroundStyle(theme.accent)
                    .opacity(opacity)
                Text("NMM")
                    .font(.system(size: 36, weight: .heavy, design: .rounded))
                    .foregroundStyle(theme.primaryText)
                    .opacity(opacity)
                Text("Neo Money Management")
                    .font(.caption)
                    .foregroundStyle(theme.secondaryText)
                    .opacity(opacity)
            }
        }
        .onAppear {
            withAnimation(.easeIn(duration: 1.0)) { opacity = 1 }
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { onFinished() }
        }
    }
}
