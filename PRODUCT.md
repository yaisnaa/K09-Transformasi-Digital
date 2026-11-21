# Product

<!-- impeccable:product-schema 1 -->

## Platform

android

## Users

**Pengguna utama: wisatawan lokal** yang berjalan kaki menyusuri koridor cagar budaya Jalan Asia Afrika, Bandung. Mereka sering belum mengenal kawasan, sekadar melintas atau berfoto, tanpa tahu letak bangunan bersejarah dan ceritanya.

Pengguna lain di PRD:
- Turis asing: terlayani lewat mode bahasa Inggris (A01, PB13). Prioritas desain tetap wisatawan lokal, tetapi setiap layar harus tetap rapi saat teks berbahasa Inggris (biasanya lebih panjang).
- Pengelola kawasan: tidak punya layar di aplikasi pada scope saat ini.

## Product Purpose

HistoryGo adalah aplikasi Android panduan wisata berbasis lokasi untuk koridor Jalan Asia Afrika. Aplikasi mendeteksi saat pengguna berada di dekat bangunan bersejarah, lalu menyajikan ceritanya lewat teks, foto, infografis, dan narasi audio. Aplikasi juga menyediakan pencarian dan navigasi jalan kaki.

Berhasil berarti pengguna yang tadinya hanya lewat jadi tahu bangunan apa yang ada di depannya dan cerita sejarahnya, tanpa harus mencari sendiri.

Konteks: tugas besar II4011 Transformasi Digital, ITB, Kelompok 9. Requirement baseline ada di `PRD.md` (F01-F08, A01, N01).

## Positioning

**Cerita muncul otomatis.** Sejarah datang sendiri saat pengguna berdiri di depan bangunan: trigger radius 25 m memunculkan kartu POI (atau notifikasi saat aplikasi di latar), dan narasi audio bisa diputar tanpa melihat layar. Google Maps hanya menunjukkan letak; papan informasi di lokasi harus didatangi dan dibaca. HistoryGo membawa cerita ke pengguna.

## Operating Context

- Pengguna berjalan kaki di trotoar, di luar ruangan, umumnya siang hari dengan sinar matahari terik.
- HP dipegang satu tangan sambil berjalan; perhatian terbagi dengan lalu lintas dan sekitar.
- Kadang memakai earphone; narasi audio adalah saluran utama saat layar terkunci atau di saku.
- Akurasi GPS di koridor umumnya 5-20 m.
- Tanpa izin lokasi, peta, pencarian, dan konten tetap harus bisa diakses manual.

## Capabilities and Constraints

Fitur yang sudah ada di kode (branch `dev`, ditambah PB10 di `feature/pb10-ui`):
- Explore Map (Google Maps SDK) dengan posisi pengguna dan 10 POI (F01)
- Trigger berbasis lokasi lewat foreground service: kartu POI saat aplikasi aktif, notifikasi saat di latar, riwayat "POI yang dilewati" (F02)
- POI Detail: teks sejarah, foto, infografis (tahun dibangun, arsitek, gaya arsitektur, peran historis), sumber (F03)
- Narasi audio Media3 yang tetap berputar saat layar terkunci, kontrol dari notifikasi media, opsi autoplay default mati (F04)
- Pencarian kata kunci parsial dan filter kategori Heritage / Spot Foto / Istirahat-Kuliner, urut terdekat (F05)
- Navigasi jalan kaki lewat Google Routes API: rute, sisa jarak dan waktu, status tiba, hitung ulang rute, termasuk rute beberapa titik singgah (F06)
- Profil dan rekomendasi kontekstual (F07, PB11): kuesioner durasi kunjungan (30/60/90/120 menit) dan minat (tiga kategori yang sama dengan filter pencarian), bisa dilewati. Maksimal 3 rekomendasi, dipilih menurut minat lalu jarak, ditampilkan sebagai satu rute berurutan terdekat-berikutnya dari titik awal (`CHANGELOG.md` CL-02). Tiap rekomendasi berlabel "Sesuai minatmu" atau "Tempat terdekat yang masih muat". Estimasi waktu kunjungan 5 menit per POI ditambah waktu jalan kaki.
- Gamifikasi "Jejak Sejarah" (F08, PB12): check-in hanya di dalam radius POI (+5 poin), satu kuis pilihan ganda per POI (jawaban benar +10 poin, hanya jawaban pertama yang dihitung), progres misi x/N POI, badge "Penjelajah Jejak Sejarah" setelah semua misi selesai. Progres tersimpan di perangkat.
- Dwibahasa ID/EN (A01, PB13): seluruh teks antarmuka dan konten POI, ganti bahasa tanpa restart, pilihan tersimpan.

- Desain high fidelity PB10: navigasi 4 tab, onboarding dengan penjelasan izin, layar Pengaturan (bahasa, autoplay, toggle deteksi, penjelasan label konten), state peta "mendekati" (25-100 m), tema terang dan gelap, ikon aplikasi dan notifikasi baru (`DESIGN.md`, CL-04 sampai CL-06)

Direncanakan, belum diimplementasikan: akun lokal (Masuk/Daftar) dengan profil, riwayat, dan progres Jejak Sejarah per akun, serta mode tamu (CL-03).

Status pengujian fitur di atas: teruji di emulator; perilaku di HP asli dan uji lapangan **belum teruji**.

Constraint teknis:
- Kotlin + Jetpack Compose, target Android saja. Font harus tersedia gratis (Google Fonts) untuk dibundel di aplikasi.
- Peta adalah Google Maps: warna bisa diatur lewat map style, tetapi font label jalan milik Google tidak bisa diganti.
- PB10: ikon aplikasi dan notifikasi, onboarding sebelum meminta izin, dan empat state prototype (di luar area, mendekati POI, terpicu, akses konten) sudah diimplementasikan; teruji di emulator, belum di HP asli.

Istilah: POI (Point of Interest; di teks untuk wisatawan disebut "tempat"), "POI yang dilewati" (riwayat), "Konten contoh" (label konten dummy), "Belum tervalidasi" (konten `validated=false`), "Jejak Sejarah" (nama mode gamifikasi), check-in, misi, poin, badge.

## Brand Commitments

- Nama produk: **HistoryGo** (bukan "Guide to Jalan Asia Afrika").
- Bahasa antarmuka: Indonesia (utama) dan Inggris.
- Arah visual yang dipilih tim: nuansa vintage yang memberi kesan sejarah. Gaya "Jurnal Penjelajah Deco" sudah disetujui (detail di `DESIGN.md`).

## Evidence on Hand

- `app/src/main/assets/poi.json`: 10 POI (P01-P10) dengan ringkasan, infografis, dan sumber. Semua `validated=false` dan `verified=false`. P02 berisi konten dummy (`dummy=true`). Koordinat P06-P10 masih perkiraan (`koordinatDummy=true`).
- `app/src/main/assets/foto/`: foto P01-P04 dan P06-P08 dari Wikimedia Commons dengan atribusi di field `sumber`. P05, P09, P10 belum punya foto.
- `app/src/main/assets/audio/placeholder.wav`: semua POI masih memakai audio placeholder; narasi asli belum ada.
- Kuis: setiap POI di `poi.json` punya satu soal (`kuis`: pertanyaan, 4 pilihan, indeks jawaban). Soal P02 bertanda `dummy=true`.
- Terjemahan Inggris: field `en` di setiap POI (nama, kategori, titik pemicu, ringkasan, infografis, kuis) dan teks antarmuka di `app/src/main/java/id/ac/itb/k09/historygo/i18n/AppStrings.kt`. Status pemeriksaan terjemahan oleh tim belum tercatat.
- `docs/sprint1`-`docs/sprint3/`: screenshot implementasi saat ini (UI Material 3 bawaan, belum high fidelity), termasuk kuesioner profil, rekomendasi, check-in, kuis, Jejak Sejarah, badge, dan mode bahasa.

Jangan dikarang: koordinat, fakta sejarah, sumber rujukan, status "tervalidasi", testimoni, atau angka pengguna. Konten yang belum ada memakai placeholder bertanda jelas (aturan kerja proyek no. 2).

## Product Principles

1. **Cerita datang ke pengguna.** Momen POI terpicu adalah inti produk; kartu dan notifikasi harus langsung jelas tanpa perlu dicari.
2. **Bisa dipakai sambil berjalan.** Satu tangan, sekilas pandang, terbaca di bawah matahari; audio menggantikan layar kapan pun memungkinkan.
3. **Jujur soal kredibilitas konten.** Status "Belum tervalidasi" dan "Konten contoh" selalu terlihat; sumber selalu dicantumkan (N01).
4. **Tetap berguna tanpa lokasi.** Izin ditolak atau GPS mati tidak boleh membuat aplikasi buntu.

## Accessibility & Inclusion

- Kontras tinggi untuk keterbacaan di luar ruangan saat siang hari.
- Target sentuh besar untuk pemakaian satu tangan sambil berjalan.
- Narasi audio sebagai alternatif membaca teks panjang.
- Standar formal (mis. WCAG level tertentu) belum ditetapkan tim.
