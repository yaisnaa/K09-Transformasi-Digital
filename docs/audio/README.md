# Narasi Audio POI (F04)

Narasi dibuat dengan text-to-speech ElevenLabs dari naskah di `naskah.md`.

Status saat ini: narasi bahasa Indonesia P01-P10 sudah ada (`p01.mp3` … `p10.mp3`). Semuanya dibuat lewat situs web ElevenLabs dengan suara Library **Maya** (Voice ID `U3dExJoUNcmTY5H6GMuG`) dan model Eleven Multilingual v2, memakai naskah tanpa perubahan. Narasi bahasa Inggris belum ada, jadi mode EN masih memutar narasi bahasa Indonesia. Akun gratis tidak bisa memakai suara Library lewat API, jadi `tools/generate_narration.py` hanya bisa dipakai dengan paket berbayar; dengan akun gratis, buat audionya lewat situs web. Karena suaranya buatan AI, setiap POI yang memakai narasi ini diberi `"audioAi": true` di `poi.json`. Aplikasi lalu menampilkan label "Suara AI" di bar narasi. Catat juga pemakaian ElevenLabs di AI & Tools Disclosure.

## Persiapan (sekali saja)

1. Buat akun di elevenlabs.io, lalu buat API key di menu **Developers → API Keys**. Izin yang dibutuhkan cukup **Text to Speech** dan **Voices (read)**.
2. Pilih suara di **Voice Library**, tambahkan ke akun ("Add to my voices"), lalu salin **Voice ID**-nya. Pilih suara yang terdengar natural untuk bahasa Indonesia.
3. Tambahkan ke `local.properties` di root project. File ini masuk `.gitignore`, jadi tidak akan ikut ter-commit:

   ```
   ELEVENLABS_API_KEY=isi_api_key
   ELEVENLABS_VOICE_ID=isi_voice_id
   # opsional, suara lain untuk narasi bahasa Inggris
   ELEVENLABS_VOICE_ID_EN=isi_voice_id
   ```

Jangan menempel API key di chat, dokumen, atau commit.

## Membuat audio

Jalankan dari root project:

```
python tools/generate_narration.py --count               # hitung karakter (kuota), tanpa API
python tools/generate_narration.py --voices              # lihat suara di akun
python tools/generate_narration.py --only P01 --lang id  # coba satu file, dengarkan dulu
python tools/generate_narration.py --lang id             # semua narasi bahasa Indonesia
python tools/generate_narration.py                       # semua yang belum ada (ID dan EN)
python tools/generate_narration.py --only P03 --force    # buat ulang satu POI
```

Hasilnya `app/src/main/assets/audio/p01.mp3` (ID) dan `p01_en.mp3` (EN). File yang sudah ada dilewati, kecuali memakai `--force`.

Kuota: seluruh naskah sekitar 12.300 karakter (ID sekitar 6.500, EN sekitar 5.900). Paket gratis ElevenLabs memberi 10.000 karakter per bulan, jadi buat ID dulu, lalu EN bulan berikutnya, atau pakai paket berbayar.

## Memasang di aplikasi

Untuk setiap POI di `app/src/main/assets/poi.json`:

```json
"audio": "audio/p01.mp3",
"audioAi": true,
"en": {
  ...
  "audio": "audio/p01_en.mp3"
}
```

- Kalau `en.audio` tidak diisi, mode bahasa Inggris memutar audio bahasa Indonesia.
- Label "Audio contoh" otomatis hilang setelah `audio` tidak lagi menunjuk ke `audio/placeholder.wav`.
- `TranslationTest.semuaFileAudio_diPoiJson_benarBenarAda` gagal kalau ada path audio yang filenya belum ada.

## Kalau konten berubah

Kalau `ringkasan` POI berubah, perbarui naskahnya di `naskah.md`, lalu buat ulang audionya dengan `--force`. Narasi tidak boleh memuat fakta yang tidak ada di sumber POI (N01).
