package id.ac.itb.k09.historygo.account

import android.content.Context
import androidx.core.content.edit
import id.ac.itb.k09.historygo.data.AccountNaming
import id.ac.itb.k09.historygo.data.ActiveAccount
import id.ac.itb.k09.historygo.data.ProfileStore
import id.ac.itb.k09.historygo.data.VisitHistoryStore
import id.ac.itb.k09.historygo.game.GameProgress
import id.ac.itb.k09.historygo.game.GameStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.security.SecureRandom

/** Satu akun lokal. Password hanya disimpan sebagai hash + salt (CL-03). */
@Serializable
data class StoredAccount(
    val id: String,
    val name: String,
    val email: String,
    val salt: String,
    val hash: String,
    val iterations: Int,
)

/** Info akun untuk layar (tanpa hash). */
data class AccountInfo(val id: String, val name: String, val email: String)

/**
 * Akun lokal di HP (CL-03, opsi A). Daftar akun disimpan di berkas accounts.json.
 * Masuk/keluar mengganti ActiveAccount, sehingga profil, riwayat, dan progres
 * Jejak Sejarah otomatis milik akun yang aktif.
 */
object AccountStore {

    private const val FILE_NAME = "accounts.json"
    private const val PREFS_NAME = "historygo_account"
    private const val KEY_AUTH_DECIDED = "auth_decided"

    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(StoredAccount.serializer())
    private val random = SecureRandom()

    private val _current = MutableStateFlow<AccountInfo?>(null)
    /** Akun yang sedang masuk; null = mode tamu. */
    val current: StateFlow<AccountInfo?> = _current.asStateFlow()

    private val _authDecided = MutableStateFlow(false)
    /** Pengguna sudah memilih Masuk, Daftar, atau Lanjut tanpa akun; layar akun tidak muncul otomatis lagi. */
    val authDecided: StateFlow<Boolean> = _authDecided.asStateFlow()

    private var loaded = false

    @Synchronized
    fun load(context: Context) {
        if (loaded) return
        _authDecided.value = prefs(context).getBoolean(KEY_AUTH_DECIDED, false)
        val activeId = ActiveAccount.current(context)
        _current.value = readAll(context).firstOrNull { it.id == activeId }?.toInfo()
        loaded = true
    }

    /** Daftar akun baru, lalu langsung masuk. Progres tamu disalin ke akun baru. */
    @Synchronized
    fun signUp(context: Context, name: String, email: String, password: String, confirm: String): AccountError? {
        load(context)
        AccountRules.validateSignUp(name, email, password, confirm)?.let { return it }
        val normalized = AccountRules.normalizeEmail(email)
        val accounts = readAll(context)
        if (accounts.any { it.email == normalized }) return AccountError.EMAIL_TAKEN

        val salt = PasswordHasher.newSalt()
        val account = StoredAccount(
            id = newId(),
            name = name.trim(),
            email = normalized,
            salt = PasswordHasher.encode(salt),
            hash = PasswordHasher.encode(PasswordHasher.hash(password, salt)),
            iterations = PasswordHasher.ITERATIONS,
        )
        writeAll(context, accounts + account)

        // Salin progres mode tamu ke akun baru (pengguna tidak kehilangan check-in dan riwayat).
        val wasGuest = ActiveAccount.current(context) == AccountNaming.GUEST_ID
        ProfileStore.load(context); GameStore.load(context); VisitHistoryStore.load(context)
        val guestProfile = ProfileStore.profile.value
        val guestProgress = GameStore.progress.value
        val guestHistory = VisitHistoryStore.history.value

        ActiveAccount.switchTo(context, account.id)
        if (wasGuest) {
            guestProfile?.let { ProfileStore.save(context, it) }
            if (guestProgress != GameProgress()) GameStore.save(context, guestProgress)
            if (guestHistory.isNotEmpty()) VisitHistoryStore.restore(context, guestHistory)
        }
        _current.value = account.toInfo()
        markDecided(context)
        return null
    }

    /** Masuk dengan email dan password. */
    @Synchronized
    fun signIn(context: Context, email: String, password: String): AccountError? {
        load(context)
        AccountRules.validateSignIn(email, password)?.let { return it }
        val account = readAll(context).firstOrNull { it.email == AccountRules.normalizeEmail(email) }
            ?: return AccountError.WRONG_CREDENTIALS
        val ok = PasswordHasher.matches(password, PasswordHasher.decode(account.salt), PasswordHasher.decode(account.hash), account.iterations)
        if (!ok) return AccountError.WRONG_CREDENTIALS
        ActiveAccount.switchTo(context, account.id)
        _current.value = account.toInfo()
        markDecided(context)
        return null
    }

    /** Keluar: kembali ke mode tamu. Data akun tetap tersimpan untuk masuk lagi nanti. */
    fun signOut(context: Context) {
        load(context)
        ActiveAccount.switchTo(context, AccountNaming.GUEST_ID)
        _current.value = null
    }

    /** "Lanjut tanpa akun". */
    fun continueAsGuest(context: Context) = markDecided(context)

    private fun markDecided(context: Context) {
        _authDecided.value = true
        prefs(context).edit { putBoolean(KEY_AUTH_DECIDED, true) }
    }

    private fun newId(): String = ByteArray(8).also { random.nextBytes(it) }.joinToString("") { "%02x".format(it) }

    private fun StoredAccount.toInfo() = AccountInfo(id, name, email)

    private fun file(context: Context) = File(context.filesDir, FILE_NAME)

    private fun readAll(context: Context): List<StoredAccount> {
        val f = file(context)
        if (!f.exists()) return emptyList()
        return runCatching { json.decodeFromString(serializer, f.readText()) }.getOrDefault(emptyList())
    }

    private fun writeAll(context: Context, accounts: List<StoredAccount>) {
        file(context).writeText(json.encodeToString(serializer, accounts))
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
