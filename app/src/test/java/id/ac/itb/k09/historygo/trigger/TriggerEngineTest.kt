package id.ac.itb.k09.historygo.trigger

import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Skenario minimum dari SPRINT.md PB05.
 * Koordinat di sini adalah titik buatan untuk pengujian, BUKAN koordinat POI asli.
 */
class TriggerEngineTest {

    // Titik acuan buatan. Posisi lain dinyatakan sebagai "sekian meter ke utara" dari titik ini.
    private val baseLat = -6.92
    private val baseLng = 107.61
    private val metersPerDegreeLat = 111_195.0   // 1 derajat lintang ≈ 111,2 km
    private val now = 1_000_000L

    private fun poi(id: String, northMeters: Double) = Poi(
        id = id, nama = "POI $id", kategori = "uji",
        lat = baseLat + northMeters / metersPerDegreeLat, lng = baseLng,
    )

    private fun at(northMeters: Double, accuracy: Double = 5.0, ageMs: Long = 0) = LocationReading(
        lat = baseLat + northMeters / metersPerDegreeLat, lng = baseLng,
        accuracyM = accuracy, timeMillis = now - ageMs,
    )

    /** Skenario 1: masuk radius. Bacaan pertama belum memicu, bacaan kedua memicu. */
    @Test
    fun masukRadius_terpicuSetelahDuaBacaanBerturutTurut() {
        val engine = TriggerEngine(listOf(poi("A", 0.0)))

        assertNull(engine.onReading(at(10.0), now))
        assertEquals("A", engine.onReading(at(10.0), now)?.id)
    }

    /** Skenario 2: satu bacaan GPS meleset ke dalam radius tidak boleh memicu. */
    @Test
    fun satuBacaanMeleset_tidakMemicu() {
        val engine = TriggerEngine(listOf(poi("A", 0.0)))

        assertNull(engine.onReading(at(100.0), now))
        assertNull(engine.onReading(at(5.0), now))     // GPS melompat ke dalam radius
        assertNull(engine.onReading(at(100.0), now))   // kembali ke posisi sebenarnya
        assertNull(engine.onReading(at(100.0), now))
    }

    /** Skenario 3: data tidak valid diabaikan dan tidak ikut dihitung. */
    @Test
    fun dataTidakValid_diabaikan() {
        val engine = TriggerEngine(listOf(poi("A", 0.0)))

        assertNull(engine.onReading(at(5.0, accuracy = 50.0), now))   // akurasi > 30 m
        assertNull(engine.onReading(at(5.0, ageMs = 20_000), now))    // umur > 15 detik
        assertNull(engine.onReading(LocationReading(Double.NaN, baseLng, 5.0, now), now))

        // Tiga bacaan rusak di atas tidak dihitung: masih butuh 2 bacaan valid.
        assertNull(engine.onReading(at(5.0), now))
        assertEquals("A", engine.onReading(at(5.0), now)?.id)
    }

    /** Skenario 4: tetap di area, POI tidak terpicu ulang. */
    @Test
    fun tetapDiArea_tidakTerpicuUlang() {
        val engine = TriggerEngine(listOf(poi("A", 0.0)))
        engine.onReading(at(5.0), now)
        assertNotNull(engine.onReading(at(5.0), now))

        repeat(10) { assertNull(engine.onReading(at(5.0), now)) }
    }

    /** Skenario 5a: keluar > 40 m lalu masuk lagi, POI terpicu lagi. */
    @Test
    fun keluarLebihDari40m_laluMasukLagi_terpicuLagi() {
        val engine = TriggerEngine(listOf(poi("A", 0.0)))
        engine.onReading(at(5.0), now)
        assertNotNull(engine.onReading(at(5.0), now))

        assertNull(engine.onReading(at(45.0), now))    // keluar sejauh 45 m
        assertNull(engine.onReading(at(5.0), now))
        assertEquals("A", engine.onReading(at(5.0), now)?.id)
    }

    /** Skenario 5b: keluar hanya 30 m (belum > 40 m), POI belum aktif lagi. */
    @Test
    fun keluarHanya30m_belumTerpicuLagi() {
        val engine = TriggerEngine(listOf(poi("A", 0.0)))
        engine.onReading(at(5.0), now)
        assertNotNull(engine.onReading(at(5.0), now))

        assertNull(engine.onReading(at(30.0), now))
        assertNull(engine.onReading(at(5.0), now))
        assertNull(engine.onReading(at(5.0), now))
    }

    /** Skenario 6: radius tumpang tindih, hanya POI terdekat yang dipicu, yang lain tidak menyusul. */
    @Test
    fun radiusTumpangTindih_hanyaPoiTerdekat() {
        // A dan B berjarak 30 m, jadi radius 25 m keduanya tumpang tindih.
        val engine = TriggerEngine(listOf(poi("A", 0.0), poi("B", 30.0)))

        // Pengguna di 10 m: jarak ke A = 10 m, ke B = 20 m. Keduanya di dalam radius.
        assertNull(engine.onReading(at(10.0), now))
        assertEquals("A", engine.onReading(at(10.0), now)?.id)

        // Tetap di sana: B tidak boleh ikut terpicu.
        repeat(5) { assertNull(engine.onReading(at(10.0), now)) }
    }
}