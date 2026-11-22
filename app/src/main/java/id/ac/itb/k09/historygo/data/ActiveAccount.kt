package id.ac.itb.k09.historygo.data

import android.content.Context
import androidx.core.content.edit
import id.ac.itb.k09.historygo.game.GameStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Akun yang sedang aktif di HP ini (CL-03, akun lokal). Default: akun tamu.
 * ProfileStore, GameStore, dan VisitHistoryStore menyimpan data per akun ini.
 * Layar Masuk/Daftar (belum dibuat) cukup memanggil [switchTo].
 */
object ActiveAccount {

    private const val PREFS_NAME = "historygo_account"
    private const val KEY_ACTIVE_ID = "active_id"

    private val _id = MutableStateFlow(AccountNaming.GUEST_ID)
    val id: StateFlow<String> = _id.asStateFlow()

    private var loaded = false

    @Synchronized
    fun load(context: Context) {
        if (loaded) return
        _id.value = prefs(context).getString(KEY_ACTIVE_ID, null)
            ?.takeIf { it == AccountNaming.GUEST_ID || AccountNaming.isValidId(it) }
            ?: AccountNaming.GUEST_ID
        loaded = true
    }

    /** Id akun aktif sekarang (memuat dari penyimpanan kalau belum). */
    fun current(context: Context): String {
        load(context)
        return _id.value
    }

    /**
     * Ganti akun aktif, lalu muat ulang profil, riwayat, dan progres Jejak Sejarah milik akun itu.
     * Dipakai saat masuk (id akun) dan keluar (AccountNaming.GUEST_ID).
     */
    @Synchronized
    fun switchTo(context: Context, accountId: String) {
        require(accountId == AccountNaming.GUEST_ID || AccountNaming.isValidId(accountId)) { "Id akun tidak valid" }
        load(context)
        _id.value = accountId
        prefs(context).edit { putString(KEY_ACTIVE_ID, accountId) }
        ProfileStore.reload(context)
        GameStore.reload(context)
        VisitHistoryStore.reload(context)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
