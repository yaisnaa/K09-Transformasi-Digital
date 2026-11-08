package id.ac.itb.k09.historygo.data

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
    val ringkasan: String? = null,
    val foto: String? = null,
    val infografis: String? = null,
    val audio: String? = null,
    val sumber: List<String> = emptyList(),
    val validated: Boolean = false,   // konten sudah diperiksa tim?
) {
    /** true kalau lat dan lng sudah terisi, artinya POI bisa tampil di peta. */
    val hasCoordinates: Boolean
        get() = lat != null && lng != null
}