package id.ac.itb.k09.historygo.game

import android.content.Context
import androidx.core.content.edit
import id.ac.itb.k09.historygo.data.AccountNaming
import id.ac.itb.k09.historygo.data.ActiveAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Menyimpan progres "Jejak Sejarah" di perangkat (F08 no. 4: progres tetap ada setelah aplikasi ditutup).
 */
object GameStore {

    private const val PREFS_NAME = "historygo_game"
    private const val KEY_PROGRESS = "progress_json"

    private val json = Json { ignoreUnknownKeys = true }

    private val _progress = MutableStateFlow(GameProgress())
    val progress: StateFlow<GameProgress> = _progress.asStateFlow()

    /** Akun yang datanya sedang dimuat; null = belum dimuat (CL-03, progres per akun). */
    private var loadedFor: String? = null

    @Synchronized
    fun load(context: Context) {
        val account = ActiveAccount.current(context)
        if (loadedFor == account) return
        _progress.value = prefs(context).getString(KEY_PROGRESS, null)?.let { text ->
            runCatching { json.decodeFromString(GameProgress.serializer(), text) }.getOrNull()
        } ?: GameProgress()
        loadedFor = account
    }

    @Synchronized
    fun save(context: Context, progress: GameProgress) {
        load(context)
        _progress.value = progress
        prefs(context).edit { putString(KEY_PROGRESS, json.encodeToString(GameProgress.serializer(), progress)) }
    }

    /** Mulai ulang permainan (tombol "Ulangi misi"). */
    fun reset(context: Context) = save(context, GameProgress())

    /** Dipanggil ActiveAccount saat berganti akun: buang data akun lama, muat data akun baru. */
    @Synchronized
    fun reload(context: Context) {
        loadedFor = null
        load(context)
    }

    /** Penyimpanan milik akun aktif; akun tamu memakai nama lama. */
    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(
        AccountNaming.scoped(PREFS_NAME, ActiveAccount.current(context)),
        Context.MODE_PRIVATE,
    )
}
