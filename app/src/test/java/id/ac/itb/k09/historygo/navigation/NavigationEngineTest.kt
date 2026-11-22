package id.ac.itb.k09.historygo.navigation

import id.ac.itb.k09.historygo.data.LocationReading
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos

/**
 * Acceptance F06 no. 2-4 dan dekoder polyline.
 * Koordinat di sini adalah titik buatan untuk pengujian, BUKAN koordinat POI asli.
 */
class NavigationEngineTest {

    private val baseLat = -6.92
    private val baseLng = 107.61
    private val metersPerDegLat = 111_195.0
    private val metersPerDegLng = metersPerDegLat * cos(baseLat * PI / 180.0)
    private val now = 1_000_000L

    private fun point(northM: Double, eastM: Double = 0.0) =
        GeoPoint(baseLat + northM / metersPerDegLat, baseLng + eastM / metersPerDegLng)

    private fun at(northM: Double, eastM: Double = 0.0, accuracy: Double = 5.0): LocationReading {
        val p = point(northM, eastM)
        return LocationReading(p.lat, p.lng, accuracy, now)
    }

    /** Rute lurus ke utara sepanjang 200 m, estimasi 150 detik. Tujuan di ujung rute. */
    private fun engineWithRoute(): NavigationEngine {
        val engine = NavigationEngine(destination = point(200.0))
        engine.setRoute(Route(listOf(point(0.0), point(100.0), point(200.0)), distanceM = 200.0, durationS = 150.0))
        return engine
    }

    @Test
    fun sisaJarakDanWaktu_berkurangSaatMendekat() {
        val engine = engineWithRoute()

        val awal = engine.onReading(at(50.0), now)!!
        assertEquals(150.0, awal.remainingM.toDouble(), 2.0)
        assertEquals(2, awal.remainingMinutes)      // 150 s x 150/200 = 112,5 s -> dibulatkan ke atas 2 menit
        assertFalse(awal.arrived)

        val dekat = engine.onReading(at(150.0), now)!!
        assertTrue(dekat.remainingM < awal.remainingM)
        assertEquals(1, dekat.remainingMinutes)
    }

    @Test
    fun masukRadiusTujuan_statusTiba() {
        val update = engineWithRoute().onReading(at(190.0), now)!!
        assertTrue(update.arrived)
        assertEquals(0, update.remainingMinutes)
    }

    @Test
    fun menyimpangLebihDari30m_duaKaliBerturutTurut_rutePerluDihitungUlang() {
        val engine = engineWithRoute()

        val pertama = engine.onReading(at(100.0, eastM = 50.0), now)!!
        assertFalse(pertama.needsReroute)            // satu bacaan belum cukup
        assertEquals(50.0, pertama.offRouteM!!.toDouble(), 2.0)

        assertTrue(engine.onReading(at(100.0, eastM = 50.0), now)!!.needsReroute)
    }

    @Test
    fun satuLompatanGps_tidakMemicuHitungUlang() {
        val engine = engineWithRoute()
        assertFalse(engine.onReading(at(100.0, eastM = 50.0), now)!!.needsReroute)
        assertFalse(engine.onReading(at(110.0), now)!!.needsReroute)   // kembali ke rute
        assertFalse(engine.onReading(at(120.0, eastM = 50.0), now)!!.needsReroute)
    }

    @Test
    fun menyimpang20m_masihDianggapDiRute() {
        val engine = engineWithRoute()
        repeat(5) { assertFalse(engine.onReading(at(100.0, eastM = 20.0), now)!!.needsReroute) }
    }

    @Test
    fun setelahHitungUlang_tidakMengulangDiTempatYangSama() {
        // Pengguna 50 m dari jalan (mis. di dalam gedung). Rute baru diminta dari titik itu,
        // tetapi garisnya tetap dimulai dari jalan, jadi pengguna masih tampak "menyimpang".
        val engine = NavigationEngine(destination = point(200.0))
        engine.setRoute(
            Route(listOf(point(0.0), point(100.0), point(200.0)), distanceM = 200.0, durationS = 150.0),
            requestedFrom = point(100.0, eastM = 50.0),
        )
        repeat(6) { assertFalse(engine.onReading(at(100.0, eastM = 50.0), now)!!.needsReroute) }

        // Setelah berpindah > 30 m dan tetap di luar rute, barulah boleh dihitung ulang.
        assertFalse(engine.onReading(at(140.0, eastM = 50.0), now)!!.needsReroute)
        assertTrue(engine.onReading(at(140.0, eastM = 50.0), now)!!.needsReroute)
    }

    /** Rute rekomendasi F07: awal -> titik 1 (100 m) -> titik 2 (200 m) -> titik 3 (300 m), lurus ke utara. */
    @Test
    fun beberapaTitik_berlanjutOtomatisSampaiTitikTerakhir() {
        val engine = NavigationEngine(stops = listOf(point(100.0), point(200.0), point(300.0)))
        engine.setRoute(
            Route(listOf(point(0.0), point(100.0), point(200.0), point(300.0)), distanceM = 300.0, durationS = 225.0)
        )

        val awal = engine.onReading(at(0.0), now)!!
        assertEquals(0, awal.currentStopIndex)
        assertEquals(300.0, awal.remainingM.toDouble(), 2.0)     // sisa sampai titik terakhir
        assertEquals(100.0, awal.toNextStopM.toDouble(), 2.0)

        val diTitik1 = engine.onReading(at(95.0), now)!!
        assertEquals(0, diTitik1.reachedStopIndex)               // titik 1 tercapai
        assertEquals(1, diTitik1.currentStopIndex)               // lanjut menuju titik 2
        assertFalse(diTitik1.arrived)

        val diTitik2 = engine.onReading(at(200.0), now)!!
        assertEquals(1, diTitik2.reachedStopIndex)
        assertEquals(2, diTitik2.currentStopIndex)

        val diTitik3 = engine.onReading(at(295.0), now)!!
        assertEquals(2, diTitik3.reachedStopIndex)
        assertTrue(diTitik3.arrived)                             // "Tiba" hanya di titik terakhir
        assertEquals(0, diTitik3.remainingMinutes)
    }

    @Test
    fun beberapaTitik_titikBerikutnyaTidakTerlewatiSebelumTitikSekarangDicapai() {
        val engine = NavigationEngine(stops = listOf(point(100.0), point(200.0)))
        // Langsung berada di titik 2 tanpa melewati titik 1: titik 2 belum dihitung tercapai.
        val update = engine.onReading(at(200.0), now)!!
        assertNull(update.reachedStopIndex)
        assertEquals(0, update.currentStopIndex)
        assertEquals(listOf(point(100.0), point(200.0)), engine.remainingStops)
    }

    @Test
    fun bacaanTidakValid_diabaikan() {
        assertNull(engineWithRoute().onReading(at(50.0, accuracy = 80.0), now))
    }

    @Test
    fun belumAdaRute_pakaiJarakGarisLurus() {
        val engine = NavigationEngine(destination = point(200.0))
        val update = engine.onReading(at(0.0), now)
        assertNotNull(update)
        assertEquals(200.0, update!!.remainingM.toDouble(), 2.0)
        assertNull(update.offRouteM)
    }

    @Test
    fun decodePolyline_contohResmiGoogle() {
        // Contoh dari dokumentasi algoritma encoded polyline Google.
        val points = decodePolyline("_p~iF~ps|U_ulLnnqC_mqNvxq`@")
        assertEquals(3, points.size)
        assertEquals(38.5, points[0].lat, 1e-9)
        assertEquals(-120.2, points[0].lng, 1e-9)
        assertEquals(40.7, points[1].lat, 1e-9)
        assertEquals(-120.95, points[1].lng, 1e-9)
        assertEquals(43.252, points[2].lat, 1e-9)
        assertEquals(-126.453, points[2].lng, 1e-9)
    }
}
