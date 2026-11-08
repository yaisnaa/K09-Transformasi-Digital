# Rencana Sprint 1 dan 2

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

## Di luar Sprint 1-2

Jangan dikerjakan kecuali diminta: profiling dan rekomendasi (F07), pencarian (F05), navigasi (F06), gamifikasi (F08), dukungan bahasa (A01).

Tombol atau state untuk "Check-in misi" boleh tampil sebagai placeholder, tetapi logikanya bukan bagian S1-S2.

## Harus dilakukan manusia

- Membuat Google Maps API key
- Menyediakan koordinat dan konten POI yang terverifikasi
- Menguji di HP asli: izin, notifikasi latar, audio saat layar terkunci
- Uji lapangan di Jalan Asia Afrika untuk memfinalkan radius, lalu mencatatnya di Change Log
