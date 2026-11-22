package id.ac.itb.k09.historygo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/**
 * Tema HistoryGo. Dynamic Color (Material You) sengaja TIDAK dipakai:
 * palet brand harus sama di semua HP (DESIGN.md, bagian Colors).
 */
@Composable
fun HistoryGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalHgExtraColors provides if (darkTheme) DarkExtraColors else LightExtraColors,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            shapes = Shapes,
            content = content,
        )
    }
}

/** Akses warna dan gaya huruf khusus HistoryGo, mis. HistoryGoTheme.colors.ornament. */
object HistoryGoTheme {
    val colors: HgExtraColors
        @Composable @ReadOnlyComposable
        get() = LocalHgExtraColors.current

    val type: HgExtraType
        get() = HgType
}
