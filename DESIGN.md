---
name: HistoryGo
description: Panduan wisata berbasis lokasi untuk koridor cagar budaya Jalan Asia Afrika, Bandung.
colors:
  kertas: "#F7EFE3"
  kartu: "#FFFBF4"
  krem-tua: "#EFE2CC"
  garis: "#E2D3BC"
  garis-tegas: "#8A7660"
  tinta-sepia: "#2B1A0E"
  bronze: "#6E5A48"
  chestnut: "#4A2C14"
  kuningan-tua: "#7F5B1C"
  emas-deco: "#B8893B"
  teal-lokasi: "#1F5A5B"
  teal-muda: "#DCE9E6"
  terakota: "#8C3520"
  terakota-muda: "#F6DCCF"
  peta-latar: "#F2E7D3"
  gelap-latar: "#17110B"
  gelap-kartu: "#241B13"
  gelap-krem: "#33281C"
  gelap-garis: "#43362A"
  gelap-tinta: "#F3E8D6"
  gelap-teks-kedua: "#C4B39C"
  gelap-primer: "#E3BC78"
  gelap-label-emas: "#D9B26C"
  gelap-teal: "#6FB3AE"
  gelap-peringatan: "#F2A48D"
  gelap-latar-peringatan: "#3E2119"
typography:
  display:
    fontFamily: "DM Serif Display, Georgia, serif"
    fontSize: "36sp"
    fontWeight: 400
    lineHeight: 1.08
    letterSpacing: "-0.01em"
  headline-md:
    fontFamily: "DM Serif Display, Georgia, serif"
    fontSize: "28sp"
    fontWeight: 400
    lineHeight: 1.1
  headline-sm:
    fontFamily: "DM Serif Display, Georgia, serif"
    fontSize: "24sp"
    fontWeight: 400
    lineHeight: 1.12
  title:
    fontFamily: "Inter, sans-serif"
    fontSize: "16sp"
    fontWeight: 600
    lineHeight: 1.35
  body-lg:
    fontFamily: "Inter, sans-serif"
    fontSize: "16sp"
    fontWeight: 400
    lineHeight: 1.6
  body-md:
    fontFamily: "Inter, sans-serif"
    fontSize: "14sp"
    fontWeight: 400
    lineHeight: 1.45
  label:
    fontFamily: "Inter, sans-serif"
    fontSize: "13sp"
    fontWeight: 500
    lineHeight: 1.3
  angka:
    fontFamily: "IBM Plex Mono, monospace"
    fontSize: "14sp"
    fontWeight: 500
    lineHeight: 1.3
rounded:
  chip: "8dp"
  tombol: "16dp"
  kartu: "20dp"
  sheet-atas: "24dp"
  pil: "999dp"
spacing:
  xs: "4dp"
  sm: "8dp"
  md: "12dp"
  lg: "16dp"
  xl: "20dp"
  xxl: "24dp"
  section: "32dp"
components:
  button-primary:
    backgroundColor: "{colors.chestnut}"
    textColor: "{colors.kartu}"
    typography: "{typography.title}"
    rounded: "{rounded.tombol}"
    height: "56dp"
    padding: "0 20dp"
  button-primary-dark:
    backgroundColor: "{colors.gelap-primer}"
    textColor: "{colors.tinta-sepia}"
    rounded: "{rounded.tombol}"
    height: "56dp"
  button-outlined:
    backgroundColor: "transparent"
    textColor: "{colors.chestnut}"
    rounded: "{rounded.tombol}"
    height: "52dp"
  button-tonal:
    backgroundColor: "{colors.krem-tua}"
    textColor: "{colors.chestnut}"
    rounded: "{rounded.tombol}"
    height: "52dp"
  button-text:
    backgroundColor: "transparent"
    textColor: "{colors.kuningan-tua}"
    height: "48dp"
  chip-filter-selected:
    backgroundColor: "{colors.chestnut}"
    textColor: "{colors.kartu}"
    rounded: "10dp"
    height: "40dp"
  chip-filter:
    backgroundColor: "transparent"
    textColor: "{colors.chestnut}"
    rounded: "10dp"
    height: "40dp"
  chip-status-warning:
    backgroundColor: "{colors.terakota-muda}"
    textColor: "{colors.terakota}"
    typography: "{typography.label}"
    rounded: "{rounded.chip}"
    height: "28dp"
  chip-status-location:
    backgroundColor: "{colors.teal-muda}"
    textColor: "{colors.teal-lokasi}"
    typography: "{typography.label}"
    rounded: "{rounded.chip}"
    height: "28dp"
  card:
    backgroundColor: "{colors.kartu}"
    textColor: "{colors.tinta-sepia}"
    rounded: "{rounded.kartu}"
    padding: "16dp"
  narration-bar:
    backgroundColor: "{colors.chestnut}"
    textColor: "{colors.kartu}"
    rounded: "{rounded.kartu}"
    padding: "14dp 16dp"
  nav-bar:
    backgroundColor: "{colors.kartu}"
    textColor: "{colors.bronze}"
    height: "80dp"
  nav-bar-indicator:
    backgroundColor: "{colors.krem-tua}"
    textColor: "{colors.tinta-sepia}"
    rounded: "{rounded.pil}"
    size: "64dp x 32dp"
---

# Design System: HistoryGo

Sumber visual: kanvas desain "HistoryGo Baseline Design" (Baseline v3; link dibagikan di grup tim). Ekspor PNG per layar ada di `docs/design/`. Kalau kanvas dan file ini berbeda soal nilai token, file ini yang benar.

## Overview

**Creative North Star: "Jurnal Penjelajah Deco"**

HistoryGo terasa seperti buku catatan penjelajah yang dibawa menyusuri Jalan Asia Afrika: kertas krem, tinta sepia, dan aksen emas dari fasad Art Deco Gedung Merdeka. Nuansa sejarah hadir lewat material (warna kertas, serif klasik, ornamen garis dan belah ketupat), bukan lewat kostum (tekstur kertas palsu, sepia filter, ornamen berlebihan).

Aplikasi ini mode **Operate**: dipakai sambil berjalan, satu tangan, di bawah matahari. Kerangkanya Material 3 (navigation bar, kartu, chip, switch, snackbar); karakter brand hidup di warna, tipografi, dan detail kecil seperti marker belah ketupat. Kepadatan rendah, target sentuh besar, satu aksi utama per layar.

**Key Characteristics:**
- Kertas krem sebagai latar, kartu hampir putih, tinta sepia untuk teks.
- Serif display (DM Serif Display) untuk nama tempat dan judul; Inter untuk semua UI; IBM Plex Mono khusus angka.
- Belah ketupat sebagai motif tunggal: marker peta, progres misi, nomor rute, ornamen pembatas, badge.
- Teal hanya berarti lokasi; emas di tombol play berarti cerita.
- Tema terang sebagai default (pemakaian siang di trotoar); tema gelap lengkap untuk malam.

## Colors

Palet kertas tua yang hangat dengan dua aksen yang punya arti tetap: emas untuk cerita dan ornamen, teal untuk lokasi.

### Primary
- **Chestnut** (#4A2C14): tombol utama, latar bar narasi, chip filter terpilih, marker bangunan. Di tema gelap diganti **Emas Muda** (#E3BC78) dengan teks Tinta Sepia.

### Secondary
- **Teal Lokasi** (#1F5A5B): titik posisi pengguna, garis rute, header navigasi, chip "Mendeteksi bangunan". Latar mudanya **Teal Muda** (#DCE9E6). Gelap: #6FB3AE.

### Tertiary
- **Kuningan Tua** (#7F5B1C): teks emas (label infografis, tombol teks, jarak di kartu). Gelap: #D9B26C.
- **Emas Deco** (#B8893B): ornamen saja: garis pembatas, bingkai marker, lingkaran tombol play, radius 25 m. Tidak pernah untuk teks di atas latar terang (kontras kurang).

### Neutral
- **Kertas** (#F7EFE3): latar layar.
- **Kartu** (#FFFBF4): permukaan kartu, sheet, bar atas, navigation bar.
- **Krem Tua** (#EFE2CC): chip netral, tombol tonal, indikator tab aktif.
- **Garis** (#E2D3BC): hairline dan border kartu.
- **Garis Tegas** (#8A7660): outline komponen interaktif (chip belum terpilih, radio, switch mati).
- **Tinta Sepia** (#2B1A0E): teks utama.
- **Bronze** (#6E5A48): teks kedua, keterangan.
- **Terakota** (#8C3520) di atas **Terakota Muda** (#F6DCCF): label "Belum tervalidasi", "Konten contoh", error.

### Peran Material 3
| Peran | Terang | Gelap |
|---|---|---|
| primary / onPrimary | Chestnut / Kartu | #E3BC78 / Tinta Sepia |
| primaryContainer / onPrimaryContainer | Krem Tua / Chestnut | #33281C / #F3E8D6 |
| secondary / onSecondary | Teal Lokasi / Kartu | #6FB3AE / #17110B |
| secondaryContainer | Teal Muda | #1F3533 |
| tertiary | Kuningan Tua | #D9B26C |
| surface / background | Kertas | #17110B |
| surfaceContainer | Kartu | #241B13 |
| onSurface / onSurfaceVariant | Tinta Sepia / Bronze | #F3E8D6 / #C4B39C |
| outline / outlineVariant | Garis Tegas / Garis | #8A7660 / #43362A |
| error / errorContainer | Terakota / Terakota Muda | #F2A48D / #3E2119 |

Dynamic Color (Material You) **tidak** dipakai: palet brand harus konsisten di semua HP.

### Named Rules
**The Two Meanings Rule.** Teal hanya untuk "di mana kamu / ke mana kamu pergi". Emas pada tombol play hanya untuk "dengarkan cerita". Jangan memakai keduanya untuk hal lain.

**The Gold Is Not Ink Rule.** Emas Deco (#B8893B) tidak pernah dipakai sebagai warna teks di latar terang. Untuk teks emas pakai Kuningan Tua (#7F5B1C).

## Typography

**Display Font:** DM Serif Display (fallback Georgia, serif)
**Body Font:** Inter (fallback sans-serif)
**Numeric Font:** IBM Plex Mono

**Character:** Serif display memberi suara "buku sejarah" pada nama tempat; Inter menjaga UI tetap terbaca cepat di luar ruangan. Mono hanya untuk angka sehingga jarak, waktu, dan poin bisa dipindai sekilas.

Semua font dibundel di `res/font/` (lisensi SIL Open Font License dari Google Fonts). Ukuran memakai **sp** supaya mengikuti pengaturan ukuran huruf sistem.

### Hierarchy
- **Display** (400, 36sp, 1.08): judul layar onboarding, judul POI Detail (32sp).
- **Headline M** (400, 28sp): judul layar utama (Rekomendasi untukmu, Jejak Sejarah, Pengaturan, 26–32sp).
- **Headline S** (400, 24sp): nama tempat di kartu dan daftar (20–24sp).
- **Title** (Inter 600, 16–18sp): judul bagian, label tombol, nama misi.
- **Body L** (Inter 400, 16sp, 1.6): teks sejarah, penjelasan.
- **Body M** (Inter 400, 14sp, 1.45): keterangan, kategori, status.
- **Label** (Inter 500, 13sp): chip status. Label infografis: Inter 600, 12sp, huruf kapital, tracking 0.08em.
- **Angka** (IBM Plex Mono 500, 14–32sp): jarak ("18 m"), waktu ("±14.22"), poin, "3/10".

### Named Rules
**The 12sp Floor Rule.** Tidak ada teks di bawah 12sp.

**The Numbers Only Rule.** Mono hanya untuk token angka. Kalimat di sekitarnya tetap Inter: "<mono>120 m</mono> · ±2 menit jalan".

**The No Eyebrow Rule.** Tidak ada label kecil di atas judul. Judul berdiri sendiri; informasi pendukung ditaruh di bawahnya.

## Layout

Layar ponsel 390×844dp sebagai acuan, edge-to-edge dengan window insets (status bar, navigation bar sistem, keyboard).

- **Peta** penuh layar. Di atasnya: bar atas (nama aplikasi, chip status, tombol Pengaturan 48dp) dan search bar, keduanya dengan margin 12dp. Di bawah: satu kartu/sheet di atas navigation bar (jarak 12dp), lalu navigation bar 80dp.
- **Layar daftar** (Rute, Jejak Sejarah, Riwayat): padding horizontal 20dp, judul 28dp dari atas, jarak antar-bagian 20–24dp.
- **Aksi utama tetap di bawah** saat konten bisa digulir (POI Detail, Rute, Kuesioner): bar 88–104dp berisi tombol 52–56dp.
- Jarak: grup rapat 8–12dp, pemisah antar-bagian 20–32dp, ruang di atas judul lebih besar dari di bawahnya.
- Pasangan jarak dan waktu tidak boleh terpotong baris.

## Elevation & Depth

Hampir datar. Kedalaman dari layering tonal (kertas → kartu) ditambah bayangan lembut hanya untuk elemen yang mengambang di atas peta.

### Shadow Vocabulary
- **Mengambang ringan** (`0 2px 8px rgba(43,26,14,0.08)`): bar atas dan search bar di atas peta.
- **Sheet di atas peta** (`0 8px 24px rgba(43,26,14,0.16)`): kartu POI, kartu navigasi, sheet status.
- **Label peta** (`0 1px 4px rgba(43,26,14,0.12)`): label nama tempat di samping marker.

Di Compose, pakai `shadowElevation` kecil (2dp dan 6dp) atau `Modifier.shadow` dengan warna bayangan sepia; jangan bayangan abu-abu bawaan yang tebal.

### Named Rules
**The Flat Page Rule.** Layar yang tidak di atas peta tidak memakai bayangan sama sekali; pemisahan memakai border Garis (#E2D3BC) dan beda tone.

## Shapes

Sudut membulat lembut, dipadukan dengan geometri tajam belah ketupat.

- Kartu dan sheet: 20dp. Sheet detail yang menimpa foto: 24dp di sudut atas.
- Tombol: 16dp. Chip status: 8dp. Chip filter: 10dp. Indikator tab dan switch: pil.
- **Belah ketupat** (persegi diputar 45°): marker bangunan 14dp berbingkai emas 2dp; marker terpicu 26dp emas dengan inti gelap 9dp; progres misi 16dp; nomor rute 30dp; ornamen pembatas 7–8dp.
- **Lengkung Deco**: bingkai foto onboarding (radius atas 180dp, bawah 16dp, border emas 2dp) dan ikon aplikasi.
- Titik posisi pengguna: lingkaran 18dp, border putih 3dp, halo teal transparan.

## Components

### Buttons
- **Primary** (Chestnut, teks Kartu, 56dp, sudut 16dp): satu per layar. "Dengarkan cerita" memakai lingkaran emas 34dp berisi ikon play gelap di kiri label.
- **Outlined** (border Chestnut 1.5dp, 52dp): aksi kedua ("Lihat detail", "Akhiri navigasi", "Arahkan ke sana").
- **Tonal** (Krem Tua, teks Chestnut): aksi kontekstual ("Check-in +5 poin").
- **Text** (Kuningan Tua, 48dp): aksi ringan ("Lewati", "Ubah", "Jelajah tanpa lokasi", "Hapus riwayat").
- **Nonaktif**: latar Garis (#E2D3BC), teks Bronze. Dipakai untuk "Jawab" sebelum memilih dan "Lihat rekomendasi" tanpa minat.
- Label tombol tidak terpotong baris.

### Chips
- **Filter**: terpilih = Chestnut + ikon centang + teks Kartu; belum terpilih = border Garis Tegas + teks Chestnut. Tinggi 40dp.
- **Status**: "Belum tervalidasi" dan "Konten contoh" (Terakota di Terakota Muda); "Audio contoh" (di bar narasi: #6B4A2C, teks Kartu); "Mendeteksi bangunan" (Teal di Teal Muda, titik 6dp); "Lokasi mati" (Terakota + ikon lokasi dicoret).

### Cards / Containers
- **Kartu**: latar Kartu, border 1dp Garis, sudut 20dp, padding 16–18dp.
- **Kartu misi**: border 1.5dp Emas Deco, ikon belah ketupat emas sebelum judul.
- **Plakat infografis**: grid 2 kolom dengan garis 1dp; label huruf kapital Kuningan Tua, nilai serif 30sp (tahun, gaya) atau Inter 15sp (arsitek, peran).

### Inputs
- **Search**: tinggi 52dp, latar Kartu, border Garis, sudut 16dp, ikon kaca pembesar Chestnut, placeholder Bronze. Fokus: outline 2dp Chestnut.
- **Radio kuis**: baris 52dp+, sudut 14dp; terpilih = border 2dp Chestnut, latar Kertas, titik Chestnut.
- **Checkbox minat**: kotak 22dp sudut 6dp; terpilih = isi Chestnut + centang Kartu.
- **Switch** (M3): nyala = track Chestnut, thumb #E3BC78 24dp; mati = track Krem Tua, outline Garis Tegas, thumb 16dp.
- **Segmented bahasa**: pil 48dp, segmen terpilih Chestnut.

### Navigation
- **Navigation bar** (M3, 80dp, latar Kartu, garis atas Garis): 4 tujuan dengan ikon garis 24dp + label 12sp: Jelajah, Rute, Jejak Sejarah, Riwayat. Aktif = indikator pil 64×32dp Krem Tua, label 600 Tinta Sepia. Pengaturan dibuka dari ikon di bar atas peta, bukan tab.
- **Top app bar** (layar turunan: Pengaturan, Cari, detail digulir): 64dp, tombol kembali 48dp, judul serif.
- Tombol Back sistem dan gesture back selalu berfungsi.

### Peta (Google Maps)
- Map style mengikuti palet: latar #F2E7D3, jalan #FFFBF4, taman #E3E0C2, sungai #C9D7D2, label jalan #7A6550; ikon bisnis Google disembunyikan. Versi gelap: latar #1D1610, jalan #2E241A, taman #252A1C, sungai #1F3533, label #A8957D.
- Font label peta milik Google dan tidak bisa diganti.
- Marker, titik posisi, radius 25 m, dan garis rute (teal 6dp dengan casing Kartu 11dp) digambar app.

### Signature: Kartu POI terpicu
Thumbnail 76dp (sudut 12dp), nama tempat Headline S, baris meta (jarak mono Kuningan Tua · kategori · chip "Belum tervalidasi"), lalu dua tombol: **Dengarkan cerita** (primary) dan **Lihat detail** (outlined). Tombol tutup 48dp di pojok kanan atas. Tanpa ringkasan teks.

### Signature: Bar narasi
Latar Chestnut, tombol play emas 56dp, judul "Dengarkan cerita", progres 4dp (track Kartu 25%, isi Emas Deco), baris bawah berisi chip "Audio contoh" (selama audio masih placeholder) dan "Tetap berputar saat layar terkunci". Saat berputar: ikon jeda, progres terisi.

### Katalog state
Setiap state di kode memakai pola berikut. Detail visualnya ada di artboard "State" pada kanvas.

| State | Pola |
|---|---|
| Lokasi belum terbaca / mencari | Chip status "Mencari lokasi…" (Krem Tua), peta tetap tampil tanpa titik posisi |
| GPS mati | Sheet seperti izin ditolak, judul "GPS sedang mati", tombol "Aktifkan GPS" |
| Izin lokasi ditolak | Sheet "Lokasimu belum terbaca" + "Izinkan lokasi" + "Jelajah tanpa lokasi" |
| `MAPS_API_KEY` kosong | Kartu peringatan Terakota di area peta (khusus pengembang) |
| POI tanpa koordinat | Baris info Bronze di sheet peta; tombol Navigasi nonaktif berlabel "Navigasi (koordinat belum ada)" |
| Foto memuat / tidak tersedia | Kotak Krem Tua dengan ikon foto garis dan teks Bronze |
| Audio / sumber belum ada | Bar narasi nonaktif (play Garis Tegas) + teks "Audio belum tersedia." |
| Konten dummy | Chip "Konten contoh" di kartu, dan kotak Terakota Muda "Konten contoh (dummy), bukan data sejarah asli." di detail |
| Navigasi: menghitung / menunggu lokasi / menghitung ulang | Kartu navigasi dengan indikator progres linear teal dan teks status |
| Navigasi: tiba | Kartu dengan belah ketupat emas besar, "Kamu sudah sampai di …", tombol "Selesai" |
| Navigasi beberapa titik | Header "Rute rekomendasi · 1 dari 3" + 3 belah ketupat kecil sebagai progres |
| Check-in terlalu jauh / lokasi belum terbaca | Teks Bronze + tombol Check-in nonaktif |
| Jawaban salah | Kotak Terakota Muda "Kurang tepat. Jawabannya: …" |
| Misi selesai | Baris teal dengan centang "Misi selesai · kuis dijawab benar/salah" |
| Badge terbuka | Emblem belah ketupat bertingkat berwarna emas penuh + "Badge terbuka" |
| Daftar kosong | Belah ketupat garis putus-putus emas + judul + satu kalimat langkah berikutnya |
| Hapus riwayat | Snackbar gelap "Riwayat dihapus" + "Urungkan" |

## Do's and Don'ts

### Do:
- **Do** pakai satu aksi primary per layar; aksi lain outlined, tonal, atau text.
- **Do** tampilkan "Belum tervalidasi" dan "Konten contoh" setiap kali datanya `validated=false` atau `dummy=true` (aturan kerja proyek no. 2 dan N01).
- **Do** jaga target sentuh minimal 48dp dan teks minimal 12sp.
- **Do** taruh aksi utama layar yang bisa digulir di bar bawah yang tetap.
- **Do** pakai belah ketupat untuk penanda dan progres.
- **Do** uji setiap layar di tema terang dan gelap, serta font scale 1.3.

### Don't:
- **Don't** pakai label kecil di atas judul (eyebrow).
- **Don't** pakai mono untuk kalimat; hanya angka.
- **Don't** pakai teal atau emas di luar arti tetapnya (lokasi dan cerita).
- **Don't** pakai gradien, tekstur kertas palsu, filter sepia pada foto, atau emoji sebagai ikon.
- **Don't** pakai Dynamic Color; palet brand tetap.
- **Don't** pakai istilah internal tim di UI ("koordinat belum dicek lapangan", "POI" pada judul untuk wisatawan).
