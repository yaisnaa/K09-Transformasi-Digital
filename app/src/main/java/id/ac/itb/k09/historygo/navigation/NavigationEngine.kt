package id.ac.itb.k09.historygo.navigation

import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.PoiSearch
import id.ac.itb.k09.historygo.trigger.TriggerConfig
import id.ac.itb.k09.historygo.trigger.distanceMeters
import id.ac.itb.k09.historygo.trigger.isValidReading
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** Hasil perhitungan navigasi untuk satu bacaan GPS. */
data class NavigationUpdate(
    val remainingM: Int,          // sisa jarak ke tujuan (meter)
    val remainingMinutes: Int,    // sisa waktu jalan kaki (menit), 0 kalau sudah tiba
    val arrived: Boolean,         // F06 no. 3: sudah masuk radius POI tujuan
    val needsReroute: Boolean,    // F06 no. 4: menyimpang > 30 m, rute perlu dihitung ulang
    val offRouteM: Int?,          // jarak pengguna ke garis rute (null kalau belum ada rute)
)

/**
 * Logika navigasi jalan kaki (F06). Kotlin murni, tanpa Android.
 * Semua ambang diambil dari TriggerConfig.
 */
class NavigationEngine(private val destination: GeoPoint) {

    private var route: Route? = null
    private var offRouteCount = 0

    /** Posisi saat rute terakhir diminta. Dipakai supaya tidak terus-menerus menghitung ulang di tempat yang sama. */
    private var routeRequestedFrom: GeoPoint? = null

    /**
     * Memasang rute baru (pertama kali atau setelah dihitung ulang).
     * requestedFrom = posisi pengguna saat rute ini diminta.
     */
    fun setRoute(newRoute: Route, requestedFrom: GeoPoint? = null) {
        route = newRoute
        routeRequestedFrom = requestedFrom
        offRouteCount = 0
    }

    /** @return null kalau bacaan tidak valid atau kedaluwarsa. */
    fun onReading(reading: LocationReading, nowMillis: Long): NavigationUpdate? {
        if (!isValidReading(reading, nowMillis)) return null

        val user = GeoPoint(reading.lat, reading.lng)
        val straightToDestination = distanceMeters(user.lat, user.lng, destination.lat, destination.lng)
        val arrived = straightToDestination <= TriggerConfig.ENTRY_RADIUS_M

        val currentRoute = route
        if (currentRoute == null || currentRoute.points.size < 2) {
            // Rute belum ada: pakai jarak garis lurus sebagai perkiraan.
            return NavigationUpdate(
                remainingM = straightToDestination.roundToInt(),
                remainingMinutes = if (arrived) 0 else PoiSearch.walkMinutes(straightToDestination),
                arrived = arrived,
                needsReroute = false,
                offRouteM = null,
            )
        }

        val progress = projectOnRoute(user, currentRoute.points)

        // Kalau pengguna belum berpindah > REROUTE_DISTANCE_M dari titik permintaan rute terakhir, jangan hitung ulang lagi.
        // Contoh: pengguna di dalam gedung, jauh dari jalan; rute baru tetap dimulai dari jalan terdekat
        // sehingga ia selalu tampak "menyimpang". Tanpa pengecekan ini rute diminta ulang setiap 6 detik.
        val requestedFrom = routeRequestedFrom
        val movedSinceRequest = requestedFrom == null ||
            distanceMeters(user.lat, user.lng, requestedFrom.lat, requestedFrom.lng) > TriggerConfig.REROUTE_DISTANCE_M

        // Menyimpang harus terjadi CONFIRM_READINGS kali berturut-turut, supaya satu lompatan GPS tidak memicu hitung ulang.
        val needsReroute = if (!arrived && movedSinceRequest && progress.offRouteM > TriggerConfig.REROUTE_DISTANCE_M) {
            offRouteCount++
            if (offRouteCount >= TriggerConfig.CONFIRM_READINGS) {
                offRouteCount = 0
                true
            } else {
                false
            }
        } else {
            offRouteCount = 0
            false
        }

        // Sisa waktu sebanding dengan sisa jarak, memakai estimasi waktu dari layanan routing.
        val remainingMinutes = when {
            arrived -> 0
            currentRoute.distanceM > 0 && currentRoute.durationS > 0 ->
                maxOf(1, ceil(currentRoute.durationS * (progress.remainingM / currentRoute.distanceM) / 60.0).toInt())
            else -> PoiSearch.walkMinutes(progress.remainingM)
        }

        return NavigationUpdate(
            remainingM = progress.remainingM.roundToInt(),
            remainingMinutes = remainingMinutes,
            arrived = arrived,
            needsReroute = needsReroute,
            offRouteM = progress.offRouteM.roundToInt(),
        )
    }
}

/** Posisi pengguna terhadap garis rute. */
internal data class RouteProgress(
    val offRouteM: Double,     // jarak terpendek pengguna ke garis rute
    val remainingM: Double,    // panjang rute dari titik terdekat sampai ujung rute
)

/**
 * Mencari titik pada garis rute yang paling dekat dengan pengguna, lalu menghitung
 * jarak ke garis dan sisa panjang rute dari titik itu.
 * Untuk jarak pendek (ratusan meter) bumi bisa dianggap datar di sekitar pengguna.
 */
internal fun projectOnRoute(user: GeoPoint, points: List<GeoPoint>): RouteProgress {
    val metersPerDegLat = 111_195.0
    val metersPerDegLng = metersPerDegLat * cos(user.lat * PI / 180.0)
    fun x(p: GeoPoint) = (p.lng - user.lng) * metersPerDegLng   // pengguna di titik (0, 0)
    fun y(p: GeoPoint) = (p.lat - user.lat) * metersPerDegLat

    var bestDistance = Double.MAX_VALUE
    var bestSegment = 0
    var bestT = 0.0
    for (i in 0 until points.size - 1) {
        val ax = x(points[i]); val ay = y(points[i])
        val bx = x(points[i + 1]); val by = y(points[i + 1])
        val dx = bx - ax; val dy = by - ay
        val lengthSq = dx * dx + dy * dy
        // t = posisi titik terdekat di segmen (0 = awal, 1 = akhir).
        val t = if (lengthSq == 0.0) 0.0 else (-(ax * dx + ay * dy) / lengthSq).coerceIn(0.0, 1.0)
        val px = ax + t * dx; val py = ay + t * dy
        val d = sqrt(px * px + py * py)
        if (d < bestDistance) {
            bestDistance = d
            bestSegment = i
            bestT = t
        }
    }

    // Sisa jarak = sisa segmen terdekat + semua segmen sesudahnya.
    fun segmentLength(i: Int) =
        distanceMeters(points[i].lat, points[i].lng, points[i + 1].lat, points[i + 1].lng)
    var remaining = segmentLength(bestSegment) * (1 - bestT)
    for (i in bestSegment + 1 until points.size - 1) remaining += segmentLength(i)

    return RouteProgress(offRouteM = bestDistance, remainingM = remaining)
}
