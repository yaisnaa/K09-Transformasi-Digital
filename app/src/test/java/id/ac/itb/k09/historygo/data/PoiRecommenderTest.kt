package id.ac.itb.k09.historygo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Acceptance F07 no. 1-4.
 * Koordinat di sini adalah titik buatan untuk pengujian, BUKAN koordinat POI asli.
 * Kecepatan jalan 80 m/menit dan waktu kunjungan 5 menit per POI.
 */
class PoiRecommenderTest {

    private val baseLat = -6.92
    private val baseLng = 107.61
    private val metersPerDegreeLat = 111_195.0

    private fun poi(id: String, northMeters: Double, vararg filter: PoiFilter) = Poi(
        id = id, nama = "POI $id", kategori = "uji",
        lat = baseLat + northMeters / metersPerDegreeLat, lng = baseLng,
        filter = filter.toList(),
    )

    // Semua POI berjajar ke utara dari titik awal.
    private val pois = listOf(
        poi("H1", 100.0, PoiFilter.HERITAGE),
        poi("F1", 150.0, PoiFilter.SPOT_FOTO),
        poi("H2", 300.0, PoiFilter.HERITAGE),
        poi("F2", 400.0, PoiFilter.SPOT_FOTO),
        poi("H3", 500.0, PoiFilter.HERITAGE),
        poi("K1", 50.0, PoiFilter.ISTIRAHAT_KULINER),
    )

    private fun recommend(profile: UserProfile, visited: Set<String> = emptySet()) =
        PoiRecommender.recommend(pois, profile, baseLat, baseLng, visited)

    @Test
    fun totalEstimasi_tidakMelebihiDurasi() {
        // 15 menit: H1 = 2 menit jalan + 5 kunjungan = 7; H2 = +3 jalan +5 = 15; tidak ada ruang lagi.
        val hasil = recommend(UserProfile(15, listOf(PoiFilter.HERITAGE)))
        assertTrue(hasil.isNotEmpty())
        assertTrue(hasil.last().cumulativeMinutes <= 15)
        assertEquals(listOf("H1", "H2"), hasil.map { it.poi.id })
    }

    @Test
    fun poiYangSudahDikunjungi_tidakDirekomendasikan() {
        val hasil = recommend(UserProfile(120, listOf(PoiFilter.HERITAGE)), visited = setOf("H1"))
        assertFalse(hasil.any { it.poi.id == "H1" })
    }

    @Test
    fun maksimalTigaRekomendasi_urutMinatLaluJarak() {
        val hasil = recommend(UserProfile(120, listOf(PoiFilter.HERITAGE)))
        assertEquals(3, hasil.size)
        // K1 (50 m) dan F1 (150 m) lebih dekat, tetapi Heritage didahulukan karena sesuai minat.
        assertEquals(listOf("H1", "H2", "H3"), hasil.map { it.poi.id })
        assertTrue(hasil.all { it.matchesInterest })
    }

    @Test
    fun duaProfilBerbeda_menghasilkanRekomendasiBerbeda() {
        val heritage = recommend(UserProfile(60, listOf(PoiFilter.HERITAGE))).map { it.poi.id }
        val foto = recommend(UserProfile(60, listOf(PoiFilter.SPOT_FOTO))).map { it.poi.id }
        assertNotEquals(heritage, foto)
        assertEquals("F1", foto.first())
    }

    @Test
    fun minatHabis_diisiPoiTerdekatYangMasihMuat() {
        // Hanya ada 1 POI kuliner; 2 slot sisanya diisi POI terdekat lain.
        val hasil = recommend(UserProfile(120, listOf(PoiFilter.ISTIRAHAT_KULINER)))
        assertEquals("K1", hasil.first().poi.id)
        assertEquals(3, hasil.size)
        assertFalse(hasil[1].matchesInterest)
    }

    @Test
    fun urutanKunjungan_dimulaiDariYangTerdekatDenganTitikAwal() {
        // Profil Spot Foto memilih F1 (150 m), F2 (400 m), lalu H2 sebagai pengisi.
        // Urutan kunjungan disusun ulang menjadi terdekat-berikutnya: F1 -> H2 (300 m) -> F2 (400 m).
        val hasil = recommend(UserProfile(60, listOf(PoiFilter.SPOT_FOTO)))
        assertEquals(listOf("F1", "H2", "F2"), hasil.map { it.poi.id })

        // Jarak tiap langkah dihitung dari titik sebelumnya, dan waktu kumulatif terus naik.
        assertEquals(150.0, hasil[0].distanceM.toDouble(), 1.0)
        assertEquals(150.0, hasil[1].distanceM.toDouble(), 1.0)
        assertEquals(100.0, hasil[2].distanceM.toDouble(), 1.0)
        assertTrue(hasil.zipWithNext().all { (a, b) -> a.cumulativeMinutes < b.cumulativeMinutes })
        assertTrue(hasil.last().cumulativeMinutes <= 60)
    }

    @Test
    fun durasiTerlaluSingkat_tidakAdaRekomendasi() {
        // Waktu kunjungan saja 5 menit, jadi durasi 4 menit tidak cukup untuk satu POI pun.
        assertTrue(recommend(UserProfile(4, listOf(PoiFilter.HERITAGE))).isEmpty())
    }

    @Test
    fun tanpaLokasi_tetapMemberiRekomendasi() {
        val hasil = PoiRecommender.recommend(pois, UserProfile(60, listOf(PoiFilter.HERITAGE)), null, null, emptySet())
        assertTrue(hasil.isNotEmpty())
    }
}
