package id.ac.itb.k09.historygo.trigger

import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/** Jari-jari rata-rata bumi (meter). Konstanta fisika, bukan parameter trigger. */
private const val EARTH_RADIUS_M = 6_371_000.0

private fun Double.toRadians(): Double = this * PI / 180.0

/**
 * Jarak garis lurus antara dua titik di permukaan bumi, dalam meter (rumus haversine).
 * Kotlin murni: tidak memakai Location.distanceBetween milik Android.
 */
fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val dLat = (lat2 - lat1).toRadians()
    val dLng = (lng2 - lng1).toRadians()
    val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(lat1.toRadians()) * cos(lat2.toRadians()) * sin(dLng / 2) * sin(dLng / 2)
    return 2 * EARTH_RADIUS_M * asin(sqrt(a))
}