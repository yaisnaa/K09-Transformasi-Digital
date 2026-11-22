# PB10: Rencana Implementasi Desain High Fidelity

Panduan kerja untuk menerapkan desain Baseline v3 ke kode. Dikerjakan berurutan per fase; setiap fase berakhir dengan build hijau, unit test hijau, dan screenshot emulator yang dibandingkan dengan desain.

## Sumber acuan

| Sumber | Isi | Kapan dipakai |
|---|---|---|
| `DESIGN.md` | Token warna, huruf, bentuk, komponen, katalog state | Setiap menulis UI. Kalau berbeda dengan kanvas, ikuti file ini |
| Kanvas desain "HistoryGo Baseline Design" (link di grup tim) | 21 layar + 6 lembar state, sebagian bisa diklik | Melihat tampilan dan alur |
| `docs/design/baseline-v3/` | Ekspor PNG kanvas | Pembanding berdampingan dengan screenshot emulator |
| `PRODUCT.md` | Pengguna, konteks pemakaian, prinsip produk | Memutuskan hal yang tidak tertulis di desain |

## Aturan kerja

1. Branch `feature/pb10-ui` dibuat dari `dev`. Satu commit per fase, merge ke `dev` lewat Pull Request.
2. Hanya mengubah tampilan dan alur UI. Logika trigger, navigasi, rekomendasi, dan game tidak diubah, kecuali yang disebut di fase terkait (state "mendekati" dan jam tiba).
3. Semua warna dan ukuran diambil dari `ui/theme/`. Dilarang hex atau ukuran huruf mentah di layar.
4. Semua teks lewat `AppStrings` (ID dan EN). Tidak ada string di Composable.
5. Fitur dianggap selesai hanya setelah dicek di emulator. Hal yang butuh HP asli (GPS nyata, notifikasi latar, layar terkunci) ditulis "belum teruji".

## Keputusan teknis

| Topik | Keputusan | Alasan |
|---|---|---|
| Gaya peta | JSON lokal lewat `MapStyleOptions` (`res/raw/map_style.json` dan `map_style_dark.json`) | Tanpa Map ID dan tanpa biaya tambahan. Cloud styling butuh Map ID yang ditagih per muat peta. Kalau di emulator gaya tidak diterapkan, pindah ke cloud styling dan catat di `DECISIONS.md` |
| Warna | Skema statis terang dan gelap, tanpa Dynamic Color | Palet brand harus sama di semua HP |
| Font | DM Serif Display, Inter, IBM Plex Mono dibundel di `res/font/` | Tidak bergantung koneksi; lisensi OFL |
| Navigasi layar | Tetap state di `MainActivity` (sealed class `Screen`) + `BackHandler`, tanpa library navigasi baru | Perubahan kecil dari pola sekarang |
| Jarak "mendekati" | Parameter baru `APPROACH_DISTANCE_M` di `TriggerConfig.kt`, **100 m** | Syarat PB10 (state "mendekati"). Disetujui tim 2026-11-22, tercatat di `DECISIONS.md` |
| Jam tiba di rekomendasi | Ditampilkan "tiba ±14.07" (jam sekarang + menit selesai) | Hasil critique: "selesai di menit ke-7" ambigu. Perubahan tampilan, bukan requirement |

## Fase

### F0. Persiapan
- [x] Buat branch `feature/pb10-ui` dari `dev`.
- [ ] Ekspor PNG semua artboard v3 dan lembar state ke `docs/design/baseline-v3/`.
- [ ] Tim mengonfirmasi `APPROACH_DISTANCE_M`, lalu dicatat di `DECISIONS.md`.

### F1. Fondasi tema
- [x] Font ke `res/font/` (`dm_serif_display.ttf`, `inter_*.ttf`, `ibm_plex_mono_medium.ttf`).
- [x] `ui/theme/Color.kt`: token `DESIGN.md` + peran M3 terang dan gelap.
- [x] `ui/theme/Type.kt`: skala huruf Material dipetakan ke tiga font (sp).
- [x] `ui/theme/Shape.kt`: 8/10/16/20/24dp.
- [x] `ui/theme/Theme.kt`: matikan Dynamic Color, pilih skema dari tema sistem.
- **Cek:** build hijau; layar yang ada sudah memakai warna dan font baru.

### F2. Kerangka navigasi
- [x] `Screen` sealed class: Jelajah, Rute, JejakSejarah, Riwayat, Detail(id), Cari, Profil, Pengaturan, Onboarding.
- [x] Komponen `HistoryGoNavBar` (4 tujuan, indikator pil).
- [x] Layar Pengaturan baru: rencana kunjungan, bahasa (segmented), autoplay, deteksi on/off, Tentang konten. Toggle bahasa dan autoplay dipindah dari `LocationScreen`.
- **Cek:** semua tab bisa dibuka; tombol Back sistem kembali dengan benar.

### F3. Peta (Explore Map)
- [x] `map_style.json` dan `map_style_dark.json` sesuai warna peta di `DESIGN.md`; sembunyikan `poi.business`.
- [x] Marker belah ketupat (`MarkerComposable`), marker terpicu, titik posisi, radius 25 m (`Circle`), label nama.
- [x] Bar atas (nama app, chip status, tombol Pengaturan) dan search bar.
- [x] Sheet per state: di luar area, mendekati, terpicu (kartu POI baru), izin ditolak, GPS mati, mencari lokasi.
- [x] Logika "mendekati" di `trigger/` (Kotlin murni) + unit test, memakai `APPROACH_DISTANCE_M`.
- **Cek emulator:** lokasi palsu di luar area, ±60 m dari P01, dan di dalam 25 m (perintah di bawah).

### F4. POI Detail
- [x] Hero foto + sheet membulat, judul display, chip status, ornamen.
- [x] Bar narasi (play dan jeda, chip "Audio contoh" selama audio placeholder).
- [x] Plakat infografis 2 kolom.
- [x] Kartu Misi Jejak Sejarah: semua state check-in dan kuis dari lembar "State: check-in, kuis". Kuis tanpa pilihan awal.
- [x] Bar "Navigasi ke sini" tetap di bawah; versi nonaktif untuk POI tanpa koordinat.

### F5. Layar fitur
- [x] Cari: search field, chip filter, hasil dengan jarak, tanpa lokasi, tidak ditemukan.
- [x] Kuesioner profil: kartu durasi, checkbox minat, tombol nonaktif tanpa minat.
- [x] Rute rekomendasi: timeline belah ketupat, jam tiba, catatan kontekstual, state kosong.
- [x] Kartu navigasi: berjalan, menghitung, menunggu lokasi, menghitung ulang, gagal, tiba, beberapa titik.
- [x] Jejak Sejarah: progres belah ketupat, poin, badge terkunci/terbuka, daftar misi, ulangi misi dengan dialog konfirmasi.
- [x] Riwayat: judul baru, state kosong, hapus dengan snackbar Urungkan.

### F6. Onboarding dan izin
- [x] Dua layar onboarding, tampil sekali (flag di `SettingsStore`).
- [x] Layar izin menjelaskan lokasi dan notifikasi sebelum dialog sistem; "Jelajah tanpa lokasi" langsung ke peta.

### F7. Ikon dan notifikasi
- [x] Adaptive icon vector (`ic_launcher_foreground.xml`, latar Chestnut) dari artboard Ikon.
- [x] Ikon notifikasi satu warna (`ic_stat_historygo.xml`) menggantikan `android.R.drawable.ic_dialog_map` di `TriggerService`.
- [x] Warna aksen notifikasi Chestnut.

### F8. Teks
- [x] Perbarui `AppStrings` ID dan EN: "Tempat yang kamu lewati", "Tempat tidak ditemukan.", jam tiba, teks onboarding, izin, Pengaturan, Tentang konten, state baru.
- [x] `TranslationTest` tetap hijau (semua kunci punya terjemahan).

### F9. Verifikasi akhir
- [x] `./gradlew testDebugUnitTest` hijau.
- [x] Screenshot emulator semua layar, terang dan gelap, disimpan di `docs/design/impl/` dan dibandingkan berdampingan dengan `docs/design/baseline-v3/`.
- [x] Font scale 1.3: tidak ada teks terpotong.
- [x] Empat state prototype PRD dapat didemokan: di luar area, mendekati, terpicu, akses konten.
- [ ] Acceptance F01-F06 dicek ulang di emulator (tidak rusak karena perubahan UI).
- [ ] Entri Development Log / AI & Tools Disclosure untuk PB10.

## Status (2026-11-21)

F1-F9 selesai di branch `feature/pb10-ui`, satu commit per fase. Bukti screenshot emulator di `docs/design/impl/` (nama file diawali nomor fase).

Teruji di emulator (`Medium_Phone_API_37.0`, lokasi palsu):
- Empat state peta: di luar area, mendekati (±60 m), terpicu, akses konten
- Izin ditolak / GPS mati / deteksi dimatikan (service benar-benar berhenti dan hidup lagi)
- POI Detail, check-in, kuis tanpa pilihan awal, jawaban salah, misi selesai
- Narasi diputar dan tombol berubah menjadi "Jeda cerita" (media session PLAYING)
- Cari, kuesioner, rute rekomendasi, navigasi dengan Routes API, Jejak Sejarah, Riwayat + Urungkan
- Onboarding pada instalasi baru, dialog izin sistem setelah penjelasan, ikon launcher baru
- Tema gelap, bahasa Inggris, ukuran huruf 1.3
- `./gradlew testDebugUnitTest`: 61 test hijau (termasuk 7 test baru `ApproachTest`)

Belum teruji (butuh HP fisik, PB07/PB14):
- Notifikasi POI saat aplikasi di latar dan ikon notifikasi baru di status bar
- Narasi saat layar terkunci dan kontrol dari lock screen
- Akurasi GPS nyata untuk state "mendekati" (100 m) dan trigger 25 m
- Map style di HP fisik dan perilaku penghemat baterai OEM

Masih terbuka:
- Ekspor PNG artboard v3 ke `docs/design/baseline-v3/` untuk perbandingan berdampingan
- Acceptance F01-F06 lengkap per butir (compliance matrix) dan entri Development Log

## Cara menguji di emulator

AVD yang tersedia: `Medium_Phone_API_37.0`.

```bash
# 1. Jalankan emulator (biarkan terbuka)
"$LOCALAPPDATA/Android/Sdk/emulator/emulator.exe" -avd Medium_Phone_API_37.0

# 2. Build, pasang, buka aplikasi
./gradlew installDebug
adb shell am start -n id.ac.itb.k09.historygo/.MainActivity

# 3. Lokasi palsu (urutan: BUJUR LINTANG)
adb emu geo fix 107.6125 -6.9180      # di luar area
adb emu geo fix 107.6097 -6.9212      # mendekati P01 (±60 m)
adb emu geo fix 107.609154 -6.921376  # di titik P01, kirim 2x agar trigger terkonfirmasi

# 4. Screenshot
adb exec-out screencap -p > docs/design/impl/peta-terpicu.png

# 5. Tema gelap dan ukuran huruf (kembalikan setelah selesai)
adb shell cmd uimode night yes
adb shell settings put system font_scale 1.3
adb shell cmd uimode night no
adb shell settings put system font_scale 1.0
```

Yang tidak bisa dibuktikan di emulator dan tetap "belum teruji" sampai uji HP fisik (PB07/PB14): akurasi GPS nyata, notifikasi saat HP di saku, audio saat layar terkunci di HP sungguhan, dan perilaku penghemat baterai OEM.

## Pemetaan desain ke kode

| Artboard | File Kotlin |
|---|---|
| Gaya dasar | `ui/theme/Color.kt`, `Type.kt`, `Shape.kt`, `Theme.kt` |
| Komponen: navigation bar | `ui/components/HistoryGoNavBar.kt` (baru) |
| Komponen: peta | `ui/ExploreMap.kt`, `res/raw/map_style*.json` |
| Peta: di luar area / mendekati / terpicu / izin ditolak / tema gelap | `ui/LocationScreen.kt`, `ui/PoiCard.kt` |
| Onboarding, Izin | `ui/OnboardingScreen.kt` (baru) |
| POI Detail, check-in & kuis | `ui/PoiDetailScreen.kt`, `ui/CheckInSection.kt` |
| Navigasi berjalan / gagal, State: navigasi | `ui/NavigationCard.kt` |
| Cari tempat | `ui/SearchScreen.kt` |
| Kuesioner profil | `ui/ProfileScreen.kt` |
| Rute rekomendasi | `ui/RecommendationScreen.kt` |
| Jejak Sejarah | `ui/GameScreen.kt` |
| Tempat yang kamu lewati | `ui/HistoryScreen.kt` |
| Pengaturan | `ui/SettingsScreen.kt` (baru) |
| Ikon aplikasi & notifikasi | `res/mipmap-anydpi-v26/`, `res/drawable/`, `location/TriggerService.kt` |
