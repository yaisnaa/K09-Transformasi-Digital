package id.ac.itb.k09.historygo.data

import id.ac.itb.k09.historygo.trigger.distanceMeters
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/** Profil pengguna dari kuesioner (F07). */
@Serializable
data class UserProfile(
    val durationMinutes: Int,          // berapa lama waktu yang dimiliki pengguna
    val interests: List<PoiFilter>,    // minat, memakai 3 kategori yang sama dengan pencarian (F05)
)

/** Satu POI yang direkomendasikan, berurutan sebagai rute. */
data class Recommendation(
    val poi: Poi,
    val distanceM: Int,                // jarak dari titik sebelumnya (posisi pengguna untuk yang pertama)
    val walkMinutes: Int,              // waktu jalan kaki dari titik sebelumnya
    val cumulativeMinutes: Int,        // total waktu sejak berangkat sampai selesai berkunjung di POI ini
    val matchesInterest: Boolean,
)

/** Rekomendasi kontekstual (F07). Kotlin murni, tanpa Android. */
object PoiRecommender {

    /** Pilihan durasi di kuesioner (menit). */
    val DURATION_OPTIONS = listOf(30, 60, 90, 120)

    /** Perkiraan waktu yang dihabiskan di setiap POI, di luar waktu jalan kaki (keputusan tim, 2026-11-21). */
    const val VISIT_MINUTES_PER_POI = 5

    /** F07 no. 3: maksimal 3 rekomendasi. */
    const val MAX_RECOMMENDATIONS = 3

    /**
     * Menyusun rute rekomendasi secara bertahap: dari posisi sekarang, pilih POI berikutnya yang
     * (1) belum dikunjungi, (2) masih muat dalam sisa durasi, lalu urutkan menurut
     * cocok-minat dulu, baru jarak terdekat. Ulangi dari POI terpilih sampai 3 POI.
     *
     * @param startLat/startLng posisi pengguna; null kalau lokasi belum terbaca
     *        (maka dipakai titik tengah semua POI sebagai titik awal perkiraan)
     * @param visitedIds POI yang sudah dikunjungi (dari riwayat "POI yang dilewati")
     */
    fun recommend(
        pois: List<Poi>,
        profile: UserProfile,
        startLat: Double?,
        startLng: Double?,
        visitedIds: Set<String>,
    ): List<Recommendation> {
        val candidates = pois.filter { it.hasCoordinates && it.id !in visitedIds }.toMutableList()
        if (candidates.isEmpty()) return emptyList()

        var currentLat = startLat ?: candidates.map { it.lat!! }.average()
        var currentLng = startLng ?: candidates.map { it.lng!! }.average()
        var elapsed = 0
        val result = mutableListOf<Recommendation>()

        while (result.size < MAX_RECOMMENDATIONS) {
            val options = candidates.mapNotNull { poi ->
                val distance = distanceMeters(currentLat, currentLng, poi.lat!!, poi.lng!!)
                val walk = PoiSearch.walkMinutes(distance)
                val finishAt = elapsed + walk + VISIT_MINUTES_PER_POI
                // F07 no. 1: total estimasi rute tidak boleh melebihi durasi pilihan pengguna.
                if (finishAt > profile.durationMinutes) return@mapNotNull null
                Recommendation(
                    poi = poi,
                    distanceM = distance.roundToInt(),
                    walkMinutes = walk,
                    cumulativeMinutes = finishAt,
                    matchesInterest = poi.filter.any { it in profile.interests },
                )
            }
            // F07 no. 3: urut menurut minat dulu, lalu jarak.
            val next = options.minWithOrNull(
                compareByDescending<Recommendation> { it.matchesInterest }
                    .thenBy { it.distanceM }
                    .thenBy { it.poi.id }
            ) ?: break

            result += next
            candidates.remove(next.poi)
            elapsed = next.cumulativeMinutes
            currentLat = next.poi.lat!!
            currentLng = next.poi.lng!!
        }

        // Urutan kunjungan disusun ulang: mulai dari yang terdekat dengan titik awal,
        // lalu terdekat dari POI sebelumnya (keputusan tim, 2026-11-21).
        val startLatFinal = startLat ?: pois.filter { it.hasCoordinates }.map { it.lat!! }.average()
        val startLngFinal = startLng ?: pois.filter { it.hasCoordinates }.map { it.lng!! }.average()
        val reordered = orderByNearest(result.map { it.poi }, startLatFinal, startLngFinal, profile)
        // F07 no. 1 tetap dijaga: kalau urutan baru melebihi durasi, pakai urutan semula.
        return if (reordered.isNotEmpty() && reordered.last().cumulativeMinutes <= profile.durationMinutes) {
            reordered
        } else {
            result
        }
    }

    /** Menyusun urutan kunjungan "terdekat berikutnya" dari titik awal, lalu menghitung ulang jarak dan waktunya. */
    internal fun orderByNearest(
        selected: List<Poi>,
        startLat: Double,
        startLng: Double,
        profile: UserProfile,
    ): List<Recommendation> {
        val remaining = selected.toMutableList()
        var lat = startLat
        var lng = startLng
        var elapsed = 0
        val ordered = mutableListOf<Recommendation>()
        while (remaining.isNotEmpty()) {
            val nearest = remaining.minWith(
                compareBy<Poi> { distanceMeters(lat, lng, it.lat!!, it.lng!!) }.thenBy { it.id }
            )
            val distance = distanceMeters(lat, lng, nearest.lat!!, nearest.lng!!)
            val walk = PoiSearch.walkMinutes(distance)
            elapsed += walk + VISIT_MINUTES_PER_POI
            ordered += Recommendation(
                poi = nearest,
                distanceM = distance.roundToInt(),
                walkMinutes = walk,
                cumulativeMinutes = elapsed,
                matchesInterest = nearest.filter.any { it in profile.interests },
            )
            remaining.remove(nearest)
            lat = nearest.lat!!
            lng = nearest.lng!!
        }
        return ordered
    }
}
