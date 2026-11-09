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
     */
    fun onReading(reading: LocationReading, nowMillis: Long): Poi? {
        // 1. Bacaan tidak valid atau kedaluwarsa: abaikan, hitungan calon tidak berubah.
        if (!isValid(reading, nowMillis)) return null

        // 2. Hitung jarak ke semua POI.
        val distances: Map<Poi, Double> = pois.associateWith { poi ->
            distanceMeters(reading.lat, reading.lng, poi.lat!!, poi.lng!!)
        }

        // 3. POI yang sudah terpicu dan kini berjarak > 40 m: aktifkan kembali.
        for ((poi, distance) in distances) {
            if (distance > TriggerConfig.REARM_DISTANCE_M) triggeredIds.remove(poi.id)
        }

        // 4. Cari satu POI terdekat yang berada di dalam radius masuk.
        val nearest: Poi? = distances
            .filter { (_, distance) -> distance <= TriggerConfig.ENTRY_RADIUS_M }
            .minByOrNull { (_, distance) -> distance }
            ?.key

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

    private fun isValid(r: LocationReading, nowMillis: Long): Boolean =
        r.lat.isFinite() && r.lng.isFinite() &&
                r.lat in -90.0..90.0 && r.lng in -180.0..180.0 &&
                r.accuracyM.isFinite() && r.accuracyM >= 0.0 &&
                r.accuracyM <= TriggerConfig.MAX_ACCURACY_M &&
                nowMillis - r.timeMillis <= TriggerConfig.MAX_AGE_MS
}