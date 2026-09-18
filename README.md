# TrackScooter Mobile 🛴

Client Android native (Kotlin) untuk **TrackScooter** — sistem manajemen inventaris, penyewaan, dan pelacakan unit scooter secara real-time. App ini memakai **backend & database yang sama** dengan web admin ([qr-track-scooter](https://github.com/sanixmon/qr-track-scooter)) lewat REST API.

## ✨ Fitur (full parity dengan web admin)

- **📊 Dashboard** — statistik real-time (Online, Offline/Rusak, Maintenance, Total), grid unit dengan filter status/jenis, riwayat aktivitas lengkap (cari + filter + paginasi), ringkasan per jenis, feed aktivitas, dan tabel status maintenance dengan tombol **Selesai**.
- **📸 Scan QR** — pindai lewat kamera (CameraX + ML Kit), upload gambar dari galeri, atau input ID manual. Unit berstatus *maintenance/rusak* wajib konfirmasi sebelum disewakan.
- **🛠️ Kelola Unit** — tambah unit (ID kustom/auto), ubah status (dengan dialog lokasi + kendala untuk maintenance), hapus unit, unduh QR per unit / semua unit (ZIP), export kondisi unit, backup database.
- **📊 Monitor** — live view hari ini atau riwayat per tanggal (navigator tanggal + date picker), statistik harian (total/keluar/masuk), panel status unit dengan filter, feed aktivitas per tanggal, export laporan harian.
- **🔍 Detail Unit** — kondisi perangkat (Spakbor, Lampu, Baterai, Jenis Error, Rem, Ban) dengan tone warna, tombol "Semua Normal" + Simpan, panel maintenance berjalan, dan riwayat unit + maintenance (export CSV).

> **Catatan export:** file Excel (.xlsx) di web digantikan format **CSV** di Android (tetap bisa dibuka di Excel). File tersimpan di folder **Downloads** perangkat.

## 🛠️ Teknologi

- **Kotlin** + **Jetpack Compose** (Material 3), Navigation Compose, ViewModel + StateFlow
- **Retrofit** + kotlinx.serialization (API), polling real-time 30 detik
- **CameraX** + **ML Kit** barcode scanning, **ZXing** untuk generate QR
- **minSdk 26** (Android 8.0) · **targetSdk/compileSdk 35**

## 🚀 Build & Rilis Otomatis

- **Auto Releases** (`.github/workflows/release.yml`): Setiap push tag (contoh: `git tag v1.0.0 && git push origin v1.0.0`) atau trigger manual di GitHub Actions akan otomatis mengompilasi APK (signed release & debug) dan mempublikasikannya ke tab **[Releases](https://github.com/sanixmon/track-mobile/releases)** lengkap dengan catatan rilis otomatis dan checksum SHA-256.
- **Continuous Integration** (`.github/workflows/android.yml`): Setiap push/PR ke `main` otomatis memvalidasi build dan mengunggah artifact APK.

Build lokal:

```bash
./gradlew assembleDebug          # APK debug
./gradlew assembleRelease        # APK release
```

Hasil di `app/build/outputs/apk/`.

## 🔌 Konfigurasi API

Base URL API default: **`https://qr.evrenhouse.online`** (nginx → Express API di `127.0.0.1:3005`).

Override per build tanpa mengubah kode:

```bash
./gradlew assembleDebug -PAPI_BASE_URL=http://192.168.1.10:3005
```

## 📁 Struktur

```text
├── app/src/main/java/com/evrenhouse/trackscooter/
│   ├── data/          # DTO, Retrofit ApiService, ScooterRepository
│   ├── ui/
│   │   ├── navigation/  # NavHost + bottom bar
│   │   ├── dashboard/   # Dashboard
│   │   ├── monitor/     # Monitor harian
│   │   ├── scan/        # Scan QR (camera/gallery/manual)
│   │   ├── manage/      # Kelola unit
│   │   ├── detail/      # Detail unit + kondisi perangkat
│   │   ├── common/      # Komponen bersama + shared ViewModel
│   │   └── theme/       # Dark theme (senada web)
│   └── util/          # DateUtils (WIB), QR, exporter CSV/ZIP
├── .github/workflows/android.yml
└── gradle/libs.versions.toml
```

## 📝 Catatan

- Semua teks UI berbahasa Indonesia, konsisten dengan web admin.
- Status unit: `available` (Tersedia) · `in-use` (Online) · `rusak` (Offline/Rusak) · `maintenance`.
- API tidak memakai autentikasi (sama seperti web) — jangan expose base URL non-produksi ke publik.
