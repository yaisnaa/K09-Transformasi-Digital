# Change Log Requirement

Perubahan requirement atau backlog setelah baseline W9, beserta alasannya (lihat `PRD.md`, bagian Timeline).

| ID | Tanggal | Requirement | Sebelum | Sesudah | Alasan | Status |
|---|---|---|---|---|---|---|
| CL-01 | 2026-11-09 | F01 acceptance 2, PB04 | Marker posisi berpindah saat pengguna berjalan ≥10 m | Marker posisi berpindah saat pengguna berjalan 1-2 m | Agar pergerakan pengguna di peta terasa lebih responsif saat menyusuri koridor | Diputuskan anggota tim, perlu konfirmasi seluruh tim |

## Catatan CL-01

- **Dampak ke kode:** tidak ada. Marker sudah diperbarui pada setiap bacaan GPS tanpa batas jarak minimum. Bacaan datang setiap 3 detik (`TriggerConfig.LOCATION_INTERVAL_MS`), sehingga pada kecepatan jalan kaki (sekitar 1,4 m/s) marker bergeser sekitar 4 m per pembaruan.
- **Risiko pengujian:** akurasi GPS HP di luar ruang umumnya 5-20 m (`DECISIONS.md`). Pergeseran 1-2 m bisa tertutup oleh goyangan GPS, dan marker dapat bergeser walau pengguna diam. Kriteria ini dapat ditunjukkan di emulator (lokasi diatur manual), tetapi di HP asli **belum teruji** dan hasilnya mungkin tidak konsisten.
