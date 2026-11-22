package id.ac.itb.k09.historygo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class YearTextTest {

    @Test
    fun tahunDenganKeteranganDalamKurung() {
        val r = YearTextSplitter.split("1895 (dirombak 1926, diperluas 1940)")
        assertEquals("1895", r.main)
        assertEquals("Dirombak 1926, diperluas 1940", r.note)
    }

    @Test
    fun kurungDiikutiKalimatLain_tidakMasukAngkaUtama() {
        val r = YearTextSplitter.split("1939 (bangunan sekarang); Hotel Homann sudah ada sejak abad ke-19")
        assertEquals("1939", r.main)
        assertEquals("Bangunan sekarang; Hotel Homann sudah ada sejak abad ke-19", r.note)
    }

    @Test
    fun rentangDanAlternatifTahun() {
        assertEquals(YearText("1928-1931", null), YearTextSplitter.split("1928-1931"))
        val r = YearTextSplitter.split("1810-1812; bentuk sekarang diresmikan 2003")
        assertEquals("1810-1812", r.main)
        assertEquals("Bentuk sekarang diresmikan 2003", r.note)
        val alt = YearTextSplitter.split("1871 atau 1877 (sumber berbeda)")
        assertEquals("1871 atau 1877", alt.main)
        assertEquals("Sumber berbeda", alt.note)
    }

    @Test
    fun tanpaAngkaDiAwal_semuanyaJadiKeterangan() {
        val r = YearTextSplitter.split("Awal abad ke-19; penataan terakhir 2014")
        assertNull(r.main)
        assertEquals("Awal abad ke-19; penataan terakhir 2014", r.note)
        assertEquals(YearText(null, null), YearTextSplitter.split(null))
    }

    @Test
    fun semuaPoiDiFileAsli_angkaUtamaPendek() {
        // Angka utama tampil dengan huruf besar di setengah lebar plakat; harus pendek agar tidak terpotong.
        val pois = PoiParser.parse(File("src/main/assets/poi.json").readText())
        for (poi in pois) {
            val main = YearTextSplitter.split(poi.infografis?.tahunDibangun).main ?: continue
            assertTrue("${poi.id}: angka utama terlalu panjang: $main", main.length <= 14)
        }
    }
}
