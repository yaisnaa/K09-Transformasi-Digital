# Keputusan Teknis dan Konflik PRD

Status: **usulan, belum disetujui tim**, kecuali ditandai lain. Implementasi memakai nilai di sini agar konsisten, dan semua parameter harus mudah diubah. Setelah disetujui, perbarui PRD dan catat di Change Log.

## Parameter trigger

Letakkan seluruhnya di satu file konstanta.

| Parameter | Nilai | Alasan |
|---|---|---|
| Radius masuk | 25 m | Default F02; akurasi GPS luar ruang di koridor umumnya 5-20 m |
| Radius re-arm | >40 m | Acceptance F02; lebih besar dari radius masuk sehingga tidak bolak-balik terpicu |
| Konfirmasi masuk | 2 pembacaan valid berturut-turut di dalam radius | Mengurangi pemicu palsu akibat GPS meleset |
| Interval GPS saat trigger aktif | 3 detik | Waktu terburuk konfirmasi sekitar 6 detik, menyisakan ruang untuk syarat ≤10 detik |
| Akurasi maksimum | 30 m | Pembacaan lebih buruk dianggap tidak valid (usulan awal, sesuaikan saat uji lapangan) |
| Umur data maksimum | 15 detik | Pembacaan lebih lama dianggap tidak valid (usulan awal) |

## Logika pemicu

```
untuk setiap pembacaan lokasi:
  jika tidak valid atau kedaluwarsa: abaikan, tunggu pembaruan
  hitung jarak ke semua POI
  POI yang sudah terpicu dan kini berjarak >40 m: aktifkan kembali
  ambil POI terdekat dalam radius 25 m yang belum terpicu
  jika sudah terkonfirmasi 2 pembacaan berturut-turut:
    tandai terpicu dan catat riwayat
    aplikasi aktif: tampilkan kartu POI
    aplikasi di latar + izin notifikasi ada: kirim notifikasi dan catat di "POI yang dilewati"
    aplikasi di latar + tanpa izin notifikasi: simpan, tampilkan saat aplikasi dibuka
```

Hanya POI terdekat yang dipicu. Prioritas "tujuan navigasi aktif" dari Bab 4.2 baru relevan setelah F06 ada.

## Konflik PRD yang diselesaikan

| Topik | Konflik | Usulan |
|---|---|---|
| Radius | F02: 25 m. Bab 4.1, 4.2, deck: 50 m | 25 m, final setelah uji lapangan |
| Trigger ulang | F02: aktif lagi setelah keluar >40 m. Bab 4.2: 1× per sesi | Ikuti F02 karena F01-F08 adalah baseline penilaian |
| Waktu pemicu | F02: ≤10 detik. Bab 4.2: minimal 10 detik di radius | Ganti dwell 10 detik dengan konfirmasi 2 pembacaan |
| Autoplay | F04: autoplay opsional. Bab 4.2: konten dibuka atas pilihan pengguna | Autoplay default mati, pengguna yang mengaktifkan |

## Contoh template yang tidak boleh dianggap fakta

Jangan menjadikan ini sebagai progres atau requirement:
- Change Log v1.1: radius 5 m ke 10 m karena GPS indoor (NG03 menyatakan outdoor saja)
- Final Compliance: F01 dan F02 sudah Pass, TC-01 sampai TC-03
- Major Progress Review: PB04 dan PB05 Completed
- UI02 dan UI03 yang masih menyebut "koleksi"
- Nama "Guide to Jalan Asia Afrika" pada flowchart; nama produk adalah HistoryGo

## Keputusan teknis

### Tidak memakai Geofencing API bawaan Android
Panduan Android menyarankan radius minimal 100-150 m, dan alert-nya biasanya tertunda sampai sekitar 2 menit. Keduanya tidak cocok dengan radius 25 m dan syarat ≤10 detik.

Gunakan **Fused Location Provider di foreground service, lalu hitung jarak sendiri**.

### Foreground service lokasi
- Deklarasikan `foregroundServiceType="location"`, `FOREGROUND_SERVICE`, dan `FOREGROUND_SERVICE_LOCATION`
- Mulai service saat aplikasi terlihat di layar, dan setelah izin lokasi diberikan
- Dengan cara ini `ACCESS_BACKGROUND_LOCATION` tidak diperlukan; jangan ditambahkan tanpa alasan
- Minta `POST_NOTIFICATIONS` saat runtime untuk Android 13 ke atas
- Tangani izin notifikasi yang ditolak sesuai logika di atas

### Kotlin, bukan Expo
Status: dipilih dalam diskusi. Sesuai PRD, target hanya Android, dan bagian paling berisiko bersifat native.

### iOS: belum masuk scope, tetapi arsitektur disiapkan
Status: dipilih dalam diskusi (Opsi A), belum dikonfirmasi tim. Target tetap Android saja sesuai baseline, jadi tidak perlu Change Log. iOS butuh Mac dan Xcode, dan mekanisme lokasi latar di iOS berbeda total, sehingga terlalu berisiko untuk tenggat W15.

Agar iOS bisa ditambahkan nanti (misalnya lewat Kotlin Multiplatform), kode di `trigger/` dan model di `data/` ditulis sebagai Kotlin murni: tanpa import `android.*`, tanpa `Context`, dan tanpa class `Location` milik Android. Kode khusus Android ditempatkan di `location/`, `audio/`, dan `ui/`.

### Jetpack Compose untuk UI
PRD tidak menentukan. Compose diusulkan agar perubahan state (di luar radius, mendekat, terpicu, akses konten) sederhana dibuat.

## Pertanyaan terbuka

- Apakah repo sudah ada?
- Siapa yang menyediakan koordinat, foto, infografis, audio, dan sumber untuk 10 POI?
- Apakah HP uji tim menjalankan Android 13/14 ke atas?
