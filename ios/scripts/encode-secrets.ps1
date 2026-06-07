# Panduan encode sertifikat Apple untuk GitHub Secrets (Windows PowerShell)

# 1. Export .p12 dari Keychain Access (Mac) atau buat di developer.apple.com
# 2. Encode certificate:
#    [Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\certificate.p12")) | Set-Clipboard

# 3. Encode provisioning profile:
#    [Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\NMM.mobileprovision")) | Set-Clipboard

# GitHub Secrets yang diperlukan (Settings → Secrets → Actions):
#
# | Secret name                      | Isi                                      |
# |----------------------------------|------------------------------------------|
# | APPLE_TEAM_ID                    | Team ID 10 karakter (developer.apple.com)|
# | BUILD_CERTIFICATE_BASE64         | Output base64 dari .p12                  |
# | P12_PASSWORD                     | Password saat export .p12                |
# | BUILD_PROVISION_PROFILE_BASE64   | Output base64 dari .mobileprovision      |
# | KEYCHAIN_PASSWORD                | String acak (mis. openssl rand -hex 16)  |

Write-Host @"

=== NMM iOS - GitHub Actions Setup ===

WORKFLOW OTOMATIS (tanpa akun Apple):
  Push ke GitHub → tab Actions → 'iOS Build (Simulator)'
  Download artifact: NMM-Simulator-app

WORKFLOW IPA (butuh Apple Developer):
  1. Isi secrets di atas
  2. Actions → 'iOS Export IPA' → Run workflow
  3. Download artifact: NMM-ipa-development

Dari Windows, cukup:
  git add .
  git commit -m "update ios"
  git push

"@
