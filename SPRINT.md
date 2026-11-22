# Rencana Sprint 1 sampai 5

Sprint berlangsung satu minggu. Status repo belum dikonfirmasi: cek dulu apakah project Android sudah ada sebelum membuat yang baru.

Catatan: di PRD asli, PB04 dan PB05 belum didefinisikan dan Sprint 2 hanya berisi PB06 padahal goal-nya trigger. Definisi PB04 dan PB05 di bawah adalah **usulan** dan perlu disetujui tim serta dicatat di Change Log.

## Sprint 1 (W10): fondasi aplikasi dan location awareness

### PB01: Setup project (Must)
- Project Kotlin yang berjalan di perangkat uji
- Google Maps API key dibaca dari `local.properties`
- Selesai jika: aplikasi berhasil dibuka tanpa crash

### PB02: Izin lokasi dan pembacaan posisi (Must, F01)
- Meminta izin lokasi, membaca posisi lewat Fused Location Provider
- Menangani izin ditolak dan GPS mati
- Selesai jika: lokasi terbaca di perangkat uji; izin ditolak atau GPS mati menampilkan pesan dan tombol untuk mengaktifkan lokasi, tanpa crash

### PB03: Dataset 10 POI (Must, F01, F02)
- File `assets/poi.json` berisi P01-P10 dari `PRD.md`
- Field minimal: `id`, `nama`, `kategori`, `lat`, `lng`, `verified`, `ringkasan`, `foto`, `infografis`, `audio`, `sumber`, `validated`
- Koordinat belum ada di PRD. Jangan mengarang: minta dari tim, atau beri placeholder dengan `verified=false`
- Selesai jika: data POI dapat dibaca aplikasi

### PB04: Explore Map (Must, F01, usulan)
- Google Map berisi marker posisi pengguna dan seluruh POI
- Selesai jika: marker pengguna muncul ≤10 detik setelah izin diberikan, dan berpindah saat pengguna berjalan 1-2 m (lihat `CHANGELOG.md` CL-01)

**Goal S1:** aplikasi berjalan, lokasi terbaca, seluruh POI tampil di peta.

## Sprint 2 (W11): POI, logika lokasi, dan trigger

### PB05: Location-based trigger (Must, F02, usulan)
Urutan pengerjaan:
1. `TriggerEngine` Kotlin murni beserta unit test
2. Sambungkan ke update lokasi saat aplikasi aktif, tampilkan kartu POI
3. Foreground service lokasi dan notifikasi saat aplikasi di latar
4. Simpan riwayat "POI yang dilewati" secara lokal

Selesai jika memenuhi acceptance F02:
- Kartu atau notifikasi muncul ≤10 detik saat masuk radius
- POI tidak terpicu ulang selama pengguna tetap di area
- Pada radius tumpang tindih, hanya POI terdekat yang dipicu
- POI yang terpicu di latar tercatat di "POI yang dilewati"

Skenario unit test minimum: masuk radius, pembacaan GPS tunggal yang meleset, data tidak valid, tetap di area, keluar lalu masuk lagi, radius tumpang tindih.

### PB06: POI Detail, visual dan audio (Must, F03, F04)
Urutan pengerjaan:
1. Halaman POI Detail: teks, foto, infografis, sumber
2. Narasi audio Media3 dengan satu tombol
3. Audio tetap berjalan di latar dan bisa dikontrol dari notifikasi media atau lock screen
4. Opsi autoplay saat POI terpicu

Selesai jika memenuhi acceptance F03 dan F04 di `PRD.md`.

**Goal S2:** pengguna mendekati POI, menerima kartu atau notifikasi, lalu membuka informasi visual dan audio.

## Sprint 3-5: sesuai slide roadmap tim

Sumber: slide "Rencana Eksekusi Sprint 3 - Sprint 5" (Next Steps & Roadmap menuju Major Progress Review dan Final Release), menggantikan usulan S3 tanggal 2026-11-10. PB07-PB14 tidak ada di PRD asli; catat di Change Log. Kriteria "Selesai jika" diambil dari acceptance criteria di `PRD.md`.

## Sprint 3 (W12): Core Exploration & Review

### PB07: Penyelesaian konten dan uji awal di HP fisik (Must, F01-F04, N01)
- Koordinat P06-P10
- Konten (ringkasan, infografis, sumber) untuk P02
- Foto P05, P09, P10
- 10 file audio narasi asli, menggantikan `audio/placeholder.wav`
- Uji awal di HP Android fisik: izin, notifikasi latar, foreground service, audio saat layar terkunci
- N01: tim memeriksa konten lalu menandai `validated=true`. Konten dummy tetap `validated=false`; penyimpangan dari N01 (konten belum tervalidasi ikut tampil dengan label) dicatat di Change Log
- Konfirmasi tim untuk CL-01 dan keputusan radius tumpang tindih, lalu rapikan `DECISIONS.md`
- Selesai jika: semua POI punya koordinat dan konten lengkap (asli atau dummy yang ditandai), dan acceptance F01-F04 sudah dicoba di HP fisik

### PB08: Search & Destination (Must, F05)
- Cari POI dengan kata kunci parsial
- Filter kategori: Heritage, Spot Foto, Tempat Istirahat/Kuliner
- Hasil diurutkan dari terdekat, menampilkan jarak (m) dan estimasi waktu jalan kaki (menit)
- Hasil membuka POI Detail dengan tombol "Navigasi"; hasil kosong menampilkan "tidak ditemukan"
- Akses manual tanpa izin lokasi (PRD Bab POI): ketuk marker POI di peta membuka POI Detail; pencarian tetap bisa dipakai tanpa lokasi (urutan jarak tidak ditampilkan)
- Selesai jika memenuhi acceptance F05 no. 1-4, setiap POI bisa dibuka dari peta dalam ≤2 tap tanpa izin lokasi, dan logika pencarian serta pengurutan punya unit test

### PB09: Walking Navigation (Must, F06)
- Garis rute jalan kaki di peta menuju POI tujuan
- Sisa jarak dan waktu diperbarui saat pengguna mendekat
- Status "Tiba di tujuan" saat masuk radius POI tujuan
- Rerouting saat pengguna menyimpang lebih dari 30 m
- Setelah F06 ada, POI tujuan navigasi aktif diprioritaskan saat radius tumpang tindih (`DECISIONS.md`, Bab 4.2)
- Selesai jika memenuhi acceptance F06 no. 1-4 (rute tampil ≤5 detik setelah tujuan dipilih)
- Perlu keputusan sebelum mulai: Directions/Routes API Google berbayar di atas kuota gratis. Ambang menyimpang 30 m menjadi parameter di `TriggerConfig.kt`

### PB10: Implementasi desain high fidelity (Must)
- Desain high fidelity diterapkan ke semua layar yang sudah ada
- Ikon aplikasi dan ikon notifikasi diganti dari bawaan template
- Layar onboarding (alur inisiasi PRD): penjelasan singkat sebelum meminta izin lokasi dan notifikasi
- State "mendekati POI" sehingga keempat kondisi prototype PRD tampil: di luar area, mendekati, terpicu, akses konten. Jarak "mendekati" adalah parameter baru: masukkan ke `TriggerConfig.kt` dan catat di `DECISIONS.md`
- Sumber desain (link Figma atau ekspor gambar per layar) disediakan tim sebelum pengerjaan; kalau onboarding atau state "mendekati" belum ada di desain, tim menentukan gayanya
- Selesai jika: setiap layar sesuai desain saat dibandingkan berdampingan, keempat state dapat didemokan, dan acceptance F01-F06 tetap lolos

### Persiapan Major Progress Review (akhir W12)
- Naskah demo 3-5 menit: buka aplikasi, berjalan, kartu atau notifikasi, detail, audio, pencarian, navigasi
- Draf PRD compliance matrix F01-F06 (Pass, Fail, atau belum teruji) beserta buktinya; "Pass" hanya untuk yang benar-benar teruji
- Rangkuman Development Log dan AI & Tools Disclosure S1-S3

**Goal S3:** eksplorasi inti (peta, trigger, konten, pencarian, navigasi) berjalan dengan tampilan high fidelity dan siap didemokan di Major Progress Review.

## Sprint 4 (W13): Personalization & Gamification

### PB11: User profiling dan Contextual Recommendation (Must, F07)
- Kuesioner profil pengguna: durasi kunjungan dan minat
- Mesin rekomendasi: rute awal dan maksimal 3 POI berikutnya
- Selesai jika memenuhi acceptance F07: total estimasi rute ≤ durasi pilihan pengguna, tidak merekomendasikan POI yang sudah dikunjungi, maksimal 3 rekomendasi diurutkan berdasarkan minat dan jarak, dan dua profil berbeda menghasilkan rekomendasi berbeda
- Logika rekomendasi ditulis Kotlin murni dan punya unit test

### PB12: Gamifikasi "Jejak Sejarah" (Must, F08)
- Check-in wajib di dalam radius 25 m POI
- Kuis sejarah per POI, poin, dan badge
- Progres, poin, dan badge tersimpan di perangkat
- Selesai jika memenuhi acceptance F08: check-in hanya berhasil di dalam radius, progres misi (x/N POI) naik setelah check-in, jawaban benar menambah poin, semua misi selesai membuka badge, dan progres tetap ada setelah aplikasi ditutup
- Soal kuis disusun dari sumber yang tercatat (N01); soal dummy boleh dipakai sementara asal ditandai

### PB13: Dukungan dwibahasa ID/EN (Should, A01)
- Seluruh teks antarmuka dan konten POI tersedia dalam bahasa Indonesia dan Inggris
- Selesai jika memenuhi acceptance A01: ganti bahasa mengubah seluruh teks tanpa restart, dan pilihan bahasa tetap tersimpan

**Feature Freeze di akhir W13:** setelah ini tidak ada fitur baru, hanya perbaikan bug dan kalibrasi.

## Sprint 5 (W14-W15): Field Testing & Deliverables

### PB14: Uji lapangan F01-F08 (Must)
- Uji jalan kaki langsung di koridor Jalan Asia Afrika dengan HP Android asli
- Stabilisasi dan retest: kalibrasi final parameter radius dan akurasi di `TriggerConfig.kt`, dicatat di Change Log
- Perbaikan bug khusus merek HP (OEM), misalnya service latar yang dimatikan sistem
- Selesai jika: setiap acceptance F01-F08 punya hasil uji lapangan (Pass atau Fail) beserta buktinya

### Deliverables W15
- APK final
- Testing report
- PRD Compliance Matrix (Bab 11)
- Video demo maksimal 3 menit
- AI & Tools Disclosure / Development Log

**Goal S5:** aplikasi stabil di HP asli dan seluruh deliverables W15 terkumpul.

## Harus dilakukan manusia

- Menyediakan koordinat, konten, foto, dan audio POI yang terverifikasi
- Memutuskan biaya API navigasi (F06) sebelum PB09
- Menyediakan desain high fidelity (link Figma atau ekspor gambar per layar)
- Menyusun soal kuis F08 dan teks terjemahan A01, beserta sumbernya
- Menguji di HP asli dan uji lapangan di Jalan Asia Afrika, lalu mencatat hasil kalibrasi di Change Log
- Mempresentasikan Major Progress Review dan merekam video demo
