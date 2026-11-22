package id.ac.itb.k09.historygo.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Token warna dari DESIGN.md ("Jurnal Penjelajah Deco").
 * Layar TIDAK boleh memakai hex langsung: ambil dari MaterialTheme.colorScheme
 * atau HistoryGoTheme.colors (warna khusus yang tidak ada di Material 3).
 */
object HgPalette {
    // Tema terang
    val Kertas = Color(0xFFF7EFE3)
    val Kartu = Color(0xFFFFFBF4)
    val KremTua = Color(0xFFEFE2CC)
    val Garis = Color(0xFFE2D3BC)
    val GarisTegas = Color(0xFF8A7660)
    val TintaSepia = Color(0xFF2B1A0E)
    val Bronze = Color(0xFF6E5A48)
    val Chestnut = Color(0xFF4A2C14)
    val ChestnutMuda = Color(0xFF6B4A2C)
    val KuninganTua = Color(0xFF7F5B1C)
    val EmasDeco = Color(0xFFB8893B)
    val TealLokasi = Color(0xFF1F5A5B)
    val TealMuda = Color(0xFFDCE9E6)
    val Terakota = Color(0xFF8C3520)
    val TerakotaMuda = Color(0xFFF6DCCF)

    // Tema gelap
    val GelapLatar = Color(0xFF17110B)
    val GelapKartu = Color(0xFF241B13)
    val GelapKrem = Color(0xFF33281C)
    val GelapGaris = Color(0xFF43362A)
    val GelapTinta = Color(0xFFF3E8D6)
    val GelapTeksKedua = Color(0xFFC4B39C)
    val GelapPrimer = Color(0xFFE3BC78)
    val GelapLabelEmas = Color(0xFFD9B26C)
    val GelapTeal = Color(0xFF6FB3AE)
    val GelapTealMuda = Color(0xFF1F3533)
    val GelapPeringatan = Color(0xFFF2A48D)
    val GelapLatarPeringatan = Color(0xFF3E2119)
}

/** Peran Material 3, tema terang (tabel "Peran Material 3" di DESIGN.md). */
internal val LightColors = lightColorScheme(
    primary = HgPalette.Chestnut,
    onPrimary = HgPalette.Kartu,
    primaryContainer = HgPalette.KremTua,
    onPrimaryContainer = HgPalette.Chestnut,
    secondary = HgPalette.TealLokasi,
    onSecondary = HgPalette.Kartu,
    secondaryContainer = HgPalette.TealMuda,
    onSecondaryContainer = HgPalette.TealLokasi,
    tertiary = HgPalette.KuninganTua,
    onTertiary = HgPalette.Kartu,
    background = HgPalette.Kertas,
    onBackground = HgPalette.TintaSepia,
    surface = HgPalette.Kertas,
    onSurface = HgPalette.TintaSepia,
    surfaceVariant = HgPalette.KremTua,
    onSurfaceVariant = HgPalette.Bronze,
    surfaceContainerLowest = HgPalette.Kartu,
    surfaceContainerLow = HgPalette.Kartu,
    surfaceContainer = HgPalette.Kartu,
    surfaceContainerHigh = HgPalette.Kartu,
    surfaceContainerHighest = HgPalette.KremTua,
    outline = HgPalette.GarisTegas,
    outlineVariant = HgPalette.Garis,
    error = HgPalette.Terakota,
    onError = HgPalette.Kartu,
    errorContainer = HgPalette.TerakotaMuda,
    onErrorContainer = HgPalette.Terakota,
    inverseSurface = HgPalette.TintaSepia,
    inverseOnSurface = HgPalette.GelapTinta,
    inversePrimary = HgPalette.GelapPrimer,
)

/** Peran Material 3, tema gelap. */
internal val DarkColors = darkColorScheme(
    primary = HgPalette.GelapPrimer,
    onPrimary = HgPalette.TintaSepia,
    primaryContainer = HgPalette.GelapKrem,
    onPrimaryContainer = HgPalette.GelapTinta,
    secondary = HgPalette.GelapTeal,
    onSecondary = HgPalette.GelapLatar,
    secondaryContainer = HgPalette.GelapTealMuda,
    onSecondaryContainer = HgPalette.GelapTeal,
    tertiary = HgPalette.GelapLabelEmas,
    onTertiary = HgPalette.TintaSepia,
    background = HgPalette.GelapLatar,
    onBackground = HgPalette.GelapTinta,
    surface = HgPalette.GelapLatar,
    onSurface = HgPalette.GelapTinta,
    surfaceVariant = HgPalette.GelapKrem,
    onSurfaceVariant = HgPalette.GelapTeksKedua,
    surfaceContainerLowest = HgPalette.GelapKartu,
    surfaceContainerLow = HgPalette.GelapKartu,
    surfaceContainer = HgPalette.GelapKartu,
    surfaceContainerHigh = HgPalette.GelapKartu,
    surfaceContainerHighest = HgPalette.GelapKrem,
    outline = HgPalette.GarisTegas,
    outlineVariant = HgPalette.GelapGaris,
    error = HgPalette.GelapPeringatan,
    onError = HgPalette.GelapLatar,
    errorContainer = HgPalette.GelapLatarPeringatan,
    onErrorContainer = HgPalette.GelapPeringatan,
    inverseSurface = HgPalette.GelapTinta,
    inverseOnSurface = HgPalette.TintaSepia,
    inversePrimary = HgPalette.Chestnut,
)

/**
 * Warna khusus HistoryGo yang tidak punya peran di Material 3.
 * Dipakai lewat HistoryGoTheme.colors.
 */
@Immutable
data class HgExtraColors(
    /** Emas Deco: ornamen, bingkai marker, radius 25 m. Bukan untuk teks di latar terang. */
    val ornament: Color,
    /** Latar lingkaran tombol play (warna "cerita"). */
    val playButton: Color,
    /** Ikon di dalam lingkaran tombol play. */
    val onPlayButton: Color,
    /** Chip "Audio contoh" di dalam bar narasi. */
    val narrationChip: Color,
    /** Isi marker bangunan di peta. */
    val markerFill: Color,
    /** Bingkai marker bangunan. */
    val markerRing: Color,
    /** Isi marker bangunan yang sedang terpicu atau menjadi tujuan. */
    val markerActiveFill: Color,
    /** Inti dan bingkai marker aktif. */
    val markerActiveCore: Color,
    /** Cincin putih di sekeliling titik posisi pengguna. */
    val userDotRing: Color,
    /** Warna bayangan lembut (sepia, bukan abu-abu). */
    val shadow: Color,
)

internal val LightExtraColors = HgExtraColors(
    ornament = HgPalette.EmasDeco,
    playButton = HgPalette.EmasDeco,
    onPlayButton = HgPalette.TintaSepia,
    narrationChip = HgPalette.ChestnutMuda,
    markerFill = HgPalette.Chestnut,
    markerRing = HgPalette.EmasDeco,
    markerActiveFill = HgPalette.EmasDeco,
    markerActiveCore = HgPalette.Chestnut,
    userDotRing = HgPalette.Kartu,
    shadow = HgPalette.TintaSepia,
)

internal val DarkExtraColors = HgExtraColors(
    ornament = HgPalette.EmasDeco,
    playButton = HgPalette.TintaSepia,
    onPlayButton = HgPalette.GelapPrimer,
    narrationChip = HgPalette.ChestnutMuda,
    markerFill = Color(0xFFC9A063),
    markerRing = HgPalette.TintaSepia,
    markerActiveFill = HgPalette.GelapPrimer,
    markerActiveCore = HgPalette.TintaSepia,
    userDotRing = HgPalette.GelapLatar,
    shadow = Color.Black,
)

internal val LocalHgExtraColors = staticCompositionLocalOf { LightExtraColors }
