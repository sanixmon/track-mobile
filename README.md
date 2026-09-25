# TrackScooter Mobile

Client Android native (Kotlin) untuk **TrackScooter** — sistem manajemen inventaris, penyewaan, dan pelacakan unit scooter secara real-time. App ini memakai **backend & database yang sama** dengan web admin lewat REST API.

## Fitur (full parity dengan web admin)

- **Dashboard** — statistik real-time (Unit Ready, Maint. Luar Outlet, Maint. di Outlet, Unit Total), Log Recent sesi sewa harian, ringkasan per jenis armada, dan tabel status maintenance dengan tombol Selesai.
- **Scan QR** — pindai lewat kamera (CameraX + ML Kit), upload gambar dari galeri, atau input ID manual. Dilengkapi modal konfirmasi untuk setiap aksi sewa dan pengembalian unit.
- **Laporan** — rekapitulasi sesi sewa harian (jam keluar, jam kembali, durasi operasional) dan checklist kehadiran fisik unit outlet dengan filter status kehadiran.
- **Kelola Unit** — tambah unit via modal dialog (ID auto/manual dengan dynamic prefix, deteksi duplikasi instan, penugasan outlet otomatis), ubah status unit, unduh QR code, export data kondisi unit, dan backup database.
- **Live Monitor** — pemantauan real-time unit yang sedang berjalan di luar (timer live, indikator jeda istirahat, tombol tukar unit) dan feed log aktivitas hari ini.
- **Detail Unit** — kondisi fisik perangkat (Spakbor, Lampu, Baterai, Jenis Error, Rem, Ban), panel maintenance aktif, dan riwayat perbaikan serta aktivitas unit.
- **Pembaruan Otomatis (Auto-Update)** — deteksi rilis APK versi terbaru via endpoint backend (`/api/app-version`), pengalihan (*redirect*) instan ke tautan unduhan, dialog modal rilis interaktif dengan catatan perubahan (*changelog*), dan tombol pengecekan manual di menu *Kelola*.

## Teknologi

- Kotlin + Jetpack Compose (Material 3), Navigation Compose, ViewModel + StateFlow
- Retrofit + kotlinx.serialization (API), polling real-time 30 detik
- CameraX + ML Kit barcode scanning, ZXing untuk generate QR
- minSdk 26 (Android 8.0) · targetSdk/compileSdk 35

## Versioning & Bump Versi

Aplikasi menggunakan standar Semantic Versioning yang dikontrol secara terpusat lewat file `version.properties`:

```properties
VERSION_MAJOR=2
VERSION_MINOR=7
VERSION_PATCH=1
VERSION_BUILD=20
```

- **PATCH (`VERSION_PATCH`)**: Naikkan untuk bugfix, styling, tweak UI, atau perbaikan kecil lainnya (misal `2.7.0` -> `2.7.1`).
- **MINOR (`VERSION_MINOR`)**: Naikkan saat ada fitur baru besar yang selesai (reset PATCH ke 0, misal `2.8.0`).
- **BUILD (`VERSION_BUILD`)**: Wajib selalu naik `+1` setiap kali menghasilkan APK baru agar Android dapat memperbarui instalasi.

## Build & Rilis Otomatis

- **Auto Releases** (`.github/workflows/release.yml`): Setiap push tag atau trigger manual di GitHub Actions akan otomatis mengompilasi APK (signed release & debug) dan mempublikasikannya ke tab Releases lengkap dengan catatan rilis otomatis dan checksum SHA-256.
- **Continuous Integration** (`.github/workflows/android.yml`): Setiap push/PR ke `main` otomatis memvalidasi build dan mengunggah artifact APK.
Build lokal:

```bash
./gradlew assembleDebug          # APK debug
./gradlew assembleRelease        # APK release
```

Hasil di `app/build/outputs/apk/`.

## Sistem Auto-Update & Sinkronisasi Versi

Aplikasi secara otomatis memeriksa ketersediaan pembaruan saat pertama kali dibuka:

1. **Endpoint Backend**: `GET /api/app-version` (atau `/api/version`) pada base URL API mengembalikan metadata versi terbaru dalam format JSON:
   ```json
   {
     "versionCode": 20,
     "versionName": "2.7.1",
     "downloadUrl": "https://github.com/sanixmon/track-mobile/releases/latest",
     "minVersionCode": 19,
     "forceUpdate": false,
     "title": "Pembaruan Tersedia",
     "changelog": "Versi 2.7.1: Pembaruan otomatis & pengalihan tautan unduh versi terbaru."
   }
   ```
2. **Pengalihan Langsung (*Instant Redirect*)**: Jika `versionCode` atau `versionName` remote lebih tinggi dari versi yang terpasang, aplikasi langsung membuka tautan unduh di browser perangkat secara otomatis (`Intent.ACTION_VIEW`).
3. **Dialog Rilis**: Menampilkan modal pembaruan dengan perbandingan versi (`v2.7.0 ➔ v2.7.1`), catatan rilis, dan tombol unduh manual untuk mencegah loop pembukaan browser jika pengguna kembali ke aplikasi.
4. **Pengecekan Manual**: Pengguna dapat memeriksa pembaruan kapan saja melalui menu dropdown **Aksi Data > Cek Pembaruan** pada tab **Kelola**.
5. **Kontrol Backend**: Admin server dapat mengubah versi target dan URL download secara instan lewat file `version.json` di server tanpa perlu me-restart proses backend.
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
│   ├── common/      # Komponen bersama, AppUpdateDialog, shared ViewModel + OutletDropdown
│   └── theme/       # Dark theme (senada web)
└── util/          # DateUtils (WIB), VersionUtils, QR, exporter CSV/XLSX, Outlets
```

## Catatan

- Semua teks UI berbahasa Indonesia, konsisten dengan web admin.
- Status unit: `available` (Unit Ready) · `in-use` (Unit Diluar) · `maintenance` (Unit Kendala).
- API tidak memakai autentikasi (sama seperti web) — jangan expose base URL non-produksi ke publik.
