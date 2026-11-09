package id.ac.itb.k09.historygo.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pengaturan pengguna yang disimpan di perangkat (SharedPreferences).
 * Saat ini hanya autoplay narasi; default MATI sesuai DECISIONS.md.
 */
object SettingsStore {

    private const val PREFS_NAME = "historygo_settings"
    private const val KEY_AUTOPLAY = "autoplay_narration"

    private val _autoplay = MutableStateFlow(false)
    val autoplay: StateFlow<Boolean> = _autoplay.asStateFlow()

    private var loaded = false

    /** Membaca pengaturan dari penyimpanan, sekali saja. */
    @Synchronized
    fun load(context: Context) {
        if (loaded) return
        _autoplay.value = prefs(context).getBoolean(KEY_AUTOPLAY, false)
        loaded = true
    }

    /** Mengubah pengaturan autoplay dan langsung menyimpannya. */
    fun setAutoplay(context: Context, enabled: Boolean) {
        load(context)
        _autoplay.value = enabled
        prefs(context).edit { putBoolean(KEY_AUTOPLAY, enabled) }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
