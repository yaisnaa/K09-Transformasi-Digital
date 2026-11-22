package id.ac.itb.k09.historygo.trigger

import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi

/** Sebuah POI beserta jaraknya dari pengguna (meter). */
data class PoiDistance(val poi: Poi, val distanceM: Double)

/**
 * State "mendekati POI" (PB10, empat state prototype PRD). Kotlin murni.
 * Mengembalikan POI terdekat yang berada di luar radius masuk (ENTRY_RADIUS_M) tetapi masih
 * dalam APPROACH_DISTANCE_M, atau null. Bacaan tidak valid atau kedaluwarsa diabaikan,
 * sama seperti TriggerEngine. Fungsi ini tidak memicu apa pun; hanya untuk tampilan.
 */
fun findApproaching(pois: List<Poi>, reading: LocationReading, nowMillis: Long): PoiDistance? {
    if (!isValidReading(reading, nowMillis)) return null
    return distancesFrom(pois, reading)
        .filter { it.distanceM > TriggerConfig.ENTRY_RADIUS_M && it.distanceM <= TriggerConfig.APPROACH_DISTANCE_M }
        .minByOrNull { it.distanceM }
}

/** POI terdekat dari pengguna, berapa pun jaraknya (state "di luar area"). Null kalau tidak ada POI berkoordinat. */
fun findNearest(pois: List<Poi>, reading: LocationReading): PoiDistance? =
    distancesFrom(pois, reading).minByOrNull { it.distanceM }

private fun distancesFrom(pois: List<Poi>, reading: LocationReading): List<PoiDistance> =
    pois.filter { it.hasCoordinates }.map { poi ->
        PoiDistance(poi, distanceMeters(reading.lat, reading.lng, poi.lat!!, poi.lng!!))
    }
