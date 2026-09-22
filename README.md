# TrackScooter Mobile

Client Android native (Kotlin) untuk **TrackScooter** — sistem manajemen inventaris, penyewaan, dan pelacakan unit scooter secara real-time. App ini memakai **backend & database yang sama** dengan web admin lewat REST API.

## Fitur (full parity dengan web admin)

- **Dashboard** — statistik real-time (Unit Ready, Maint. Luar Outlet, Maint. di Outlet, Unit Total), Log Recent sesi sewa harian, ringkasan per jenis armada, dan tabel status maintenance dengan tombol Selesai.
- **Scan QR** — pindai lewat kamera (CameraX + ML Kit), upload gambar dari galeri, atau input ID manual. Dilengkapi modal konfirmasi untuk setiap aksi sewa dan pengembalian unit.
- **Laporan** — rekapitulasi sesi sewa harian (jam keluar, jam kembali, durasi operasional) dan checklist kehadiran fisik unit outlet dengan filter status kehadiran.
- **Kelola Unit** — tambah unit via modal dialog (ID auto/manual dengan dynamic prefix, deteksi duplikasi instan, penugasan outlet otomatis), ubah status unit, unduh QR code, export data kondisi unit, dan backup database.
- **Live Monitor** — pemantauan real-time unit yang sedang berjalan di luar (timer live, indikator jeda istirahat, tombol tukar unit) dan feed log aktivitas hari ini.
- **Detail Unit** — kondisi fisik perangkat (Spakbor, Lampu, Baterai, Jenis Error, Rem, Ban), panel maintenance aktif, dan riwayat perbaikan serta aktivitas unit.

> **Catatan export:** file Excel (.xlsx) di web digantikan format **CSV** di Android (dapat dibuka di Excel). File tersimpan di folder Downloads perangkat.

## Teknologi

- Kotlin + Jetpack Compose (Material 3), Navigation Compose, ViewModel + StateFlow
- Retrofit + kotlinx.serialization (API), polling real-time 30 detik
- CameraX + ML Kit barcode scanning, ZXing untuk generate QR
- minSdk 26 (Android 8.0) · targetSdk/compileSdk 35

## Build & Rilis Otomatis

- **Auto Releases** (`.github/workflows/release.yml`): Setiap push tag atau trigger manual di GitHub Actions akan otomatis mengompilasi APK (signed release & debug) dan mempublikasikannya ke tab Releases lengkap dengan catatan rilis otomatis dan checksum SHA-256.
- **Continuous Integration** (`.github/workflows/android.yml`): Setiap push/PR ke `main` otomatis memvalidasi build dan mengunggah artifact APK.

Build lokal:

```bash
./gradlew assembleDebug          # APK debug
./gradlew assembleRelease        # APK release
```

Hasil di `app/build/outputs/apk/`.

## Konfigurasi API

Base URL API default: `https://qr.evrenhouse.online` (nginx -> Express API di loopback).

Override per build tanpa mengubah kode:

```bash
./gradlew assembleDebug -PAPI_BASE_URL=http://192.168.1.10:3005
```

## Struktur

```text
app/src/main/java/com/evrenhouse/trackscooter/
├── data/          # DTO, Retrofit ApiService, ScooterRepository
├── ui/
│   ├── navigation/  # NavHost + bottom bar
│   ├── dashboard/   # Dashboard (stat cards, recent logs, type summary, maintenance)
│   ├── monitor/     # Live monitor (live sessions + activity feed)
│   ├── scan/        # Scan QR + ScanConfirmDialog
│   ├── report/      # Laporan (rekap sesi + checklist kehadiran outlet)
│   ├── manage/      # Kelola unit + AddScooterDialog
│   ├── detail/      # Detail unit + kondisi perangkat
│   ├── common/      # Komponen bersama + shared ViewModel + OutletDropdown
│   └── theme/       # Dark theme (senada web)
└── util/          # DateUtils (WIB), QR, exporter CSV/ZIP, Outlets
```

## Catatan

- Semua teks UI berbahasa Indonesia, konsisten dengan web admin.
- Status unit: `available` (Unit Ready) · `in-use` (Unit Diluar) · `maintenance` (Unit Kendala).
- API tidak memakai autentikasi (sama seperti web) — jangan expose base URL non-produksi ke publik.
