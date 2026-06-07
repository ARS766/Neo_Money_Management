#!/bin/bash
# Build NMM iOS app (.app) and optionally export .ipa
# Requires: macOS, Xcode 15+, Apple Developer account for device/IPA builds
#
# Usage:
#   ./scripts/build_ipa.sh              # Simulator .app (Debug)
#   ./scripts/build_ipa.sh --release    # Release archive + IPA export
#   ./scripts/build_ipa.sh --simulator  # Build for iOS Simulator only

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
IOS_DIR="$(dirname "$SCRIPT_DIR")"
PROJECT="$IOS_DIR/NMM.xcodeproj"
SCHEME="NMM"
BUILD_DIR="$IOS_DIR/build"
ARCHIVE_PATH="$BUILD_DIR/NMM.xcarchive"
EXPORT_PATH="$BUILD_DIR/ipa"
EXPORT_OPTIONS="$IOS_DIR/ExportOptions.plist"

MODE="debug"
if [[ "${1:-}" == "--release" ]]; then
  MODE="release"
elif [[ "${1:-}" == "--simulator" ]]; then
  MODE="simulator"
fi

echo "=== NMM iOS Build ($MODE) ==="

if [[ "$MODE" == "simulator" || "$MODE" == "debug" ]]; then
  DESTINATION="generic/platform=iOS Simulator"
  if [[ "$MODE" == "simulator" ]]; then
    DESTINATION="platform=iOS Simulator,name=iPhone 16"
  fi

  xcodebuild \
    -project "$PROJECT" \
    -scheme "$SCHEME" \
    -configuration Debug \
    -destination "$DESTINATION" \
    -derivedDataPath "$BUILD_DIR/DerivedData" \
    build

  APP_PATH=$(find "$BUILD_DIR/DerivedData" -name "NMM.app" -type d | head -1)
  echo ""
  echo "✅ Build succeeded!"
  echo "   .app location: $APP_PATH"
  exit 0
fi

# Release: Archive + Export IPA
echo "Archiving for device..."
xcodebuild \
  -project "$PROJECT" \
  -scheme "$SCHEME" \
  -configuration Release \
  -destination "generic/platform=iOS" \
  -archivePath "$ARCHIVE_PATH" \
  archive

if [[ ! -f "$EXPORT_OPTIONS" ]]; then
  echo "⚠️  ExportOptions.plist not found. Creating template..."
  cat > "$EXPORT_OPTIONS" << 'PLIST'
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>development</string>
    <key>teamID</key>
    <string>YOUR_TEAM_ID</string>
    <key>signingStyle</key>
    <string>automatic</string>
</dict>
</plist>
PLIST
  echo "   Edit ios/ExportOptions.plist with your Team ID, then re-run."
  exit 1
fi

echo "Exporting IPA..."
xcodebuild \
  -exportArchive \
  -archivePath "$ARCHIVE_PATH" \
  -exportPath "$EXPORT_PATH" \
  -exportOptionsPlist "$EXPORT_OPTIONS"

IPA=$(find "$EXPORT_PATH" -name "*.ipa" | head -1)
echo ""
echo "✅ IPA exported!"
echo "   .ipa location: $IPA"
