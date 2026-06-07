# NMM – Neo Money Management (iOS)

Native iOS application built with **SwiftUI + SwiftData + MVVM**, ported from the Android codebase per `nmm_ios_build_specification.txt`.

## Requirements

- macOS with **Xcode 15+**
- iOS **17.0+** deployment target
- Apple Developer account (for device testing and `.ipa` export)

## Project Structure

```
ios/
├── NMM.xcodeproj/          # Xcode project (builds .app / .ipa)
├── NMM/
│   ├── NMMApp.swift        # App entry + SwiftData container
│   ├── Models/             # Transaction, Budget, SavingsGoal
│   ├── Data/               # Repository + UserPreferences
│   ├── ViewModels/         # FinanceViewModel (@Observable)
│   ├── Views/              # All screens (Splash → Settings)
│   ├── Theme/              # Cosmic Dark Slate colors
│   ├── Security/           # Privacy overlay + SecureContentView
│   └── Utilities/          # Translation, Notifications
├── scripts/
│   └── build_ipa.sh        # CLI build script
└── ExportOptions.plist     # IPA export configuration
```

## Open in Xcode

1. Copy/clone the project to a Mac
2. Open `ios/NMM.xcodeproj` in Xcode
3. Select your **Development Team** under Signing & Capabilities
4. Choose a simulator or connected iPhone
5. Press **⌘R** to run

## Build from Command Line

```bash
cd ios

# Simulator .app (Debug)
chmod +x scripts/build_ipa.sh
./scripts/build_ipa.sh --simulator

# Device archive + .ipa (Release)
# 1. Edit ExportOptions.plist → set your teamID
# 2. Run:
./scripts/build_ipa.sh --release
```

Output locations:
- **.app** → `ios/build/DerivedData/.../Build/Products/Debug-iphonesimulator/NMM.app`
- **.ipa** → `ios/build/ipa/NMM.ipa`

## Architecture (MVVM)

| Layer | iOS Implementation |
|-------|-------------------|
| Model | SwiftData `@Model` entities |
| View | SwiftUI modular views |
| ViewModel | `@MainActor @Observable FinanceViewModel` |
| Preferences | `UserDefaults` via `UserPreferences` |
| Security | `scenePhase` privacy shield, `SecureContentView`, iPad blur |

## Feature Parity with Android

- ✅ Splash → Onboarding → PIN/Biometric → Main navigation
- ✅ Dashboard, History, Budget, Savings, Analytics, Settings
- ✅ Add Transaction with PhotosUI receipt picker
- ✅ Swift Charts analytics (pie + line)
- ✅ Budget notifications via `UNUserNotificationCenter`
- ✅ CSV export + email feedback (`MFMailComposeViewController`)
- ✅ 6-language support (ID, EN, ES, UK, JA, ZH)
- ✅ App Switcher privacy overlay
- ✅ Screenshot/recording protection (`isSecureTextEntry` wrapper)

## Bundle ID

`com.neo.nmm` (matches Android `applicationId`)

---

## Build dari Windows (GitHub Actions)

Tidak punya Mac? Gunakan CI cloud — edit di Windows, build di server macOS GitHub.

### 1. Simulator `.app` (gratis, tanpa Apple Developer)

```powershell
git add .
git commit -m "update ios"
git push origin main
```

1. Buka repo di GitHub → tab **Actions**
2. Pilih workflow **iOS Build (Simulator)**
3. Setelah hijau, download artifact **NMM-Simulator-app**

> `.app` simulator tidak bisa dipasang di iPhone fisik.

### 2. Export `.ipa` (butuh Apple Developer Program)

Isi **GitHub Secrets** (`Settings → Secrets and variables → Actions`):

| Secret | Keterangan |
|--------|------------|
| `APPLE_TEAM_ID` | Team ID 10 karakter |
| `BUILD_CERTIFICATE_BASE64` | Base64 file `.p12` |
| `P12_PASSWORD` | Password export `.p12` |
| `BUILD_PROVISION_PROFILE_BASE64` | Base64 `.mobileprovision` |
| `KEYCHAIN_PASSWORD` | String acak untuk keychain CI |

Encode di PowerShell (lihat `ios/scripts/encode-secrets.ps1`):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\cert.p12"))
```

Jalankan manual: **Actions → iOS Export IPA → Run workflow** → download **NMM-ipa-development**.
