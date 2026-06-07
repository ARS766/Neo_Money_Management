import SwiftUI
import LocalAuthentication

struct LoginScreen: View {
    let theme: NMMTheme
    let language: String
    let savedPin: String?
    let isBiometricEnabled: Bool
    let onSuccess: () -> Void
    let onSetupPin: (String) -> Void

    @State private var enteredPin = ""
    @State private var confirmPin = ""
    @State private var isSetupMode: Bool
    @State private var errorMessage: String?

    init(
        theme: NMMTheme,
        language: String,
        savedPin: String?,
        isBiometricEnabled: Bool,
        onSuccess: @escaping () -> Void,
        onSetupPin: @escaping (String) -> Void
    ) {
        self.theme = theme
        self.language = language
        self.savedPin = savedPin
        self.isBiometricEnabled = isBiometricEnabled
        self.onSuccess = onSuccess
        self.onSetupPin = onSetupPin
        _isSetupMode = State(initialValue: savedPin == nil)
    }

    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "lock.fill")
                .font(.system(size: 48))
                .foregroundStyle(theme.accent)

            Text(isSetupMode ? L("login_setup_pin", lang: language) : L("login_enter_pin", lang: language))
                .font(.title2.bold())
                .foregroundStyle(theme.primaryText)

            HStack(spacing: 12) {
                ForEach(0..<4, id: \.self) { i in
                    Circle()
                        .fill(i < enteredPin.count ? theme.accent : theme.secondaryText.opacity(0.3))
                        .frame(width: 16, height: 16)
                }
            }

            if let errorMessage {
                Text(errorMessage)
                    .font(.caption)
                    .foregroundStyle(NMColors.dangerRed)
            }

            if !isSetupMode && isBiometricEnabled {
                Button { authenticateBiometric() } label: {
                    Label(L("login_use_biometric", lang: language), systemImage: "faceid")
                        .foregroundStyle(theme.accent)
                }
            }

            pinPad
        }
        .padding()
        .background(theme.background)
        .onAppear {
            if !isSetupMode && isBiometricEnabled { authenticateBiometric() }
        }
    }

    private var pinPad: some View {
        let keys = ["1","2","3","4","5","6","7","8","9","","0","⌫"]
        return LazyVGrid(columns: Array(repeating: GridItem(.flexible()), count: 3), spacing: 16) {
            ForEach(keys, id: \.self) { key in
                if key.isEmpty {
                    Color.clear.frame(height: 56)
                } else {
                    Button { handleKey(key) } label: {
                        Text(key)
                            .font(.title2)
                            .frame(maxWidth: .infinity, minHeight: 56)
                            .background(theme.card)
                            .clipShape(Circle())
                            .foregroundStyle(theme.primaryText)
                    }
                }
            }
        }
        .padding(.horizontal, 32)
    }

    private func handleKey(_ key: String) {
        errorMessage = nil
        if key == "⌫" {
            if !enteredPin.isEmpty { enteredPin.removeLast() }
            return
        }
        guard enteredPin.count < 4 else { return }
        enteredPin += key
        if enteredPin.count == 4 {
            if isSetupMode {
                if confirmPin.isEmpty {
                    confirmPin = enteredPin
                    enteredPin = ""
                } else if confirmPin == enteredPin {
                    onSetupPin(enteredPin)
                    onSuccess()
                } else {
                    errorMessage = "PIN tidak cocok"
                    enteredPin = ""
                    confirmPin = ""
                }
            } else if enteredPin == savedPin {
                onSuccess()
            } else {
                errorMessage = "PIN salah"
                enteredPin = ""
            }
        }
    }

    private func authenticateBiometric() {
        let context = LAContext()
        var error: NSError?
        guard context.canEvaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, error: &error) else { return }
        context.evaluatePolicy(.deviceOwnerAuthenticationWithBiometrics, localizedReason: "Unlock NMM") { success, _ in
            DispatchQueue.main.async { if success { onSuccess() } }
        }
    }
}
