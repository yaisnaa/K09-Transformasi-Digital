package id.ac.itb.k09.historygo.navigation

/** Satu titik koordinat. Kotlin murni (bukan LatLng milik Google Maps) supaya mudah diuji. */
data class GeoPoint(val lat: Double, val lng: Double)

/** Rute jalan kaki dari layanan routing (F06). */
data class Route(
    val points: List<GeoPoint>,   // titik-titik garis rute, dari asal ke tujuan
    val distanceM: Double,        // panjang rute menurut layanan routing (meter)
    val durationS: Double,        // estimasi waktu tempuh menurut layanan routing (detik)
)

/**
 * Mengubah "encoded polyline" (format teks ringkas dari Google) menjadi daftar titik.
 * Algoritma standar Google dengan presisi 5 angka desimal.
 */
fun decodePolyline(encoded: String): List<GeoPoint> {
    val points = mutableListOf<GeoPoint>()
    var index = 0
    var lat = 0
    var lng = 0

    // Setiap angka disimpan sebagai potongan 5-bit; potongan dengan bit 0x20 berarti "masih berlanjut".
    fun nextValue(): Int {
        var result = 0
        var shift = 0
        var chunk: Int
        do {
            chunk = encoded[index++].code - 63
            result = result or ((chunk and 0x1f) shl shift)
            shift += 5
        } while (chunk >= 0x20)
        // Bit terakhir menandai tanda negatif.
        return if (result and 1 != 0) (result shr 1).inv() else result shr 1
    }

    while (index < encoded.length) {
        lat += nextValue()   // nilai disimpan sebagai selisih dari titik sebelumnya
        lng += nextValue()
        points += GeoPoint(lat / 1e5, lng / 1e5)
    }
    return points
}
