package id.ac.itb.k09.historygo.data

import kotlinx.serialization.Serializable

/** Bahasa aplikasi (A01). */
enum class AppLanguage { ID, EN }

/**
 * Terjemahan konten satu POI (A01). Field yang null memakai teks bahasa Indonesia.
 * Terjemahan dibuat dari konten bahasa Indonesia yang sudah bersumber, bukan konten baru.
 */
@Serializable
data class PoiTranslation(
    val nama: String? = null,
    val kategori: String? = null,
    val titikPemicu: String? = null,
    val ringkasan: String? = null,
    val infografis: Infografis? = null,
    val kuis: Kuis? = null,
    val audio: String? = null,   // narasi bahasa Inggris, mis. "audio/p01_en.mp3"; null = pakai audio bahasa Indonesia
)

/** Mengembalikan POI dengan teks dan audio dalam bahasa yang dipilih. ID, koordinat, foto, dan sumber tidak berubah. */
fun Poi.localized(language: AppLanguage): Poi {
    val t = en
    if (language == AppLanguage.ID || t == null) return this
    return copy(
        nama = t.nama ?: nama,
        kategori = t.kategori ?: kategori,
        titikPemicu = t.titikPemicu ?: titikPemicu,
        ringkasan = t.ringkasan ?: ringkasan,
        infografis = t.infografis ?: infografis,
        kuis = t.kuis ?: kuis,
        audio = t.audio ?: audio,
    )
}
