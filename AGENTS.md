# Agent Guidelines - TrackScooter Mobile

## 1. Versioning & Bump Protocol

Semua agent dan pengembang **WAJIB** mengikuti Semantic Versioning terpusat melalui file `version.properties` di root project (`/home/ubuntu/mobile/track-mobile/version.properties`):

```properties
VERSION_MAJOR=2
VERSION_MINOR=7
VERSION_PATCH=0
VERSION_BUILD=19
```

- **Perubahan Kecil / Bugfix / Styling / Refactor**:
  - **DILARANG** langsung menaikkan `VERSION_MINOR` (misal dari 2.7 ke 2.8).
  - **WAJIB** hanya menaikkan `VERSION_PATCH` (misal `2.7.0` -> `2.7.1`).
  - **WAJIB** menaikkan `VERSION_BUILD` +1 setiap kali rilis APK baru (misal `19` -> `20`).
- **Fitur Baru Besar**:
  - Naikkan `VERSION_MINOR` (misal `2.8.0`), reset `VERSION_PATCH=0`, dan naikkan `VERSION_BUILD` +1.
- **Perombakan Arsitektur / Sistem Total**:
  - Naikkan `VERSION_MAJOR` (misal `3.0.0`), reset MINOR dan PATCH ke 0.

## 2. Release Signing & Play Protect Compliance

- **Keystore Permanen**:
  - Release APK **HARUS SELALU** ditandatangani menggunakan release keystore resmi (`keystore/release.jks` atau secret CI `RELEASE_KEYSTORE_BASE64`).
  - **DILARANG** mengembalikan `signingConfig` release ke `debug` agar aplikasi tetap updateable tanpa signature mismatch dan tidak terdeteksi bahaya oleh Google Play Protect.
- **ABI Filters**:
  - Pertahankan filter ABI hanya untuk `arm64-v8a` dan `armeabi-v7a` di `defaultConfig` agar APK tidak membengkak oleh binary emulator x86/x86_64.
- **Izin Manifest**:
  - Jangan menambahkan izin berisiko tinggi (`ACCESS_FINE_LOCATION`, `SMS`, `STORAGE_MANAGER`) kecuali benar-benar dibutuhkan oleh spesifikasi domain.


## 3. Build & Release Protocol (GitHub CI & GH CLI)

- **Semua Build Bergantung pada GitHub CI**:
  - Lingkungan lokal tidak melakukan build APK langsung. Semua build didelegasikan dan dijalankan melalui GitHub CI (`gh` CLI / GitHub Actions).
  - Pemicuan workflow build dilakukan via `gh workflow run` atau push ke repository remote.
- **Larangan Release APK Otomatis**:
  - **DILARANG** melakukan build atau trigger release APK secara sembarangan/otomatis.
  - Jika ingin melakukan build, **WAJIB** build debug terlebih dahulu (`android.yml` / debug APK).
  - Release APK **HANYA** boleh dilakukan ketika pengguna secara eksplisit meminta rilis.

## Agent skills

### Issue tracker

GitHub Issues via `gh` CLI. See `docs/agents/issue-tracker.md`.

### Triage labels

Default canonical roles (`needs-triage`, `needs-info`, `ready-for-agent`, `ready-for-human`, `wontfix`). See `docs/agents/triage-labels.md`.

### Domain docs

Single-context (`CONTEXT.md` and `docs/adr/` at repo root). See `docs/agents/domain.md`.
