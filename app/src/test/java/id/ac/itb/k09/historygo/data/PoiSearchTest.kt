package id.ac.itb.k09.historygo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Acceptance F05 no. 1-3 dan pengurutan.
 * Koordinat di sini adalah titik buatan untuk pengujian, BUKAN koordinat POI asli.
 */
class PoiSearchTest {

    private val baseLat = -6.92
    private val baseLng = 107.61
    private val metersPerDegreeLat = 111_195.0

    private fun poi(id: String, nama: String, northMeters: Double?, vararg filter: PoiFilter) = Poi(
        id = id, nama = nama, kategori = "uji",
        lat = northMeters?.let { baseLat + it / metersPerDegreeLat },
        lng = northMeters?.let { baseLng },
        filter = filter.toList(),
    )

    private val pois = listOf(
        poi("A", "Gedung Merdeka", 300.0, PoiFilter.HERITAGE),
        poi("B", "Tugu Bola Dunia", 100.0, PoiFilter.SPOT_FOTO),
        poi("C", "Alun-Alun Bandung", 50.0, PoiFilter.ISTIRAHAT_KULINER),
        poi("D", "Penjara Banceuy", null, PoiFilter.HERITAGE),   // koordinat belum ada
    )

    @Test
    fun kataKunciParsial_tanpaBedaHurufBesarKecil_menemukanPoi() {
        val hasil = PoiSearch.search(pois, "  merDEK ", null, null, null)
        assertEquals(listOf("A"), hasil.map { it.poi.id })
    }

    @Test
    fun filterKategori_hanyaMenampilkanKategoriItu() {
        val hasil = PoiSearch.search(pois, "", PoiFilter.HERITAGE, null, null)
        assertEquals(setOf("A", "D"), hasil.map { it.poi.id }.toSet())
        assertTrue(hasil.all { PoiFilter.HERITAGE in it.poi.filter })
    }

    @Test
    fun denganLokasi_urutDariTerdekat_danMenampilkanJarakSertaWaktu() {
        val hasil = PoiSearch.search(pois, "", null, baseLat, baseLng)

        // C (50 m) -> B (100 m) -> A (300 m) -> D (tanpa koordinat, paling bawah)
        assertEquals(listOf("C", "B", "A", "D"), hasil.map { it.poi.id })
        assertEquals(300, hasil[2].distanceM)
        assertEquals(4, hasil[2].walkMinutes)   // 300 m / 80 m per menit = 3,75 -> dibulatkan ke atas
        assertNull(hasil[3].distanceM)
    }

    @Test
    fun tanpaLokasi_urutMenurutNama_tanpaJarak() {
        val hasil = PoiSearch.search(pois, "", null, null, null)
        assertEquals(
            listOf("Alun-Alun Bandung", "Gedung Merdeka", "Penjara Banceuy", "Tugu Bola Dunia"),
            hasil.map { it.poi.nama },
        )
        assertTrue(hasil.all { it.distanceM == null && it.walkMinutes == null })
    }

    @Test
    fun hasilKosong_saatTidakAdaYangCocok() {
        assertTrue(PoiSearch.search(pois, "tidak ada", null, null, null).isEmpty())
        assertTrue(PoiSearch.search(pois, "merdeka", PoiFilter.SPOT_FOTO, null, null).isEmpty())
    }

    @Test
    fun waktuJalanKaki_minimalSatuMenit() {
        assertEquals(1, PoiSearch.walkMinutes(5.0))
        assertEquals(1, PoiSearch.walkMinutes(80.0))
        assertEquals(2, PoiSearch.walkMinutes(81.0))
    }
}
