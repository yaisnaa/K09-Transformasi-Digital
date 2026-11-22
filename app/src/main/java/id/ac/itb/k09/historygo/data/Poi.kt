package id.ac.itb.k09.historygo.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Satu Point of Interest. Nama field sama persis dengan kunci di assets/poi.json.
 * Field yang bisa kosong diberi tanda ? dan nilai bawaan.
 */
@Serializable
data class Poi(
    val id: String,
    val nama: String,
    val kategori: String,
    val titikPemicu: String? = null,
    val lat: Double? = null,          // null = koordinat belum diisi
    val lng: Double? = null,
    val verified: Boolean = false,    // koordinat sudah dicek di lapangan?
    val koordinatDummy: Boolean = false, // true = koordinat perkiraan, BUKAN titik pemicu asli (CLAUDE.md aturan 2)
    val ringkasan: String? = null,
    val foto: String? = null,         // path di folder assets, mis. "foto/p01.jpg"
    val infografis: Infografis? = null,
    val audio: String? = null,        // path di folder assets, mis. "audio/p01.mp3"
    val audioAi: Boolean = false,     // true = narasi dibuat dengan suara AI (text-to-speech); tampil label "Suara AI"
    val sumber: List<String> = emptyList(),
    val validated: Boolean = false,   // konten sudah diperiksa tim?
    val dummy: Boolean = false,       // true = konten contoh buatan, bukan data asli (CLAUDE.md aturan 2)
    val filter: List<PoiFilter> = emptyList(), // kategori untuk filter pencarian (F05); boleh lebih dari satu
    val kuis: Kuis? = null,           // soal kuis "Jejak Sejarah" (F08)
    val en: PoiTranslation? = null,   // terjemahan bahasa Inggris (A01)
) {
    /** true kalau lat dan lng sudah terisi, artinya POI bisa tampil di peta. */
    val hasCoordinates: Boolean
        get() = lat != null && lng != null
}

/**
 * Satu soal pilihan ganda untuk misi "Jejak Sejarah" (F08).
 * jawaban = indeks pilihan yang benar (0 = pilihan pertama).
 * dummy = soal dibuat dari konten dummy (CLAUDE.md aturan 2).
 */
@Serializable
data class Kuis(
    val pertanyaan: String,
    val pilihan: List<String>,
    val jawaban: Int,
    val dummy: Boolean = false,
)

/**
 * Tiga kategori filter pencarian dari PRD F05.
 * @SerialName = tulisan di poi.json; label = tulisan di layar.
 */
@Serializable
enum class PoiFilter(val label: String) {
    @SerialName("heritage") HERITAGE("Heritage"),
    @SerialName("spot_foto") SPOT_FOTO("Spot Foto"),
    @SerialName("istirahat_kuliner") ISTIRAHAT_KULINER("Istirahat/Kuliner"),
}

/**
 * Infografis ringkas (F03). Semua berupa teks supaya fleksibel,
 * mis. tahun "1895" atau "sekitar 1920-an".
 */
@Serializable
data class Infografis(
    val tahunDibangun: String? = null,
    val arsitek: String? = null,
    val gayaArsitektur: String? = null,
    val peranHistoris: String? = null,
)