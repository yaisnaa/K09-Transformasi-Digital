# HistoryGo PRD (ringkasan untuk pengembangan)

Ringkasan dari PRD Kelompok 9. Untuk parameter trigger, `DECISIONS.md` lebih diutamakan.

## Masalah

Pengunjung Jalan Asia Afrika sering belum mengenal kawasan, sekadar melintas atau berfoto tanpa tahu letak bangunan bersejarah dan ceritanya. Belum ada panduan rute terintegrasi untuk merencanakan tur jalan kaki, memperkirakan waktu tempuh, serta menemukan tempat istirahat dan makan.

## Produk

Aplikasi panduan wisata berbasis lokasi dengan user profiling sederhana, deteksi geolokasi otomatis, navigasi dengan estimasi waktu, konten multimodal (audio dan infografis), serta gamifikasi.

## Pengguna

| Pengguna | Kebutuhan |
|---|---|
| Wisatawan lokal | Rekomendasi tempat, navigasi, informasi historis |
| Turis asing | Informasi dalam bahasa yang dipahami, petunjuk arah mudah, rekomendasi sesuai waktu |
| Pengelola kawasan | Memperbarui informasi destinasi, fasilitas, jam operasional, aksesibilitas |

## Goals

- G01: informasi sejarah kontekstual lewat infografis dan audio sesuai lokasi
- G02: perencanaan rute, estimasi jarak dan waktu, navigasi ke POI dan tempat istirahat/makan
- G03: rekomendasi yang dipersonalisasi
- G04: minat naik lewat gamifikasi treasure hunt dan/atau identifikasi kamera

## Non-goals

- NG01: tidak ada pembelian tiket atau reservasi tempat makan
- NG02: tidak mencakup kawasan di luar koridor Jalan Asia Afrika dan sekitarnya
- NG03: versi awal hanya outdoor GPS geofencing, tanpa navigasi indoor
- NG04: tidak ada pemandu manusia via live chat atau pemesanan tour guide

## Alur utama

1. Inisiasi dan profiling: buka aplikasi, izin GPS dan notifikasi, kuesioner, rute awal di Explore Map
2. Deteksi lokasi: GPS dibaca berkala, cek apakah masuk radius POI
3. Eksplorasi konten: aplikasi aktif menampilkan kartu POI; aplikasi di latar mengirim notifikasi dan menyimpan POI yang dilewati; detail POI berisi teks, infografis, audio, dan check-in misi
4. Pencarian dan navigasi: pilih tujuan, rute jalan kaki dan estimasi waktu
5. Rekomendasi: evaluasi lokasi, riwayat, dan profil; lanjut atau akhiri

## Requirement baseline

Semua berprioritas **Must**.

### F01 Location Awareness (S1)
GPS berkala (Fused Location Provider, interval ±5 detik saat aktif), menampilkan posisi dan seluruh POI di Explore Map.
1. Marker posisi muncul ≤10 detik setelah izin diberikan
2. Marker berpindah saat pengguna berjalan 1-2 m (diubah dari ≥10 m, lihat `CHANGELOG.md` CL-01)
3. Izin ditolak atau GPS mati: pesan dan tombol untuk mengaktifkan lokasi, tanpa crash

### F02 Location-Based Trigger (S2)
Tiap POI punya area lingkaran. Masuk radius: kartu pop-up bila aplikasi aktif, push notification bila di latar. Radius tumpang tindih: hanya POI terdekat.
1. Kartu atau notifikasi muncul ≤10 detik saat masuk radius
2. POI tidak terpicu ulang selama pengguna di area; aktif lagi setelah keluar >40 m lalu masuk lagi
3. Pada radius tumpang tindih hanya satu POI terdekat yang dipicu
4. POI yang terpicu di latar tercatat di "POI yang dilewati"

### F03 Multimodal Exploration (S2)
POI Detail: teks sejarah, foto, infografis ringkas (tahun dibangun, arsitek, gaya arsitektur, peran historis), narasi audio.
1. Tiap POI heritage memiliki minimal teks, 1 foto, 1 infografis, 1 audio
2. Semua elemen terjangkau ≤2 tap dari kartu POI
3. Setiap konten sejarah mencantumkan sumber

### F04 Eyes-Free Interaction (S2)
Narasi satu tombol, tetap berjalan saat layar terkunci atau di latar (Media3), kontrol dari notifikasi media, opsi autoplay.
1. Narasi tetap berbunyi saat layar dikunci atau aplikasi diminimalkan
2. Play/pause dari notifikasi media atau lock screen tanpa membuka aplikasi
3. Jika autoplay aktif, narasi berputar otomatis saat masuk radius POI

### F05 Search & Destination (nanti)
Cari nama atau filter kategori (Heritage, Spot Foto, Tempat Istirahat/Kuliner), urut dari terdekat.
1. Kata kunci parsial menemukan POI
2. Filter hanya menampilkan kategori itu
3. Tiap hasil menampilkan jarak (m) dan waktu jalan kaki (menit)
4. Hasil membuka detail dengan tombol "Navigasi"; hasil kosong menampilkan "tidak ditemukan"

### F06 Navigation (nanti)
Rute jalan kaki (Google Maps SDK/Directions), sisa jarak dan waktu diperbarui.
1. Rute tampil ≤5 detik setelah tujuan dipilih
2. Sisa jarak dan waktu berkurang saat mendekat
3. Status "Tiba di tujuan" saat masuk radius POI tujuan
4. Menyimpang >30 m: rute dihitung ulang

### F07 Contextual Recommendation (nanti)
Berdasarkan profil (durasi, minat), lokasi, dan riwayat.
1. Total estimasi rute ≤ durasi pilihan pengguna
2. Tidak merekomendasikan POI yang sudah dikunjungi
3. Maksimal 3 rekomendasi, urut minat dan jarak
4. Dua profil berbeda menghasilkan rekomendasi berbeda

### F08 "Jejak Sejarah" (nanti)
Treasure hunt: check-in, kuis, petunjuk POI berikutnya. Progres, poin, dan badge disimpan di perangkat.
1. Check-in hanya berhasil di dalam radius POI
2. Progres misi (x/N POI) naik setelah check-in
3. Jawaban benar menambah poin; semua misi selesai membuka badge
4. Progres tetap ada setelah aplikasi ditutup

## Requirement tambahan

- **A01 Bahasa ID/EN (Should):** ganti bahasa mengubah seluruh teks antarmuka dan POI tanpa restart; pilihan tetap tersimpan
- **N01 Kredibilitas konten (Must):** setiap POI heritage punya ≥1 sumber rujukan tercatat; konten ditandai "tervalidasi" sebelum tampil

## POI

Koordinat persis belum tersedia di PRD.

| ID | Nama | Kategori | Titik pemicu |
|---|---|---|---|
| P01 | Gedung Merdeka / Museum KAA | Heritage dan sejarah KAA | Pedestrian depan Gedung Merdeka |
| P02 | Tugu Bola Dunia Asia Afrika | Monumen dan spot foto | Pertemuan Jl. Asia Afrika dan Alun-Alun Timur |
| P03 | Hotel Savoy Homann | Heritage dan arsitektur | Trotoar depan fasad hotel |
| P04 | Grand Hotel Preanger | Heritage dan arsitektur | Trotoar depan hotel, sekitar Jl. Tamblong |
| P05 | Titik Nol Kilometer Bandung | Landmark sejarah kota | Pedestrian sekitar penanda |
| P06 | Kantor Pos Besar Bandung | Heritage dan layanan publik | Trotoar depan, sisi barat Alun-Alun |
| P07 | Alun-Alun Bandung | Ruang publik dan istirahat | Akses pedestrian dari Jl. Asia Afrika |
| P08 | Masjid Raya Bandung | Landmark religi dan sejarah | Pedestrian sisi luar kompleks |
| P09 | Cikapundung Riverspot | Ruang publik dan spot foto | Titik akses dekat Jl. Asia Afrika |
| P10 | Situs Penjara Banceuy | Heritage dan perjuangan | Titik akses luar situs, Jl. Banceuy |

Gedung Merdeka dan Museum KAA adalah satu POI agar tidak terjadi pemicu ganda.

Tanpa izin lokasi, peta, pencarian, dan konten tetap harus bisa diakses secara manual.

## Screen/state

| ID | Screen | Requirement |
|---|---|---|
| UI01 | Explore Map: posisi pengguna dan POI sekitar | F01, F02 |
| UI02 | POI Trigger Notification | F02 |
| UI03 | POI Detail: informasi visual dan audio | F03, F04 |

Prototype memperlihatkan empat kondisi: di luar area, mendekati POI, terpicu, dan akses konten.

## Timeline

| Sprint | Minggu | Goal |
|---|---|---|
| S1 | W10 | Fondasi aplikasi dan location awareness |
| S2 | W11 | POI, logika lokasi, trigger |
| S3 | W12 | Integrasi core exploration, Major Progress Review |
| S4 | W13 | Fitur utama dan integrasi, Feature Freeze akhir W13 |
| S5 | W14 | Uji acceptance criteria, perbaikan, stabilisasi |

Output W15: produk jalan (APK/URL/QR), testing report, PRD compliance matrix, video maksimal 3 menit, AI & Tools Disclosure / Development Log.

Perubahan requirement atau backlog setelah baseline W9 wajib dicatat di Change Log beserta alasannya.

Prinsip AI: AI boleh membantu, tetapi tim tetap bertanggung jawab memahami, memverifikasi, menguji, dan mendemokan setiap fitur.
