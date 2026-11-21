package id.ac.itb.k09.historygo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PoiParserTest {

    @Test
    fun poiDenganKoordinat_terbacaBenar() {
        val text = """
            [{"id":"PX","nama":"Uji","kategori":"Tes","lat":-6.9,"lng":107.6,
              "verified":false,"sumber":[],"validated":false}]
        """
        val pois = PoiParser.parse(text)

        assertEquals(1, pois.size)
        assertEquals("PX", pois[0].id)
        assertEquals(-6.9, pois[0].lat!!, 0.0)
        assertTrue(pois[0].hasCoordinates)
    }

    @Test
    fun koordinatNull_tetapTerbaca_tapiTidakPunyaKoordinat() {
        val text = """[{"id":"PX","nama":"Uji","kategori":"Tes","lat":null,"lng":null}]"""
        val pois = PoiParser.parse(text)

        assertEquals(1, pois.size)
        assertFalse(pois[0].hasCoordinates)
    }

    @Test
    fun fieldDummy_bawaannyaFalse_danBisaDibacaTrue() {
        val text = """
            [{"id":"PA","nama":"Asli","kategori":"Tes"},
             {"id":"PB","nama":"Contoh","kategori":"Tes","dummy":true}]
        """
        val pois = PoiParser.parse(text)

        assertFalse(pois[0].dummy)   // tidak ditulis di JSON = bukan dummy
        assertTrue(pois[1].dummy)
    }

    @Test
    fun poiDummy_diFileAsli_tidakBolehTervalidasi_danTanpaSumberTeks() {
        // Aturan CLAUDE.md no. 2: konten dummy tidak boleh ditandai tervalidasi
        // dan tidak boleh mencantumkan sumber rujukan (atribusi foto tetap boleh).
        val pois = PoiParser.parse(File("src/main/assets/poi.json").readText())

        for (poi in pois.filter { it.dummy }) {
            assertFalse("${poi.id}: dummy tidak boleh validated", poi.validated)
            val sumberTeks = poi.sumber.filterNot { it.startsWith("Foto:") }
            assertTrue("${poi.id}: dummy tidak boleh punya sumber teks", sumberTeks.isEmpty())
        }
    }

    @Test
    fun koordinatDummy_diFileAsli_tidakBolehVerified() {
        // Koordinat perkiraan belum dicek di lapangan, jadi tidak boleh ditandai verified.
        val pois = PoiParser.parse(File("src/main/assets/poi.json").readText())

        for (poi in pois.filter { it.koordinatDummy }) {
            assertFalse("${poi.id}: koordinat dummy tidak boleh verified", poi.verified)
            assertTrue("${poi.id}: koordinat dummy harus terisi", poi.hasCoordinates)
        }
    }

    @Test
    fun fileAsliPoiJson_berisiP01sampaiP10_danKoordinatnyaDiBandung() {
        // Unit test dijalankan dari folder app/, jadi path-nya relatif dari sana.
        val pois = PoiParser.parse(File("src/main/assets/poi.json").readText())

        // ID harus P01, P02, ..., P10 dan berurutan.
        val idYangDiharapkan = (1..10).map { "P%02d".format(it) }
        assertEquals(idYangDiharapkan, pois.map { it.id })

        // Koordinat yang sudah diisi harus berada di sekitar Bandung (menangkap salah ketik / lat-lng tertukar).
        for (poi in pois.filter { it.hasCoordinates }) {
            assertTrue("${poi.id}: lat di luar Bandung", poi.lat!! in -7.0..-6.8)
            assertTrue("${poi.id}: lng di luar Bandung", poi.lng!! in 107.5..107.7)
        }
    }
}