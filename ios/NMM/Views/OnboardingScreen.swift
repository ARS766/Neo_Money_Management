import SwiftUI

struct OnboardingScreen: View {
    let theme: NMMTheme
    let language: String
    let onFinished: (String, String) -> Void

    @State private var page = 0
    @State private var selectedLanguage = "ID"
    @State private var selectedCurrency = "Rp"

    private let currencies = ["Rp", "$", "€", "£", "¥"]
    private let languages = ["ID", "EN", "ES", "UK", "JA", "ZH"]

    var body: some View {
        VStack(spacing: 24) {
            TabView(selection: $page) {
                onboardingPage(
                    icon: "chart.line.uptrend.xyaxis",
                    title: L("onboarding_welcome", lang: selectedLanguage),
                    subtitle: L("onboarding_desc", lang: selectedLanguage)
                ).tag(0)

                VStack(spacing: 20) {
                    Text(L("language", lang: selectedLanguage))
                        .font(.headline)
                        .foregroundStyle(theme.primaryText)
                    Picker(L("language", lang: selectedLanguage), selection: $selectedLanguage) {
                        ForEach(languages, id: \.self) { Text($0).tag($0) }
                    }
                    .pickerStyle(.wheel)

                    Text(L("currency", lang: selectedLanguage))
                        .font(.headline)
                        .foregroundStyle(theme.primaryText)
                    Picker(L("currency", lang: selectedLanguage), selection: $selectedCurrency) {
                        ForEach(currencies, id: \.self) { Text($0).tag($0) }
                    }
                    .pickerStyle(.segmented)
                }
                .padding()
                .tag(1)
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .frame(maxHeight: 400)

            Button {
                if page < 1 {
                    withAnimation { page += 1 }
                } else {
                    onFinished(selectedLanguage, selectedCurrency)
                }
            } label: {
                Text(page < 1 ? "Next" : L("onboarding_get_started", lang: selectedLanguage))
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(theme.accent)
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal)
        }
        .padding()
        .background(theme.background)
    }

    private func onboardingPage(icon: String, title: String, subtitle: String) -> some View {
        VStack(spacing: 16) {
            Image(systemName: icon)
                .font(.system(size: 64))
                .foregroundStyle(theme.accent)
            Text(title)
                .font(.title2.bold())
                .foregroundStyle(theme.primaryText)
            Text(subtitle)
                .font(.body)
                .foregroundStyle(theme.secondaryText)
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}
