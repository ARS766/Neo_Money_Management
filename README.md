<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# NMM – Neo Money Management

Cross-platform personal finance app with cosmic dark theme, PIN/biometric security, and offline-first data storage.

## Project Structure (Multi-Platform)

```
nmm-–-neo-money-management/
├── app/                          # Android (Kotlin + Jetpack Compose)
├── ios/                          # iOS (Swift + SwiftUI + SwiftData)
│   ├── NMM.xcodeproj             # Xcode project → .app / .ipa
│   └── NMM/                      # Swift source (MVVM)
├── nmm_ios_build_specification.txt
└── README.md
```

| Platform | Tech Stack | Build Output |
|----------|-----------|--------------|
| **Android** | Kotlin, Compose, Room | `.apk` / `.aab` |
| **iOS** | Swift, SwiftUI, SwiftData | `.app` / `.ipa` |

---

## Android

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Open Android Studio → **Open** this project directory
2. Create `.env` from `.env.example` and set `GEMINI_API_KEY`
3. Remove `signingConfig = signingConfigs.getByName("debugConfig")` from `app/build.gradle.kts` for local debug
4. Run on emulator or device

---

## iOS

**Prerequisites:** macOS, Xcode 15+, iOS 17+

1. Open `ios/NMM.xcodeproj` in Xcode
2. Set your **Development Team** under Signing & Capabilities
3. Run with **⌘R** on simulator or device

**CLI build (macOS):**

```bash
cd ios
chmod +x scripts/build_ipa.sh
./scripts/build_ipa.sh --simulator   # .app for Simulator
./scripts/build_ipa.sh --release     # .ipa for device
```

See [ios/README-iOS.md](ios/README-iOS.md) for full iOS documentation.

### Build iOS from Windows (no Mac needed)

Push to GitHub — macOS runners build automatically:

| Workflow | Trigger | Output | Apple account |
|----------|---------|--------|---------------|
| **iOS Build (Simulator)** | Auto on push to `ios/` | `.app` artifact | Not required |
| **iOS Export IPA** | Manual (Actions tab) | `.ipa` artifact | Required + secrets |

```powershell
git push origin main
# GitHub → Actions → download artifact
```

---

## AI Studio

View in AI Studio: https://ai.studio/apps/8e31f754-57d9-4245-a53a-73480cecd308
