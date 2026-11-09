package id.ac.itb.k09.historygo.ui

import android.graphics.BitmapFactory
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import id.ac.itb.k09.historygo.audio.narrationItem
import id.ac.itb.k09.historygo.audio.rememberMediaController
import id.ac.itb.k09.historygo.data.Infografis
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.loadPoisFromAssets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Halaman POI Detail (UI03, F03): foto, sejarah, infografis, audio, sumber. */
@Composable
fun PoiDetailScreen(poiId: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val poi = remember(poiId) { loadPoisFromAssets(context).firstOrNull { it.id == poiId } }

    // Tombol Back di HP kembali ke layar sebelumnya.
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // halaman bisa di-scroll
            .padding(16.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Kembali") }

        if (poi == null) {
            Text("POI tidak ditemukan.")
        } else {
            PoiDetailContent(poi)
        }
    }
}

@Composable
private fun PoiDetailContent(poi: Poi) {
    Text(poi.nama, style = MaterialTheme.typography.headlineSmall)
    Text(poi.kategori, style = MaterialTheme.typography.bodyMedium)
    if (!poi.validated) {
        Text(
            "Konten belum divalidasi tim.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
    }

    SectionTitle("Foto")
    AssetImage(path = poi.foto, contentDescription = "Foto ${poi.nama}")

    SectionTitle("Sejarah singkat")
    Text(poi.ringkasan ?: "Ringkasan sejarah belum tersedia.")

    SectionTitle("Infografis")
    InfografisCard(poi.infografis)

    SectionTitle("Narasi audio")
    val audioPath = poi.audio
    if (audioPath == null) {
        Text("Audio belum tersedia.")
    } else {
        NarrationButton(poi = poi, audioPath = audioPath)
    }
    SectionTitle("Sumber")
    if (poi.sumber.isEmpty()) {
        Text("Sumber rujukan belum dicantumkan.")
    } else {
        poi.sumber.forEach { Text("• $it") }
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(20.dp))
    Text(text, style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(8.dp))
}

/** Menampilkan gambar dari folder assets. Dibaca di latar belakang supaya layar tidak macet. */
@Composable
private fun AssetImage(path: String?, contentDescription: String) {
    val context = LocalContext.current
    // null = masih dimuat; Result = berhasil atau gagal.
    val result by produceState<Result<ImageBitmap>?>(initialValue = null, path) {
        if (path != null) {
            value = withContext(Dispatchers.IO) {
                runCatching {
                    context.assets.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                            ?: error("Format gambar tidak dikenali")
                    }
                }
            }
        }
    }

    val image = result?.getOrNull()
    when {
        path == null -> Text("Foto belum tersedia.")
        result == null -> Text("Memuat foto…")
        image != null -> Image(
            bitmap = image,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        else -> Text("Foto tidak ditemukan: $path")
    }
}

@Composable
private fun InfografisCard(info: Infografis?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow("Tahun dibangun", info?.tahunDibangun)
            InfoRow("Arsitek", info?.arsitek)
            InfoRow("Gaya arsitektur", info?.gayaArsitektur)
            InfoRow("Peran historis", info?.peranHistoris)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(130.dp))
        Text(value ?: "belum diisi", style = MaterialTheme.typography.bodyMedium)
    }
}
/** Tombol narasi satu tombol (F04): putar / jeda. */
@Composable
private fun NarrationButton(poi: Poi, audioPath: String) {
    val controller = rememberMediaController()
    var isPlaying by remember { mutableStateOf(false) }
    var currentId by remember { mutableStateOf<String?>(null) }

    // Dengarkan perubahan dari pemutar, termasuk saat dijeda lewat notifikasi atau lock screen.
    DisposableEffect(controller) {
        val c = controller
        val listener = object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                isPlaying = player.isPlaying
                currentId = player.currentMediaItem?.mediaId
            }
        }
        if (c != null) {
            isPlaying = c.isPlaying
            currentId = c.currentMediaItem?.mediaId
            c.addListener(listener)
        }
        onDispose { c?.removeListener(listener) }
    }

    // Tombol ini hanya menampilkan "Jeda" kalau yang sedang berputar adalah narasi POI INI.
    val thisPoiPlaying = isPlaying && currentId == poi.id

    Button(
        enabled = controller != null,
        onClick = {
            val c = controller ?: return@Button
            when {
                thisPoiPlaying -> c.pause()
                currentId == poi.id -> c.play()            // lanjutkan dari posisi terakhir
                else -> {                                  // narasi POI lain / belum ada: mulai baru
                    c.setMediaItem(narrationItem(poi, audioPath))
                    c.prepare()
                    c.play()
                }
            }
        },
    ) {
        Text(if (thisPoiPlaying) "⏸  Jeda narasi" else "▶  Putar narasi")
    }
}