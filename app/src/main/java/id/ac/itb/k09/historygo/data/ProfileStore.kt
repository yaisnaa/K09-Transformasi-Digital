package id.ac.itb.k09.historygo.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/**
 * Menyimpan profil pengguna dari kuesioner (F07) di perangkat (SharedPreferences, format JSON),
 * supaya tidak perlu mengisi ulang setiap membuka aplikasi.
 */
object ProfileStore {

    private const val PREFS_NAME = "historygo_profile"
    private const val KEY_PROFILE = "profile_json"
    private const val KEY_SKIPPED = "profile_skipped"

    private val json = Json { ignoreUnknownKeys = true }

    private val _profile = MutableStateFlow<UserProfile?>(null)
    /** null = kuesioner belum diisi. */
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _skipped = MutableStateFlow(false)
    /** true = pengguna memilih "Lewati" di kuesioner, jadi jangan ditampilkan otomatis lagi. */
    val skipped: StateFlow<Boolean> = _skipped.asStateFlow()

    private var loaded = false

    @Synchronized
    fun load(context: Context) {
        if (loaded) return
        val prefs = prefs(context)
        _profile.value = prefs.getString(KEY_PROFILE, null)?.let { text ->
            runCatching { json.decodeFromString(UserProfile.serializer(), text) }.getOrNull()
        }
        _skipped.value = prefs.getBoolean(KEY_SKIPPED, false)
        loaded = true
    }

    fun save(context: Context, profile: UserProfile) {
        load(context)
        _profile.value = profile
        prefs(context).edit { putString(KEY_PROFILE, json.encodeToString(UserProfile.serializer(), profile)) }
    }

    fun skip(context: Context) {
        load(context)
        _skipped.value = true
        prefs(context).edit { putBoolean(KEY_SKIPPED, true) }
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
