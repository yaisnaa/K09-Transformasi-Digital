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
 * - onboarding: sudah dilihat atau belum (PB10, tampil sekali)
 * - izin notifikasi: sudah pernah diminta atau belum, supaya tidak diminta dua kali
 */
object SettingsStore {

    private const val PREFS_NAME = "historygo_settings"
    private const val KEY_AUTOPLAY = "autoplay_narration"
    private const val KEY_DETECTION = "detection_enabled"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_NOTIFICATION_ASKED = "notification_asked"

    private val _autoplay = MutableStateFlow(false)
    val autoplay: StateFlow<Boolean> = _autoplay.asStateFlow()

    private val _detection = MutableStateFlow(true)
    val detection: StateFlow<Boolean> = _detection.asStateFlow()

    private val _onboardingDone = MutableStateFlow(false)
    val onboardingDone: StateFlow<Boolean> = _onboardingDone.asStateFlow()

    private val _notificationAsked = MutableStateFlow(false)
    val notificationAsked: StateFlow<Boolean> = _notificationAsked.asStateFlow()

    private var loaded = false

    /** Membaca pengaturan dari penyimpanan, sekali saja. */
    @Synchronized
    fun load(context: Context) {
        if (loaded) return
        val prefs = prefs(context)
        _autoplay.value = prefs.getBoolean(KEY_AUTOPLAY, false)
        _detection.value = prefs.getBoolean(KEY_DETECTION, true)
        _onboardingDone.value = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        _notificationAsked.value = prefs.getBoolean(KEY_NOTIFICATION_ASKED, false)
        loaded = true
    }

    /** Mengubah pengaturan autoplay dan langsung menyimpannya. */
    fun setAutoplay(context: Context, enabled: Boolean) = set(context, KEY_AUTOPLAY, enabled, _autoplay)

    /** Mengubah pengaturan deteksi bangunan dan langsung menyimpannya. */
    fun setDetection(context: Context, enabled: Boolean) = set(context, KEY_DETECTION, enabled, _detection)

    /** Onboarding selesai (dilanjutkan atau dilewati); tidak ditampilkan lagi. */
    fun setOnboardingDone(context: Context) = set(context, KEY_ONBOARDING_DONE, true, _onboardingDone)

    /** Izin notifikasi sudah pernah diminta. */
    fun setNotificationAsked(context: Context) = set(context, KEY_NOTIFICATION_ASKED, true, _notificationAsked)

    private fun set(context: Context, key: String, value: Boolean, flow: MutableStateFlow<Boolean>) {
        load(context)
        flow.value = value
        prefs(context).edit { putBoolean(key, value) }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
