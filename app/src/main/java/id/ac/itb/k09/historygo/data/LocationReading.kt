package id.ac.itb.k09.historygo.data

/**
 * Satu hasil bacaan GPS.
 * Sengaja tidak memakai class Location milik Android supaya logika trigger
 * tetap Kotlin murni dan mudah diuji (lihat DECISIONS.md, bagian iOS).
 */
data class LocationReading(
    val lat: Double,         // lintang (latitude)
    val lng: Double,         // bujur (longitude)
    val accuracyM: Double,   // perkiraan meleset, dalam meter
    val timeMillis: Long,    // waktu bacaan diambil
)