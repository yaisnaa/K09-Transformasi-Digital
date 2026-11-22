package id.ac.itb.k09.historygo.ui.components

import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset

/*
 * Gerak HistoryGo (PB10). Mengikuti pola Material: fade-through antar tab, shared-axis (geser)
 * saat masuk/keluar layar yang lebih dalam, sheet peta naik dari bawah. Satu momen yang menonjol:
 * kartu POI terpicu ("cerita datang ke kamu"). Semua gerak dimatikan bila pengguna memilih
 * "Hapus animasi" di Android.
 */

/** Easing Material 3 "emphasized decelerate". */
private val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

/** Layar yang tampil dan kedalamannya: 0 = tab, makin besar = makin "dalam". */
data class ScreenKey(val id: String, val depth: Int)

/** true kalau pengguna mematikan animasi di Android (Settings → Aksesibilitas → Hapus animasi). */
@Composable
fun rememberReduceMotion(): Boolean {
    val context = LocalContext.current
    return remember {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
        }.getOrDefault(false)
    }
}

/** Nilai terakhir yang tidak null; dipakai agar konten yang sedang keluar tetap utuh selama animasi. */
@Composable
fun <T> rememberLastNonNull(value: T?): T? {
    val holder = remember { mutableStateOf(value) }
    if (value != null) holder.value = value
    val last by holder
    return last
}

/** Transisi antar layar utama. */
fun screenTransition(reduceMotion: Boolean): AnimatedContentTransitionScope<ScreenKey>.() -> ContentTransform = {
    if (reduceMotion) {
        EnterTransition.None togetherWith ExitTransition.None
    } else {
        val from = initialState.depth
        val to = targetState.depth
        when {
            // Antar tab / setara: fade-through.
            from == to -> (fadeIn(tween(210, delayMillis = 90, easing = LinearOutSlowInEasing)) +
                scaleIn(tween(210, delayMillis = 90, easing = LinearOutSlowInEasing), initialScale = 0.97f)) togetherWith
                fadeOut(tween(90, easing = FastOutLinearInEasing))
            // Masuk lebih dalam: geser dari kanan.
            to > from -> (slideInHorizontally(tween(320, easing = EmphasizedDecelerate)) { it / 5 } +
                fadeIn(tween(240, delayMillis = 40))) togetherWith
                (slideOutHorizontally(tween(320, easing = EmphasizedDecelerate)) { -it / 10 } + fadeOut(tween(120)))
            // Kembali: geser ke kanan.
            else -> (slideInHorizontally(tween(320, easing = EmphasizedDecelerate)) { -it / 10 } +
                fadeIn(tween(240, delayMillis = 40))) togetherWith
                (slideOutHorizontally(tween(320, easing = EmphasizedDecelerate)) { it / 5 } + fadeOut(tween(120)))
        }.using(SizeTransform(clip = false))
    }
}

/**
 * Transisi sheet di bawah peta. Kartu POI terpicu naik penuh dengan pegas (momen utama);
 * sheet lain naik sedikit dan memudar.
 */
fun sheetTransition(reduceMotion: Boolean): AnimatedContentTransitionScope<String>.() -> ContentTransform = {
    if (reduceMotion) {
        EnterTransition.None togetherWith ExitTransition.None
    } else {
        val triggered = targetState.startsWith("poi:")
        val enter = if (triggered) {
            slideInVertically(spring<IntOffset>(dampingRatio = 0.78f, stiffness = 380f)) { it } +
                fadeIn(tween(180))
        } else {
            slideInVertically(tween(280, easing = EmphasizedDecelerate)) { it / 3 } + fadeIn(tween(220))
        }
        (enter togetherWith (slideOutVertically(tween(180, easing = FastOutLinearInEasing)) { it / 4 } + fadeOut(tween(150))))
            .using(SizeTransform(clip = false))
    }
}
