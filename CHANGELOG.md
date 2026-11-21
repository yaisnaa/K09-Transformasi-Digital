# Change Log Requirement

Perubahan requirement atau backlog setelah baseline W9, beserta alasannya (lihat `PRD.md`, bagian Timeline).

| ID | Tanggal | Requirement | Sebelum | Sesudah | Alasan | Status |
|---|---|---|---|---|---|---|
| CL-01 | 2026-11-09 | F01 acceptance 2, PB04 | Marker posisi berpindah saat pengguna berjalan ≥10 m | Marker posisi berpindah saat pengguna berjalan 1-2 m | Agar pergerakan pengguna di peta terasa lebih responsif saat menyusuri koridor | Diputuskan anggota tim, perlu konfirmasi seluruh tim |
| CL-02 | 2026-11-21 | F07 acceptance 3, PB11 | Maksimal 3 rekomendasi, urut minat dan jarak | Maksimal 3 rekomendasi **dipilih** menurut minat lalu jarak; **urutan tampil dan kunjungan** dimulai dari POI terdekat dengan titik awal, lalu terdekat dari POI sebelumnya. Navigasi menjadi satu rute: titik awal → rekomendasi 1 → 2 → 3 | Rekomendasi dipakai sebagai rute jalan kaki; urutan terdekat-berikutnya mengurangi jalan bolak-balik | Diputuskan anggota tim, perlu konfirmasi seluruh tim |
| CL-03 | 2026-11-21 | Baru (di luar F01-F08) | Tidak ada akun; progres tersimpan per HP | Masuk/Daftar dengan akun lokal di HP; profil, riwayat, dan progres Jejak Sejarah tersimpan per akun; mode tamu tetap ada | Beberapa pengguna bisa memakai HP yang sama tanpa progresnya tercampur; demo progres per akun | Diimplementasikan di `feature/pb10-ui`, teruji di emulator, perlu konfirmasi seluruh tim |
| CL-04 | 2026-11-21 | PB10, alur PRD no. 1 | Izin lokasi diminta otomatis saat peta pertama kali dibuka | Izin lokasi dan notifikasi diminta di layar onboarding setelah dijelaskan; "Jelajah tanpa lokasi" langsung ke peta tanpa dialog | Pengguna memahami alasan izin sebelum dialog sistem; tetap bisa memakai aplikasi tanpa lokasi | Diimplementasikan di `feature/pb10-ui`, teruji di emulator, perlu konfirmasi seluruh tim |
| CL-05 | 2026-11-21 | Baru (di luar F01-F08), PB10 | Deteksi hanya bisa dihentikan lewat tombol "Berhenti" di notifikasi permanen | Toggle "Deteksi bangunan sekitar" di layar Pengaturan menghentikan atau menjalankan pembacaan GPS dan notifikasi | Kendali pengguna atas lokasi dan baterai (hasil critique desain) | Diimplementasikan di `feature/pb10-ui`, teruji di emulator, perlu konfirmasi seluruh tim |
| CL-06 | 2026-11-21 | PB10, state prototype PRD | Tidak ada state "mendekati POI" | Pita "Mendekati …" saat POI terdekat berjarak 25-100 m; hanya tampilan, tidak memicu kartu atau notifikasi | Keempat state prototype PRD (di luar area, mendekati, terpicu, akses konten) dapat didemokan | Diimplementasikan di `feature/pb10-ui` dengan unit test, teruji di emulator; jarak 100 m perlu konfirmasi seluruh tim dan kalibrasi lapangan |

## Catatan CL-01

- **Dampak ke kode:** tidak ada. Marker sudah diperbarui pada setiap bacaan GPS tanpa batas jarak minimum. Bacaan datang setiap 3 detik (`TriggerConfig.LOCATION_INTERVAL_MS`), sehingga pada kecepatan jalan kaki (sekitar 1,4 m/s) marker bergeser sekitar 4 m per pembaruan.
- **Risiko pengujian:** akurasi GPS HP di luar ruang umumnya 5-20 m (`DECISIONS.md`). Pergeseran 1-2 m bisa tertutup oleh goyangan GPS, dan marker dapat bergeser walau pengguna diam. Kriteria ini dapat ditunjukkan di emulator (lokasi diatur manual), tetapi di HP asli **belum teruji** dan hasilnya mungkin tidak konsisten.

## Catatan CL-02

- **Yang tetap:** F07 no. 1 (total estimasi ≤ durasi), no. 2 (POI yang sudah dilewati tidak direkomendasikan), dan no. 4 (profil berbeda menghasilkan rekomendasi berbeda). Pemilihan 3 POI masih mendahulukan minat, lalu jarak.
- **Yang berubah:** urutan kunjungan disusun ulang menjadi terdekat-berikutnya dari titik awal. Kalau urutan baru melebihi durasi pilihan pengguna, urutan pemilihan semula yang dipakai.
- **Dampak:** POI pengisi yang tidak sesuai minat (dipakai saat POI sesuai minat tidak cukup) bisa tampil di urutan pertama kalau letaknya paling dekat. Label "Sesuai minatmu" / "Tempat terdekat yang masih muat" tetap ditampilkan di setiap rekomendasi.
- **Kode:** `PoiRecommender.orderByNearest`, `NavigationEngine` (beberapa titik), `RoutesClient` (titik singgah `intermediates`), `NavigationManager.startRoute`. Unit test: `urutanKunjungan_dimulaiDariYangTerdekatDenganTitikAwal`, `beberapaTitik_berlanjutOtomatisSampaiTitikTerakhir`.
- **Status pengujian:** teruji di emulator dengan Google Routes API (2026-11-21); **belum teruji di HP asli**.

## Catatan CL-03

- **Pilihan:** akun lokal (opsi A), bukan Firebase. Tanpa server, tanpa biaya, berjalan offline; sesuai `CLAUDE.md` ("Firebase hanya kalau benar-benar dibutuhkan"). Konsekuensi: akun dan progres tidak tersinkron antar-HP.
- **Keamanan:** password tidak pernah disimpan polos; disimpan sebagai hash PBKDF2 dengan salt acak per akun.
- **Data per akun:** profil kunjungan (F07), riwayat tempat yang dilewati (F02 no. 4), progres Jejak Sejarah (F08). Bahasa, autoplay, dan deteksi tetap pengaturan per HP.
- **Mode tamu:** tetap ada ("Lanjut tanpa akun"); saat tamu mendaftar, progresnya **disalin** ke akun baru (data tamu tetap ada di HP).
- **Alur:** layar Masuk/Daftar muncul sekali setelah onboarding; dari Pengaturan bisa Masuk/Daftar atau Keluar (dengan konfirmasi). Akun tamu memakai nama penyimpanan lama sehingga progres yang sudah ada tidak hilang.
- **Kode:** `account/AccountRules.kt` (validasi + PBKDF2, Kotlin murni), `account/AccountStore.kt`, `data/ActiveAccount.kt`, `data/AccountNaming.kt`, `ui/AuthScreen.kt`; `ProfileStore`, `GameStore`, `VisitHistoryStore` menyimpan data per akun.
- **Unit test:** `AccountRulesTest` (6), `AccountNamingTest` (4).
- **Status pengujian:** teruji di emulator (2026-11-21): validasi password pendek, daftar, riwayat tercatat per akun, keluar ke mode tamu dengan riwayat terpisah, password salah ditolak, masuk lagi dan riwayat akun kembali. Belum teruji di HP asli.

## Catatan CL-04 sampai CL-06 (PB10)

- **Kode:** `ui/OnboardingScreen.kt`, `ui/SettingsScreen.kt`, `ui/LocationScreen.kt`, `data/SettingsStore.kt` (`onboardingDone`, `notificationAsked`, `detection`), `location/TriggerService.kt` (`start` mematuhi pengaturan deteksi, `stop` baru), `trigger/Approach.kt` + `TriggerConfig.APPROACH_DISTANCE_M`.
- **Unit test:** `ApproachTest` (7 skenario).
- **Status pengujian:** teruji di emulator dengan lokasi palsu (screenshot di `docs/design/impl/`); **belum teruji di HP asli**. Akurasi GPS nyata dapat membuat state "mendekati" muncul-hilang; kalibrasi saat uji lapangan (PB14).
- **Perubahan tampilan tanpa dampak requirement** (tidak dicatat sebagai CL tersendiri): rekomendasi menampilkan jam perkiraan tiba ("tiba ±14.07") alih-alih "selesai di menit ke-X"; judul riwayat menjadi "Tempat yang kamu lewati"; istilah "POI" di teks untuk wisatawan diganti "tempat".
