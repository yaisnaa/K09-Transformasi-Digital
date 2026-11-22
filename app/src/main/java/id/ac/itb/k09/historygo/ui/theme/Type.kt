package id.ac.itb.k09.historygo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import id.ac.itb.k09.historygo.R

/*
 * Tipografi dari DESIGN.md. Font dibundel di res/font (lisensi OFL, salinannya di assets/licenses).
 * - DM Serif Display: nama tempat dan judul
 * - Inter (variable font): semua teks UI
 * - IBM Plex Mono: KHUSUS angka (jarak, waktu, poin)
 * Ukuran memakai sp agar ikut pengaturan ukuran huruf sistem. Tidak ada teks di bawah 12sp.
 */

val DmSerifDisplay = FontFamily(Font(R.font.dm_serif_display, FontWeight.Normal))

@OptIn(ExperimentalTextApi::class)
private fun interWeight(weight: Int) = Font(
    resId = R.font.inter_variable,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

val Inter = FontFamily(
    interWeight(400),
    interWeight(500),
    interWeight(600),
)

val IbmPlexMono = FontFamily(
    Font(R.font.ibm_plex_mono_regular, FontWeight.Normal),
    Font(R.font.ibm_plex_mono_medium, FontWeight.Medium),
)

private fun serif(size: Int, lineHeight: Double, tracking: Double = 0.0) = TextStyle(
    fontFamily = DmSerifDisplay,
    fontWeight = FontWeight.Normal,
    fontSize = size.sp,
    lineHeight = (size * lineHeight).sp,
    letterSpacing = tracking.em,
)

private fun inter(size: Int, weight: Int, lineHeight: Double) = TextStyle(
    fontFamily = Inter,
    fontWeight = FontWeight(weight),
    fontSize = size.sp,
    lineHeight = (size * lineHeight).sp,
    letterSpacing = 0.sp,
)

/** Skala huruf Material 3 dipetakan ke tiga font HistoryGo. */
val Typography = Typography(
    displayLarge = serif(48, 1.05, -0.01),
    displayMedium = serif(40, 1.06, -0.01),
    displaySmall = serif(36, 1.08, -0.01),
    headlineLarge = serif(32, 1.08),
    headlineMedium = serif(28, 1.1),
    headlineSmall = serif(24, 1.12),
    titleLarge = inter(18, 600, 1.35),
    titleMedium = inter(16, 600, 1.35),
    titleSmall = inter(14, 600, 1.35),
    bodyLarge = inter(16, 400, 1.6),
    bodyMedium = inter(14, 400, 1.45),
    bodySmall = inter(13, 400, 1.4),
    labelLarge = inter(16, 600, 1.25),
    labelMedium = inter(13, 500, 1.3),
    labelSmall = inter(12, 500, 1.3),
)

/** Gaya tambahan yang tidak ada di skala Material. Dipakai lewat HistoryGoTheme.type. */
data class HgExtraType(
    /** Angka kecil-sedang: "18 m", "±14.22", "35 poin". */
    val angka: TextStyle,
    /** Angka besar: sisa jarak navigasi, "3/10". */
    val angkaBesar: TextStyle,
    /** Label infografis: huruf kapital, tracking lebar. */
    val labelPlakat: TextStyle,
    /** Nama tempat di daftar (Headline S yang lebih kecil). */
    val namaTempat: TextStyle,
)

val HgType = HgExtraType(
    angka = TextStyle(
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
    angkaBesar = TextStyle(
        fontFamily = IbmPlexMono,
        fontWeight = FontWeight.Medium,
        fontSize = 30.sp,
        lineHeight = 34.sp,
    ),
    labelPlakat = TextStyle(
        fontFamily = Inter,
        fontWeight = FontWeight(600),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.08.em,
    ),
    namaTempat = serif(20, 1.15),
)
