package id.ac.itb.k09.historygo.audio

import android.content.ComponentName
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import id.ac.itb.k09.historygo.data.Poi

/** Membuat item audio dari POI. Judulnya tampil di notifikasi media dan lock screen. */
fun narrationItem(poi: Poi, audioPath: String): MediaItem =
    MediaItem.Builder()
        .setMediaId(poi.id)
        .setUri("asset:///$audioPath")   // file di app/src/main/assets/
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(poi.nama)
                .setArtist("HistoryGo · Narasi sejarah")
                .build()
        )
        .build()

/**
 * Menghubungkan layar ke NarrationService.
 * Mengembalikan null selama koneksi belum siap (biasanya hanya sepersekian detik).
 */
@Composable
fun rememberMediaController(): MediaController? {
    val context = LocalContext.current
    var controller by remember { mutableStateOf<MediaController?>(null) }

    DisposableEffect(Unit) {
        val token = SessionToken(context, ComponentName(context, NarrationService::class.java))
        val future = MediaController.Builder(context, token).buildAsync()
        // Koneksi dibuat di latar; saat siap, simpan controller-nya.
        future.addListener(
            { controller = runCatching { future.get() }.getOrNull() },
            ContextCompat.getMainExecutor(context),
        )
        // Saat layar ditutup, putuskan koneksi. Audio TETAP berputar di service.
        onDispose {
            MediaController.releaseFuture(future)
            controller = null
        }
    }
    return controller
}