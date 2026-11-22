package id.ac.itb.k09.historygo.account

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/** Kesalahan isian formulir Masuk/Daftar. Teksnya diterjemahkan di layar (A01). */
enum class AccountError { NAME_EMPTY, EMAIL_INVALID, PASSWORD_SHORT, PASSWORD_MISMATCH, EMAIL_TAKEN, WRONG_CREDENTIALS }

/** Validasi isian akun (CL-03). Kotlin murni, diuji di AccountRulesTest. */
object AccountRules {
    const val MIN_PASSWORD = 8
    private val EMAIL = Regex("""^[^\s@]+@[^\s@]+\.[^\s@]{2,}$""")

    fun normalizeEmail(email: String) = email.trim().lowercase()

    /** Mengembalikan kesalahan pertama, atau null kalau isian daftar valid. */
    fun validateSignUp(name: String, email: String, password: String, confirm: String): AccountError? = when {
        name.isBlank() -> AccountError.NAME_EMPTY
        !EMAIL.matches(normalizeEmail(email)) -> AccountError.EMAIL_INVALID
        password.length < MIN_PASSWORD -> AccountError.PASSWORD_SHORT
        password != confirm -> AccountError.PASSWORD_MISMATCH
        else -> null
    }

    fun validateSignIn(email: String, password: String): AccountError? = when {
        !EMAIL.matches(normalizeEmail(email)) -> AccountError.EMAIL_INVALID
        password.isEmpty() -> AccountError.WRONG_CREDENTIALS
        else -> null
    }
}

/**
 * Hash password PBKDF2-HMAC-SHA256 dengan salt acak per akun (DECISIONS.md, CL-03).
 * Password asli tidak pernah disimpan. Kotlin murni (javax.crypto ada di JVM dan Android).
 */
object PasswordHasher {
    const val ITERATIONS = 120_000
    private const val KEY_BITS = 256
    private val random = SecureRandom()

    fun newSalt(): ByteArray = ByteArray(16).also { random.nextBytes(it) }

    fun hash(password: String, salt: ByteArray, iterations: Int = ITERATIONS): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    /** Perbandingan waktu-konstan supaya tidak membocorkan info lewat lama proses. */
    fun matches(password: String, salt: ByteArray, expected: ByteArray, iterations: Int): Boolean {
        val actual = hash(password, salt, iterations)
        if (actual.size != expected.size) return false
        var diff = 0
        for (i in actual.indices) diff = diff or (actual[i].toInt() xor expected[i].toInt())
        return diff == 0
    }

    fun encode(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
    fun decode(text: String): ByteArray = Base64.getDecoder().decode(text)
}
