package id.ac.itb.k09.historygo.i18n

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.edit
import id.ac.itb.k09.historygo.data.AppLanguage
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.loadPoisFromAssets
import id.ac.itb.k09.historygo.data.localized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Menyimpan pilihan bahasa (A01: "pilihan tetap tersimpan").
 * Mengganti nilai StateFlow langsung menggambar ulang semua layar, jadi tidak perlu restart.
 */
object LanguageStore {

    private const val PREFS_NAME = "historygo_settings"
    private const val KEY_LANGUAGE = "language"

    private val _language = MutableStateFlow(AppLanguage.ID)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    private var loaded = false

    @Synchronized
    fun load(context: Context) {
        if (loaded) return
        val saved = prefs(context).getString(KEY_LANGUAGE, null)
        _language.value = AppLanguage.entries.firstOrNull { it.name == saved } ?: AppLanguage.ID
        loaded = true
    }

    fun set(context: Context, language: AppLanguage) {
        load(context)
        _language.value = language
        prefs(context).edit { putString(KEY_LANGUAGE, language.name) }
    }

    fun toggle(context: Context) =
        set(context, if (language.value == AppLanguage.ID) AppLanguage.EN else AppLanguage.ID)

    /** Untuk kode di luar layar (mis. notifikasi dari service). */
    fun strings(context: Context): AppStrings {
        load(context)
        return AppStrings.of(language.value)
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}

/** Bahasa dan kamus yang sedang aktif, dibagikan ke semua layar oleh MainActivity. */
val LocalLanguage = staticCompositionLocalOf { AppLanguage.ID }
val LocalStrings = staticCompositionLocalOf<AppStrings> { IdStrings }

/** Daftar POI dalam bahasa aktif. Dibaca ulang otomatis saat bahasa diganti. */
@Composable
fun rememberPois(): List<Poi> {
    val context = LocalContext.current
    val language = LocalLanguage.current
    val raw = remember { loadPoisFromAssets(context) }
    return remember(raw, language) { raw.map { it.localized(language) } }
}
