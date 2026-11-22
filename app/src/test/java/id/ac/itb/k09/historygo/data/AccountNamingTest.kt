package id.ac.itb.k09.historygo.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Penyimpanan per akun (CL-03): tiap akun punya nama penyimpanan sendiri, tamu memakai nama lama. */
class AccountNamingTest {

    @Test
    fun akunTamu_memakaiNamaLama_agarProgresLamaTidakHilang() {
        assertEquals("historygo_game", AccountNaming.scoped("historygo_game", AccountNaming.GUEST_ID))
        assertEquals("visit_history.json", AccountNaming.scopedFile("visit_history.json", AccountNaming.GUEST_ID))
    }

    @Test
    fun akunBerbeda_namaPenyimpananBerbeda() {
        val a = AccountNaming.scoped("historygo_game", "a1b2")
        val b = AccountNaming.scoped("historygo_game", "c3d4")
        assertEquals("historygo_game_a1b2", a)
        assertNotEquals(a, b)
        assertEquals("visit_history_a1b2.json", AccountNaming.scopedFile("visit_history.json", "a1b2"))
    }

    @Test
    fun idTidakAman_ditolak() {
        assertFalse(AccountNaming.isValidId(""))
        assertFalse(AccountNaming.isValidId("../rahasia"))
        assertFalse(AccountNaming.isValidId("Akun A"))
        assertTrue(AccountNaming.isValidId("a1b2"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun scopedDenganIdTidakAman_gagal() {
        AccountNaming.scoped("historygo_game", "../x")
    }
}
