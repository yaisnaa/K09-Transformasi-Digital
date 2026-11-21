package id.ac.itb.k09.historygo.i18n

import id.ac.itb.k09.historygo.data.AppLanguage
import id.ac.itb.k09.historygo.data.PoiFilter
import id.ac.itb.k09.historygo.data.PoiParser
import id.ac.itb.k09.historygo.data.localized
import id.ac.itb.k09.historygo.navigation.RouteError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Uji dwibahasa (A01): kamus teks aplikasi dan terjemahan konten POI. */
class TranslationTest {

    private val pois = PoiParser.parse(File("src/main/assets/poi.json").readText())

    @Test
    fun kamus_dipilihSesuaiBahasa() {
        assertSame(IdStrings, AppStrings.of(AppLanguage.ID))
        assertSame(EnStrings, AppStrings.of(AppLanguage.EN))
    }

    @Test
    fun kamusInggris_berbedaDariIndonesia() {
        assertNotEquals(IdStrings.back, EnStrings.back)
        assertNotEquals(IdStrings.missionCardTitle, EnStrings.missionCardTitle)
        assertNotEquals(IdStrings.badgeName, EnStrings.badgeName)
        assertNotEquals(IdStrings.minutes(30), EnStrings.minutes(30))
        PoiFilter.entries.forEach { assertTrue(EnStrings.filterLabel(it).isNotBlank()) }
        RouteError.entries.forEach { assertNotEquals(IdStrings.routeError(it), EnStrings.routeError(it)) }
    }

    @Test
    fun semuaPoi_punyaTerjemahanInggris() {
        pois.forEach { poi ->
            assertNotNull("${poi.id} belum punya blok en", poi.en)
            val en = poi.en!!
            assertTrue("${poi.id} nama", !en.nama.isNullOrBlank())
            assertTrue("${poi.id} kategori", !en.kategori.isNullOrBlank())
            assertTrue("${poi.id} ringkasan", !en.ringkasan.isNullOrBlank())
        }
    }

    @Test
    fun kuisInggris_strukturnyaSamaDenganAslinya() {
        pois.filter { it.kuis != null }.forEach { poi ->
            val id = poi.kuis!!
            val en = poi.en?.kuis
            assertNotNull("${poi.id} kuis belum diterjemahkan", en)
            assertEquals("${poi.id} jumlah pilihan", id.pilihan.size, en!!.pilihan.size)
            assertEquals("${poi.id} indeks jawaban", id.jawaban, en.jawaban)
            assertEquals("${poi.id} status dummy", id.dummy, en.dummy)
        }
    }

    @Test
    fun localized_menggantiTeks_tapiTidakMengubahDataLain() {
        val poi = pois.first { it.en != null }
        val en = poi.localized(AppLanguage.EN)
        assertEquals(poi.en!!.nama, en.nama)
        assertEquals(poi.id, en.id)
        assertEquals(poi.lat, en.lat)
        assertEquals(poi.sumber, en.sumber)
        assertEquals(poi.dummy, en.dummy)
        assertSame(poi, poi.localized(AppLanguage.ID))
    }

    @Test
    fun localized_tanpaTerjemahan_kembaliKeBahasaIndonesia() {
        val poi = PoiParser.parse("""[{"id":"PX","nama":"Uji","kategori":"Tes"}]""").first()
        assertSame(poi, poi.localized(AppLanguage.EN))
    }
}
