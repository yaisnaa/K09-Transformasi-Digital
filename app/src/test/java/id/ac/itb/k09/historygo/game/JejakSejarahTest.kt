package id.ac.itb.k09.historygo.game

import id.ac.itb.k09.historygo.data.Kuis
import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.PoiParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Acceptance F08 no. 1-3 (no. 4, progres tetap ada, diuji di emulator karena memakai penyimpanan Android).
 * Koordinat di sini adalah titik buatan untuk pengujian, BUKAN koordinat POI asli.
 */
class JejakSejarahTest {

    private val baseLat = -6.92
    private val baseLng = 107.61
    private val metersPerDegreeLat = 111_195.0
    private val now = 1_000_000L

    private val kuis = Kuis("Soal uji?", listOf("A", "B", "C", "D"), jawaban = 2)
    private fun poi(id: String, northMeters: Double) = Poi(
        id = id, nama = "POI $id", kategori = "uji",
        lat = baseLat + northMeters / metersPerDegreeLat, lng = baseLng, kuis = kuis,
    )
    private fun at(northMeters: Double, accuracy: Double = 5.0) =
        LocationReading(baseLat + northMeters / metersPerDegreeLat, baseLng, accuracy, now)

    private val a = poi("A", 0.0)
    private val b = poi("B", 500.0)

    @Test
    fun checkIn_diDalamRadius_berhasilDanMenambahPoin() {
        val hasil = JejakSejarah.checkIn(GameProgress(), a, at(10.0), now)
        assertTrue(hasil is CheckInResult.Success)
        hasil as CheckInResult.Success
        assertEquals(setOf("A"), hasil.progress.checkedIn)
        assertEquals(JejakSejarah.CHECK_IN_POINTS, hasil.progress.points)
    }

    @Test
    fun checkIn_diLuarRadius_ditolak() {
        val hasil = JejakSejarah.checkIn(GameProgress(), a, at(40.0), now)
        assertTrue(hasil is CheckInResult.TooFar)
        assertEquals(40.0, (hasil as CheckInResult.TooFar).distanceM.toDouble(), 1.0)
    }

    @Test
    fun checkIn_tanpaLokasiAtauBacaanBuruk_ditolak() {
        assertEquals(CheckInResult.NoLocation, JejakSejarah.checkIn(GameProgress(), a, null, now))
        assertEquals(CheckInResult.NoLocation, JejakSejarah.checkIn(GameProgress(), a, at(5.0, accuracy = 80.0), now))
    }

    @Test
    fun checkInDuaKali_tidakMenambahPoinLagi() {
        val sekali = (JejakSejarah.checkIn(GameProgress(), a, at(5.0), now) as CheckInResult.Success).progress
        assertEquals(CheckInResult.AlreadyCheckedIn, JejakSejarah.checkIn(sekali, a, at(5.0), now))
    }

    @Test
    fun jawabanBenar_menambahPoin_salahTidak_danHanyaJawabanPertamaDihitung() {
        val setelahCheckIn = (JejakSejarah.checkIn(GameProgress(), a, at(5.0), now) as CheckInResult.Success).progress

        val benar = JejakSejarah.answer(setelahCheckIn, a, choiceIndex = 2)!!
        assertTrue(benar.correct)
        assertEquals(JejakSejarah.CHECK_IN_POINTS + JejakSejarah.CORRECT_ANSWER_POINTS, benar.progress.points)
        assertNull(JejakSejarah.answer(benar.progress, a, choiceIndex = 2))   // tidak bisa menjawab ulang

        val salah = JejakSejarah.answer(setelahCheckIn, a, choiceIndex = 0)!!
        assertFalse(salah.correct)
        assertEquals(JejakSejarah.CHECK_IN_POINTS, salah.progress.points)
    }

    @Test
    fun kuis_tidakBisaDijawabSebelumCheckIn() {
        assertNull(JejakSejarah.answer(GameProgress(), a, choiceIndex = 2))
    }

    @Test
    fun progresMisi_naikSetelahCheckInDanKuis_badgeTerbukaSaatSemuaSelesai() {
        val pois = listOf(a, b)
        var progress = GameProgress()
        assertEquals(0, JejakSejarah.completedCount(progress, pois))

        progress = (JejakSejarah.checkIn(progress, a, at(5.0), now) as CheckInResult.Success).progress
        assertEquals(0, JejakSejarah.completedCount(progress, pois))      // kuis belum dijawab
        progress = JejakSejarah.answer(progress, a, 1)!!.progress          // salah pun tetap menyelesaikan misi
        assertEquals(1, JejakSejarah.completedCount(progress, pois))
        assertFalse(JejakSejarah.badgeUnlocked(progress, pois))

        progress = (JejakSejarah.checkIn(progress, b, at(500.0), now) as CheckInResult.Success).progress
        progress = JejakSejarah.answer(progress, b, 2)!!.progress
        assertEquals(2, JejakSejarah.completedCount(progress, pois))
        assertTrue(JejakSejarah.badgeUnlocked(progress, pois))
    }

    @Test
    fun kuisDiFileAsli_lengkapDanJawabannyaValid() {
        val pois = PoiParser.parse(File("src/main/assets/poi.json").readText())
        for (poi in pois) {
            val q = poi.kuis
            assertTrue("${poi.id}: belum punya kuis", q != null)
            q!!
            assertTrue("${poi.id}: pertanyaan kosong", q.pertanyaan.isNotBlank())
            assertEquals("${poi.id}: harus 4 pilihan", 4, q.pilihan.size)
            assertTrue("${poi.id}: indeks jawaban di luar pilihan", q.jawaban in q.pilihan.indices)
            assertEquals("${poi.id}: pilihan tidak boleh kembar", 4, q.pilihan.toSet().size)
            // Aturan CLAUDE.md no. 2: kuis untuk POI dengan konten dummy harus ikut ditandai dummy.
            if (poi.dummy) assertTrue("${poi.id}: kuis dari konten dummy harus dummy", q.dummy)
        }
    }
}
