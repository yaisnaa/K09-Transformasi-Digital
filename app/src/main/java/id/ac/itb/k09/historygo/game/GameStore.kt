package id.ac.itb.k09.historygo.game

import android.content.Context
import androidx.core.content.edit
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

    private var loaded = false

    @Synchronized
    fun load(context: Context) {
        if (loaded) return
        _progress.value = prefs(context).getString(KEY_PROGRESS, null)?.let { text ->
            runCatching { json.decodeFromString(GameProgress.serializer(), text) }.getOrNull()
        } ?: GameProgress()
        loaded = true
    }

    @Synchronized
    fun save(context: Context, progress: GameProgress) {
        load(context)
        _progress.value = progress
        prefs(context).edit { putString(KEY_PROGRESS, json.encodeToString(GameProgress.serializer(), progress)) }
    }

    /** Mulai ulang permainan (tombol "Ulangi misi"). */
    fun reset(context: Context) = save(context, GameProgress())

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
