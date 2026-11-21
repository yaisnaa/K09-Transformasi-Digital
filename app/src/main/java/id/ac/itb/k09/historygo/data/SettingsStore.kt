package id.ac.itb.k09.historygo.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pengaturan pengguna yang disimpan di perangkat (SharedPreferences).
 * - autoplay narasi: default MATI sesuai DECISIONS.md
 * - deteksi bangunan: default NYALA; kalau dimatikan, TriggerService tidak dijalankan (PB10, layar Pengaturan)
 */
object SettingsStore {

    private const val PREFS_NAME = "historygo_settings"
    private const val KEY_AUTOPLAY = "autoplay_narration"
    private const val KEY_DETECTION = "detection_enabled"

    private val _autoplay = MutableStateFlow(false)
    val autoplay: StateFlow<Boolean> = _autoplay.asStateFlow()

    private val _detection = MutableStateFlow(true)
    val detection: StateFlow<Boolean> = _detection.asStateFlow()

    private var loaded = false

    /** Membaca pengaturan dari penyimpanan, sekali saja. */
    @Synchronized
    fun load(context: Context) {
        if (loaded) return
        val prefs = prefs(context)
        _autoplay.value = prefs.getBoolean(KEY_AUTOPLAY, false)
        _detection.value = prefs.getBoolean(KEY_DETECTION, true)
        loaded = true
    }

    /** Mengubah pengaturan autoplay dan langsung menyimpannya. */
    fun setAutoplay(context: Context, enabled: Boolean) {
        load(context)
        _autoplay.value = enabled
        prefs(context).edit { putBoolean(KEY_AUTOPLAY, enabled) }
    }

    /** Mengubah pengaturan deteksi bangunan dan langsung menyimpannya. */
    fun setDetection(context: Context, enabled: Boolean) {
        load(context)
        _detection.value = enabled
        prefs(context).edit { putBoolean(KEY_DETECTION, enabled) }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
