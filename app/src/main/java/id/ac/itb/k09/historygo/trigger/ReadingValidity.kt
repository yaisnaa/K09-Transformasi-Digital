package id.ac.itb.k09.historygo.trigger

import id.ac.itb.k09.historygo.data.LocationReading

/**
 * Apakah bacaan GPS layak dipakai? Dipakai bersama oleh TriggerEngine dan NavigationEngine
 * supaya aturan valid/tidaknya bacaan hanya ada di satu tempat. Kotlin murni.
 */
fun isValidReading(r: LocationReading, nowMillis: Long): Boolean =
    r.lat.isFinite() && r.lng.isFinite() &&
        r.lat in -90.0..90.0 && r.lng in -180.0..180.0 &&
        r.accuracyM.isFinite() && r.accuracyM >= 0.0 &&
        r.accuracyM <= TriggerConfig.MAX_ACCURACY_M &&
        nowMillis - r.timeMillis <= TriggerConfig.MAX_AGE_MS
