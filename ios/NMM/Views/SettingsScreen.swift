import SwiftUI
import MessageUI
import UIKit

struct SettingsScreen: View {
    let theme: NMMTheme
    let language: String
    let viewModel: FinanceViewModel
    let onReset: () -> Void

    @State private var showResetAlert = false
    @State private var showMailComposer = false
    @State private var exportURL: IdentifiableURL?

    private let languages = ["ID", "EN", "ES", "UK", "JA", "ZH"]
    private let currencies = ["Rp", "$", "€", "£", "¥"]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                header
                displaySection
                securitySection
                exportSection
                supportSection
                dangerSection
            }
            .padding()
        }
        .background(theme.background)
        .alert(L("clear_data", lang: language), isPresented: $showResetAlert) {
            Button(L("button_cancel", lang: language), role: .cancel) {}
            Button(L("clear_data", lang: language), role: .destructive) {
                viewModel.resetAllData()
                onReset()
            }
        } message: {
            Text(L("confirm_delete", lang: language))
        }
        .sheet(isPresented: $showMailComposer) {
            MailComposeView(
                subject: "Feedback & Masukan Aplikasi NMM (iOS)",
                body: "Halo Developer,\n\nBerikut masukan saya untuk aplikasi NMM:\n\n"
            )
        }
        .sheet(item: $exportURL) { item in
            ShareSheet(items: [item.url])
        }
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(L("settings", lang: language))
                .font(.title.bold())
                .foregroundStyle(theme.primaryText)
            Text(L("settings_subtitle", lang: language))
                .font(.caption)
                .foregroundStyle(theme.secondaryText)
        }
    }

    private var displaySection: some View {
        settingsGroup(title: L("dark_mode", lang: language)) {
            Toggle(L("dark_mode", lang: language), isOn: Bindable(viewModel.preferences).isDarkMode)
                .tint(theme.accent)

            Picker(L("language", lang: language), selection: Bindable(viewModel.preferences).selectedLanguage) {
                ForEach(languages, id: \.self) { Text($0).tag($0) }
            }

            Picker(L("currency", lang: language), selection: Bindable(viewModel.preferences).selectedCurrency) {
                ForEach(currencies, id: \.self) { Text($0).tag($0) }
            }
        }
    }

    private var securitySection: some View {
        settingsGroup(title: L("pin_lock", lang: language)) {
            Toggle(L("biometric", lang: language), isOn: Bindable(viewModel.preferences).isBiometricEnabled)
                .tint(theme.accent)

            HStack {
                Text(viewModel.preferences.userPin != nil ? L("pin_active_desc", lang: language) : L("pin_inactive", lang: language))
                    .foregroundStyle(theme.secondaryText)
                Spacer()
                if viewModel.preferences.userPin != nil {
                    Button(L("button_cancel", lang: language)) {
                        viewModel.preferences.userPin = nil
                    }
                    .font(.caption)
                    .foregroundStyle(NMColors.dangerRed)
                }
            }
        }
    }

    private var exportSection: some View {
        settingsGroup(title: L("export_csv", lang: language)) {
            Button {
                exportURL = viewModel.exportCSV().map { IdentifiableURL(url: $0) }
            } label: {
                Label(L("export_csv", lang: language), systemImage: "square.and.arrow.up")
                    .foregroundStyle(theme.accent)
            }
        }
    }

    private var supportSection: some View {
        settingsGroup(title: L("support_feedback", lang: language)) {
            Button {
                if MFMailComposeViewController.canSendMail() {
                    showMailComposer = true
                } else if let url = URL(string: "mailto:neo.developer.apps@gmail.com?subject=Feedback%20%26%20Masukan%20Aplikasi%20NMM%20(iOS)&body=Halo%20Developer,%0A%0ABerikut%20masukan%20saya%20untuk%20aplikasi%20NMM:%0A%0A") {
                    UIApplication.shared.open(url)
                }
            } label: {
                VStack(alignment: .leading, spacing: 4) {
                    Text(L("feedback_apps", lang: language))
                        .font(.subheadline.bold())
                        .foregroundStyle(theme.primaryText)
                    Text(L("feedback_desc", lang: language))
                        .font(.caption)
                        .foregroundStyle(theme.secondaryText)
                }
            }
        }
    }

    private var dangerSection: some View {
        Button {
            showResetAlert = true
        } label: {
            Label(L("clear_data", lang: language), systemImage: "trash.fill")
                .frame(maxWidth: .infinity)
                .padding()
                .background(NMColors.dangerRed.opacity(0.15))
                .foregroundStyle(NMColors.dangerRed)
                .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    private func settingsGroup<Content: View>(title: String, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(title)
                .font(.headline)
                .foregroundStyle(theme.primaryText)
            VStack(spacing: 12) { content() }
                .cosmicCard(theme)
        }
    }
}

struct IdentifiableURL: Identifiable {
    let url: URL
    var id: String { url.absoluteString }
}

struct MailComposeView: UIViewControllerRepresentable {
    let subject: String
    let body: String

    func makeUIViewController(context: Context) -> MFMailComposeViewController {
        let vc = MFMailComposeViewController()
        vc.setToRecipients(["neo.developer.apps@gmail.com"])
        vc.setSubject(subject)
        vc.setMessageBody(body, isHTML: false)
        vc.mailComposeDelegate = context.coordinator
        return vc
    }

    func updateUIViewController(_ uiViewController: MFMailComposeViewController, context: Context) {}

    func makeCoordinator() -> Coordinator { Coordinator() }

    class Coordinator: NSObject, MFMailComposeViewControllerDelegate {
        func mailComposeController(_ controller: MFMailComposeViewController, didFinishWith result: MFMailComposeResult, error: Error?) {
            controller.dismiss(animated: true)
        }
    }
}

struct ShareSheet: UIViewControllerRepresentable {
    let items: [Any]

    func makeUIViewController(context: Context) -> UIActivityViewController {
        UIActivityViewController(activityItems: items, applicationActivities: nil)
    }

    func updateUIViewController(_ uiViewController: UIActivityViewController, context: Context) {}
}
