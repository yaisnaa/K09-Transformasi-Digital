# Change Log Requirement

Perubahan requirement atau backlog setelah baseline W9, beserta alasannya (lihat `PRD.md`, bagian Timeline).

| ID | Tanggal | Requirement | Sebelum | Sesudah | Alasan | Status |
|---|---|---|---|---|---|---|
| CL-01 | 2026-11-09 | F01 acceptance 2, PB04 | Marker posisi berpindah saat pengguna berjalan ≥10 m | Marker posisi berpindah saat pengguna berjalan 1-2 m | Agar pergerakan pengguna di peta terasa lebih responsif saat menyusuri koridor | Diputuskan anggota tim, perlu konfirmasi seluruh tim |
| CL-02 | 2026-11-21 | F07 acceptance 3, PB11 | Maksimal 3 rekomendasi, urut minat dan jarak | Maksimal 3 rekomendasi **dipilih** menurut minat lalu jarak; **urutan tampil dan kunjungan** dimulai dari POI terdekat dengan titik awal, lalu terdekat dari POI sebelumnya. Navigasi menjadi satu rute: titik awal → rekomendasi 1 → 2 → 3 | Rekomendasi dipakai sebagai rute jalan kaki; urutan terdekat-berikutnya mengurangi jalan bolak-balik | Diputuskan anggota tim, perlu konfirmasi seluruh tim |
| CL-03 | 2026-11-21 | Baru (di luar F01-F08) | Tidak ada akun; progres tersimpan per HP | Masuk/Daftar dengan akun lokal di HP; profil, riwayat, dan progres Jejak Sejarah tersimpan per akun; mode tamu tetap ada | Beberapa pengguna bisa memakai HP yang sama tanpa progresnya tercampur; demo progres per akun | Diputuskan anggota tim, perlu konfirmasi seluruh tim; belum diimplementasikan |

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
- **Mode tamu:** tetap ada ("Lanjut tanpa akun"); saat tamu mendaftar, progresnya dipindahkan ke akun baru.
- **Status pengujian:** belum diimplementasikan.
