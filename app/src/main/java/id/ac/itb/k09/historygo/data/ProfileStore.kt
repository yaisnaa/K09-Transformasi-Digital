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

    /** Akun yang datanya sedang dimuat; null = belum dimuat (CL-03, progres per akun). */
    private var loadedFor: String? = null

    @Synchronized
    fun load(context: Context) {
        val account = ActiveAccount.current(context)
        if (loadedFor == account) return
        val prefs = prefs(context)
        _profile.value = prefs.getString(KEY_PROFILE, null)?.let { text ->
            runCatching { json.decodeFromString(UserProfile.serializer(), text) }.getOrNull()
        }
        _skipped.value = prefs.getBoolean(KEY_SKIPPED, false)
        loadedFor = account
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
