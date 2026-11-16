# Duitku untuk Android — Afief & Intan

Pencatat keuangan pribadi (Kotlin + Jetpack Compose + Room), porting dari versi iOS
(SwiftUI + SwiftData). Semua data tersimpan lokal di perangkat, tanpa akun dan tanpa internet.

- Nama paket: `id.afiefintan.duitku`
- Android 8.0 (API 26) ke atas
- Identitas pembuat "Afief & Intan" tampil di: bar judul Beranda, layar kunci,
  footer Beranda, dan Pengaturan → Tentang. Semuanya dari satu tempat: `Identitas.kt`.

## Cara mendapatkan file APK

### Cara A — Android Studio (paling mudah)

1. Pasang **Android Studio** (versi Ladybug 2024.2 atau lebih baru) dari developer.android.com/studio.
2. **File → Open**, pilih folder `DuitkuAndroid` ini. Tunggu *Gradle sync* selesai
   (pertama kali mengunduh dependensi, butuh internet beberapa menit).
3. Menu **Build → Build Bundle(s) / APK(s) → Build APK(s)**.
4. Klik **locate** pada notifikasi yang muncul. File ada di:
   `app/build/outputs/apk/debug/app-debug.apk`
   Untuk versi rilis (lebih ringan dan cepat): **Build → Generate Signed App Bundle / APK**,
   atau jalankan `assembleRelease` → `app/build/outputs/apk/release/app-release.apk`.

> Folder ini hanya berisi `gradle-wrapper.properties` (tanpa `gradlew` dan `gradle-wrapper.jar`).
> Android Studio tetap bisa membukanya dan mengunduh Gradle 8.9 sendiri. Bila ingin
> membuat wrapper lengkap: pasang Gradle 8.9 lalu jalankan `gradle wrapper`.

### Cara B — GitHub Actions (tanpa memasang apa pun di komputer)

1. Buat repositori GitHub baru, lalu unggah seluruh isi folder ini (termasuk folder `.github`).
2. Buka tab **Actions → Build APK → Run workflow** (atau otomatis berjalan setiap *push* ke `main`).
3. Setelah selesai (sekitar 5–10 menit), unduh artefak **Duitku-APK**. Isinya:
   `Duitku-Afief-Intan.apk` (rilis) dan `Duitku-Afief-Intan-debug.apk`.

### Cara C — Baris perintah

Syarat: JDK 17, Android SDK (platform 35), Gradle 8.9.

```
gradle assembleRelease
```

## Memasang di HP Android

1. Kirim file APK ke HP (kabel USB, Google Drive, WhatsApp, dsb.).
2. Buka file tersebut. Jika diminta, izinkan **Pasang aplikasi dari sumber ini** untuk aplikasi
   yang dipakai membuka file (Files / Chrome / WhatsApp).
3. Ketuk **Pasang**.

APK rilis di proyek ini ditandatangani dengan kunci *debug* supaya langsung bisa dipasang.
Untuk menerbitkan di Google Play, buat keystore sendiri dan ganti `signingConfig` di
`app/build.gradle.kts`.

## Padanan dengan versi iOS

| iOS | Android |
|---|---|
| SwiftUI | Jetpack Compose + Material 3 |
| SwiftData | Room (SQLite) |
| Swift Charts | Grafik donat dan batang digambar dengan Canvas |
| Face ID / Touch ID / kode sandi | BiometricPrompt (sidik jari / wajah / PIN / pola) |
| Share sheet (CSV) | Menu bagikan Android via FileProvider |
| `@AppStorage` | SharedPreferences |

Fitur yang sama: 3 jenis transaksi, banyak dompet, anggaran bulanan dengan indikator warna dan
batas harian, laporan donat + tren 6 bulan, pencarian dan filter, sembunyikan saldo, tema
terang/gelap, kunci aplikasi, ekspor CSV, kelola dompet dan kategori.

Penyesuaian untuk Android:

- Geser-untuk-hapus diganti tombol **Hapus** di layar ubah (transaksi, anggaran, dompet, kategori).
- Saat kunci aktif, isi aplikasi disembunyikan di daftar aplikasi terbaru dan tangkapan layar diblokir.
- Orientasi dikunci potret.
- Nilai uang memakai `Long` (di iOS `Int` 64-bit), jadi aman untuk angka di atas 2 miliar.

## Struktur kode (`app/src/main/java/id/afiefintan/duitku`)

| File | Isi |
|---|---|
| `MainActivity.kt` | Entry point, tema, kunci aplikasi |
| `RootScreen.kt` | Tab utama dan tumpukan layar |
| `Models.kt`, `Database.kt` | Entitas dan DAO Room |
| `DuitkuViewModel.kt` | State, pengaturan, aksi data |
| `Formatters.kt` | Rupiah, tanggal Indonesia, ikon, perhitungan, data bawaan, CSV |
| `Components.kt`, `Theme.kt` | Komponen UI bersama dan tema |
| `HomeScreen.kt`, `TransaksiListScreen.kt`, `TransaksiFormScreen.kt` | Beranda dan transaksi |
| `AnggaranScreens.kt`, `LaporanScreen.kt` | Anggaran dan laporan |
| `ManageScreens.kt`, `SettingsScreen.kt` | Kelola dompet/kategori, pengaturan |
| `Kunci.kt`, `Ekspor.kt`, `Identitas.kt` | Biometrik, bagikan CSV, nama pembuat |

## Catatan penting

Kode ini ditulis dan diperiksa secara statis (kurung seimbang, XML valid, impor), tetapi
**belum pernah dikompilasi**, karena lingkungan tempat kode ini dibuat tidak punya Android SDK,
Kotlin, Gradle, maupun internet. Jika build pertama menampilkan galat, salin pesannya
(tab *Build* di Android Studio, atau log langkah *Build APK* di GitHub Actions) dan perbaikannya
biasanya satu-dua baris.
