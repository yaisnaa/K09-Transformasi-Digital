package id.ac.itb.k09.historygo.audio

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import id.ac.itb.k09.historygo.data.Poi

/**
 * Pengendali narasi untuk layar (F04). Dipakai kartu POI di peta dan halaman POI Detail,
 * supaya keduanya menampilkan status yang sama (berputar / dijeda), termasuk saat dijeda
 * lewat notifikasi media atau lock screen.
 */
@Stable
class NarrationHandle internal constructor() {
    internal var controller by mutableStateOf<MediaController?>(null)
    private var isPlaying by mutableStateOf(false)
    private var currentId by mutableStateOf<String?>(null)

    /** false selama koneksi ke NarrationService belum siap (biasanya sepersekian detik). */
    val ready: Boolean get() = controller != null

    /** true kalau yang sedang berputar adalah narasi POI ini. */
    fun isPlaying(poiId: String): Boolean = isPlaying && currentId == poiId

    /** Putar, jeda, atau lanjutkan narasi sebuah POI. */
    fun toggle(poi: Poi, audioPath: String, artist: String) {
        val c = controller ?: return
        when {
            isPlaying(poi.id) -> c.pause()
            currentId == poi.id -> {
                // Narasi sudah selesai: putar ulang dari awal. Kalau hanya dijeda: lanjutkan.
                if (c.playbackState == Player.STATE_ENDED) c.seekTo(0)
                // Pemutar sempat berhenti total (mis. error atau dihentikan): siapkan lagi.
                if (c.playbackState == Player.STATE_IDLE) c.prepare()
                c.play()
            }
            else -> {   // narasi POI lain / belum ada: mulai baru
                c.setMediaItem(narrationItem(poi, audioPath, artist))
                c.prepare()
                c.play()
            }
        }
    }

    internal fun sync(player: Player) {
        isPlaying = player.isPlaying
        currentId = player.currentMediaItem?.mediaId
    }
}

/** Menghubungkan layar ke NarrationService dan mengikuti status pemutar. */
@Composable
fun rememberNarration(): NarrationHandle {
    val controller = rememberMediaController()
    val handle = remember { NarrationHandle() }
    handle.controller = controller
    DisposableEffect(controller) {
        val c = controller
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) = handle.sync(player)
        }
        if (c != null) {
            handle.sync(c)
            c.addListener(listener)
        }
        onDispose { c?.removeListener(listener) }
    }
    return handle
}
