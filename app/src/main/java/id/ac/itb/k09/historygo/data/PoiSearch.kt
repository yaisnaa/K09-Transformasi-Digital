package id.ac.itb.k09.historygo.data

import id.ac.itb.k09.historygo.trigger.distanceMeters
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * Satu hasil pencarian (F05).
 * distanceM dan walkMinutes null kalau posisi pengguna atau koordinat POI belum ada.
 */
data class SearchResult(
    val poi: Poi,
    val distanceM: Int?,
    val walkMinutes: Int?,
)

/** Logika pencarian dan filter POI (F05). Kotlin murni, tanpa Android. */
object PoiSearch {

    /**
     * Kecepatan jalan kaki rata-rata, sekitar 4,8 km/jam.
     * Estimasi waktu memakai jarak garis lurus sampai rute jalan kaki (F06) tersedia.
     */
    const val WALKING_SPEED_M_PER_MIN = 80.0

    /**
     * @param query kata kunci; dicocokkan sebagian dengan nama POI, tanpa membedakan huruf besar/kecil
     * @param filter kategori yang dipilih, atau null untuk semua kategori
     * @param userLat/userLng posisi pengguna, atau null kalau lokasi belum terbaca
     * @return hasil terurut dari yang terdekat; tanpa posisi, terurut menurut nama
     */
    fun search(
        pois: List<Poi>,
        query: String,
        filter: PoiFilter?,
        userLat: Double?,
        userLng: Double?,
    ): List<SearchResult> {
        val keyword = query.trim().lowercase()
        return pois
            .filter { keyword.isEmpty() || it.nama.lowercase().contains(keyword) }
            .filter { filter == null || filter in it.filter }
            .map { poi ->
                val distance = if (userLat != null && userLng != null && poi.hasCoordinates) {
                    distanceMeters(userLat, userLng, poi.lat!!, poi.lng!!)
                } else {
                    null
                }
                SearchResult(
                    poi = poi,
                    distanceM = distance?.roundToInt(),
                    walkMinutes = distance?.let { walkMinutes(it) },
                )
            }
            // Terdekat dulu; yang jaraknya tidak diketahui di paling bawah; seri diurutkan menurut nama.
            .sortedWith(
                compareBy<SearchResult, Int?>(nullsLast()) { it.distanceM }
                    .thenBy { it.poi.nama }
            )
    }

    /** Waktu jalan kaki dalam menit, dibulatkan ke atas, minimal 1 menit. */
    fun walkMinutes(distanceM: Double): Int =
        maxOf(1, ceil(distanceM / WALKING_SPEED_M_PER_MIN).toInt())
}
