package id.ac.itb.k09.historygo.account

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AccountRulesTest {

    @Test
    fun daftarValid_tidakAdaKesalahan() {
        assertNull(AccountRules.validateSignUp("Rina", " Rina@Mail.com ", "rahasia123", "rahasia123"))
    }

    @Test
    fun daftarTidakValid_kesalahanSesuai() {
        assertEquals(AccountError.NAME_EMPTY, AccountRules.validateSignUp(" ", "a@b.co", "rahasia123", "rahasia123"))
        assertEquals(AccountError.EMAIL_INVALID, AccountRules.validateSignUp("A", "bukan-email", "rahasia123", "rahasia123"))
        assertEquals(AccountError.PASSWORD_SHORT, AccountRules.validateSignUp("A", "a@b.co", "pendek", "pendek"))
        assertEquals(AccountError.PASSWORD_MISMATCH, AccountRules.validateSignUp("A", "a@b.co", "rahasia123", "rahasia124"))
    }

    @Test
    fun emailDinormalisasi() {
        assertEquals("rina@mail.com", AccountRules.normalizeEmail("  Rina@Mail.COM "))
    }

    @Test
    fun hashPassword_cocokHanyaDenganPasswordBenar() {
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash("rahasia123", salt, 1_000)
        assertTrue(PasswordHasher.matches("rahasia123", salt, hash, 1_000))
        assertFalse(PasswordHasher.matches("rahasia124", salt, hash, 1_000))
    }

    @Test
    fun saltBerbeda_hashBerbeda() {
        val a = PasswordHasher.hash("rahasia123", PasswordHasher.newSalt(), 1_000)
        val b = PasswordHasher.hash("rahasia123", PasswordHasher.newSalt(), 1_000)
        assertFalse(a.contentEquals(b))
    }

    @Test
    fun encodeDecode_bolakBalik() {
        val salt = PasswordHasher.newSalt()
        assertTrue(salt.contentEquals(PasswordHasher.decode(PasswordHasher.encode(salt))))
    }
}
