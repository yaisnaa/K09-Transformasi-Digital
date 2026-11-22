package id.ac.itb.k09.historygo.trigger

import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi

/**
 * Memutuskan kapan sebuah POI terpicu (F02). Kotlin murni, tanpa Android.
 * Semua angka diambil dari TriggerConfig.
 */
class TriggerEngine(pois: List<Poi>) {

    /** Hanya POI yang punya koordinat yang bisa dipicu. */
    private val pois = pois.filter { it.hasCoordinates }

    /** POI yang sudah terpicu dan belum aktif lagi (pengguna belum menjauh > 40 m). */
    private val triggeredIds = mutableSetOf<String>()

    /** POI calon pemicu dan berapa kali berturut-turut ia menjadi yang terdekat. */
    private var candidateId: String? = null
    private var candidateCount = 0

    /**
     * Dipanggil untuk setiap bacaan GPS.
     * Mengembalikan POI yang BARU SAJA terpicu, atau null kalau tidak ada.
     * nowMillis = waktu sekarang; dikirim dari luar supaya mudah diuji.
     * priorityPoiId = POI tujuan navigasi aktif (F06). Kalau POI itu ada di dalam radius,
     * ia diprioritaskan walau ada POI lain yang lebih dekat (DECISIONS.md, Bab 4.2).
     */
    fun onReading(reading: LocationReading, nowMillis: Long, priorityPoiId: String? = null): Poi? {
        // 1. Bacaan tidak valid atau kedaluwarsa: abaikan, hitungan calon tidak berubah.
        if (!isValidReading(reading, nowMillis)) return null

        // 2. Hitung jarak ke semua POI.
        val distances: Map<Poi, Double> = pois.associateWith { poi ->
            distanceMeters(reading.lat, reading.lng, poi.lat!!, poi.lng!!)
        }

        // 3. POI yang sudah terpicu dan kini berjarak > 40 m: aktifkan kembali.
        for ((poi, distance) in distances) {
            if (distance > TriggerConfig.REARM_DISTANCE_M) triggeredIds.remove(poi.id)
        }

        // 4. Cari satu POI di dalam radius masuk: tujuan navigasi kalau ada di sana, kalau tidak yang terdekat.
        val inRadius = distances.filter { (_, distance) -> distance <= TriggerConfig.ENTRY_RADIUS_M }
        val nearest: Poi? = inRadius.keys.firstOrNull { it.id == priorityPoiId }
            ?: inRadius.minByOrNull { (_, distance) -> distance }?.key

        // Tidak ada POI di dekat pengguna, atau POI terdekat sudah terpicu: tidak ada yang dipicu.
        if (nearest == null || nearest.id in triggeredIds) {
            resetCandidate()
            return null
        }

        // 5. Hitung berapa kali berturut-turut POI ini menjadi yang terdekat.
        if (nearest.id == candidateId) {
            candidateCount++
        } else {
            candidateId = nearest.id
            candidateCount = 1
        }

        if (candidateCount >= TriggerConfig.CONFIRM_READINGS) {
            triggeredIds.add(nearest.id)
            resetCandidate()
            return nearest
        }
        return null
    }

    private fun resetCandidate() {
        candidateId = null
        candidateCount = 0
    }
}