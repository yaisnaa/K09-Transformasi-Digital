# HistoryGo

Aplikasi Android panduan wisata berbasis lokasi untuk koridor cagar budaya Jalan Asia Afrika, Bandung.
Tugas besar II4011 Transformasi Digital, ITB, Kelompok 9.

Fokus saat ini: **Sprint 1 dan Sprint 2**.

## Dokumen konteks

Baca sesuai kebutuhan, jangan menebak isinya:

| File | Isi | Baca saat |
|---|---|---|
| `SPRINT.md` | Backlog S1-S2, urutan kerja, definisi selesai | Memulai atau memilih pekerjaan |
| `PRD.md` | Requirement F01-F08, acceptance criteria, POI, non-goals | Mengerjakan atau menguji fitur |
| `DECISIONS.md` | Keputusan teknis, konflik PRD, parameter trigger | Menyentuh lokasi, trigger, notifikasi, atau audio |

Kalau `PRD.md` dan `DECISIONS.md` berbeda soal trigger, ikuti `DECISIONS.md`.

## Stack

- Bahasa: Kotlin, di Android Studio, target Android saja
- Lokasi: Fused Location Provider
- Peta: Google Maps SDK
- Data: JSON lokal di `assets/` (Firebase hanya kalau nanti benar-benar dibutuhkan)
- Audio: Android Media3
- UI: Jetpack Compose (usulan, lihat `DECISIONS.md`)

Gunakan versi library stabil yang terbaru saat setup. Jangan menebak versi dari ingatan; cek dulu.

## Struktur kode yang dituju

```
data/      model POI, pembaca JSON, penyimpanan lokal
location/  pembacaan GPS dan foreground service
trigger/   logika pemicu POI (Kotlin murni, tanpa dependensi Android)
audio/     pemutar narasi berbasis Media3
ui/        layar: Explore Map, kartu POI, POI Detail
```

## Aturan kerja

1. Kerjakan hanya PB yang diminta. Jangan menambah fitur Sprint 3 ke atas tanpa diminta.
2. Jangan mengarang koordinat POI, sejarah, sumber rujukan, atau status "tervalidasi". Kalau datanya belum ada, pakai placeholder yang jelas dan tandai `verified=false` / `validated=false`.
3. Semua parameter trigger berada di satu file konstanta. Dilarang hardcode di tempat lain.
4. Jangan commit API key. Simpan di `local.properties` dan pastikan file itu masuk `.gitignore`.
5. Logika trigger wajib punya unit test.
6. Jangan menyatakan fitur lolos kalau hanya berhasil di-compile. Hal yang butuh HP asli atau uji lapangan harus disebut sebagai "belum teruji".
7. Jangan mengubah requirement diam-diam. Kalau implementasi harus menyimpang dari PRD, sampaikan agar bisa dicatat di Change Log.

## Cara menjelaskan

- Gunakan Bahasa Indonesia dan jangan mengasumsikan pengalaman Kotlin.
- Setiap anggota tim harus bisa menjelaskan, menguji, dan mendemokan fitur yang dibuat, jadi jelaskan singkat apa yang dibuat dan alasannya.
- Di akhir setiap PB, tulis: apa yang dibantu AI, file yang berubah, dan cara memverifikasinya. Ini bahan AI & Tools Disclosure / Development Log.

## Perintah

```
./gradlew assembleDebug
./gradlew installDebug
./gradlew testDebugUnitTest
```
