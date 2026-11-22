package id.ac.itb.k09.historygo.trigger

import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * State "mendekati POI" (PB10). Koordinat di sini titik buatan untuk pengujian, BUKAN koordinat POI asli.
 */
class ApproachTest {

    private val baseLat = -6.92
    private val baseLng = 107.61
    private val metersPerDegreeLat = 111_195.0
    private val now = 1_000_000L

    private fun poi(id: String, northMeters: Double) = Poi(
        id = id, nama = "POI $id", kategori = "uji",
        lat = baseLat + northMeters / metersPerDegreeLat, lng = baseLng,
    )

    private fun at(northMeters: Double, accuracy: Double = 5.0, ageMs: Long = 0) = LocationReading(
        lat = baseLat + northMeters / metersPerDegreeLat, lng = baseLng,
        accuracyM = accuracy, timeMillis = now - ageMs,
    )

    @Test
    fun diAntaraRadiusMasukDanJarakMendekati_terdeteksiMendekati() {
        val result = findApproaching(listOf(poi("A", 0.0)), at(60.0), now)
        assertEquals("A", result?.poi?.id)
        assertEquals(60.0, result!!.distanceM, 1.0)
    }

    @Test
    fun didalamRadiusMasuk_bukanMendekati() {
        // 20 m sudah masuk radius 25 m: itu state "terpicu", bukan "mendekati".
        assertNull(findApproaching(listOf(poi("A", 0.0)), at(20.0), now))
    }

    @Test
    fun lebihJauhDariJarakMendekati_tidakAda() {
        assertNull(findApproaching(listOf(poi("A", 0.0)), at(TriggerConfig.APPROACH_DISTANCE_M + 20), now))
    }

    @Test
    fun duaPoiDalamJangkauan_yangTerdekatDipilih() {
        val pois = listOf(poi("JAUH", 0.0), poi("DEKAT", 140.0))
        // Pengguna di 80 m: 80 m dari JAUH, 60 m dari DEKAT. Keduanya dalam jangkauan; DEKAT yang dipilih.
        assertEquals("DEKAT", findApproaching(pois, at(80.0), now)?.poi?.id)
    }

    @Test
    fun bacaanTidakValid_diabaikan() {
        val pois = listOf(poi("A", 0.0))
        assertNull(findApproaching(pois, at(60.0, accuracy = 50.0), now))   // akurasi > 30 m
        assertNull(findApproaching(pois, at(60.0, ageMs = 20_000), now))    // umur > 15 detik
    }

    @Test
    fun poiTanpaKoordinat_diabaikan() {
        val tanpaKoordinat = Poi(id = "X", nama = "X", kategori = "uji")
        assertNull(findApproaching(listOf(tanpaKoordinat), at(60.0), now))
        assertNull(findNearest(listOf(tanpaKoordinat), at(60.0)))
    }

    @Test
    fun terdekat_mengembalikanPoiPalingDekatBerapaPunJaraknya() {
        val pois = listOf(poi("A", 0.0), poi("B", 1_000.0))
        val result = findNearest(pois, at(900.0))
        assertEquals("B", result?.poi?.id)
        assertEquals(100.0, result!!.distanceM, 1.0)
    }
}
