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
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Halaman POI Detail (UI03, F03): foto, sejarah, infografis, audio, sumber. */
@Composable
fun PoiDetailScreen(
    poiId: String,
    onBack: () -> Unit,
    onNavigate: (Poi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    // POI dalam bahasa aktif; ganti bahasa langsung mengganti isi halaman ini (A01).
    val pois = rememberPois()
    val poi = pois.firstOrNull { it.id == poiId }

    // Tombol Back di HP kembali ke layar sebelumnya.
    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())   // halaman bisa di-scroll
            .padding(16.dp),
    ) {
        TextButton(onClick = onBack) { Text(s.back) }

        if (poi == null) {
            Text(s.poiNotFound)
        } else {
            PoiDetailContent(poi, onNavigate = { onNavigate(poi) })
        }
    }
}

@Composable
private fun PoiDetailContent(poi: Poi, onNavigate: () -> Unit) {
    val s = LocalStrings.current
    Text(poi.nama, style = MaterialTheme.typography.headlineSmall)
    Text(poi.kategori, style = MaterialTheme.typography.bodyMedium)
    if (!poi.validated) {
        Text(
            s.notValidated,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
    if (poi.dummy) {
        Text(
            s.dummyLong,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.error,
        )
    }

    // F05 no. 4 + F06: tombol "Navigasi" memulai rute jalan kaki lalu kembali ke peta.
    Spacer(Modifier.height(12.dp))
    Button(onClick = onNavigate, enabled = poi.hasCoordinates) {
        Text(if (poi.hasCoordinates) s.navigateHere else s.navigateNoCoordinates)
    }

    // F08: check-in di lokasi lalu kuis (PRD alur 3: "check-in misi" di POI Detail).
    Spacer(Modifier.height(12.dp))
    CheckInSection(poi)

    SectionTitle(s.sectionPhoto)
    AssetImage(path = poi.foto, contentDescription = s.photoDescription(poi.nama))

    SectionTitle(s.sectionHistory)
    Text(poi.ringkasan ?: s.noSummary)

    SectionTitle(s.sectionInfographic)
    InfografisCard(poi.infografis)

    SectionTitle(s.sectionNarration)
    val audioPath = poi.audio
    if (audioPath == null) {
        Text(s.noAudio)
    } else {
        NarrationButton(poi = poi, audioPath = audioPath)
    }
    SectionTitle(s.sectionSources)
    if (poi.sumber.isEmpty()) {
        Text(s.noSources)
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

    val s = LocalStrings.current
    val image = result?.getOrNull()
    when {
        path == null -> Text(s.photoUnavailable)
        result == null -> Text(s.photoLoading)
        image != null -> Image(
            bitmap = image,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 240.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
        else -> Text(s.photoNotFound(path))
    }
}

@Composable
private fun InfografisCard(info: Infografis?) {
    val s = LocalStrings.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(s.infoYearBuilt, info?.tahunDibangun)
            InfoRow(s.infoArchitect, info?.arsitek)
            InfoRow(s.infoStyle, info?.gayaArsitektur)
            InfoRow(s.infoRole, info?.peranHistoris)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    Row(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.labelLarge, modifier = Modifier.width(130.dp))
        Text(value ?: LocalStrings.current.infoEmpty, style = MaterialTheme.typography.bodyMedium)
    }
}
/** Tombol narasi satu tombol (F04): putar / jeda. */
@Composable
private fun NarrationButton(poi: Poi, audioPath: String) {
    val s = LocalStrings.current
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
                currentId == poi.id -> {
                    // Narasi sudah selesai: putar ulang dari awal. Kalau hanya dijeda: lanjutkan.
                    if (c.playbackState == Player.STATE_ENDED) c.seekTo(0)
                    // Pemutar sempat berhenti total (mis. error atau dihentikan): siapkan lagi.
                    if (c.playbackState == Player.STATE_IDLE) c.prepare()
                    c.play()
                }
                else -> {                                  // narasi POI lain / belum ada: mulai baru
                    c.setMediaItem(narrationItem(poi, audioPath, s.narrationArtist))
                    c.prepare()
                    c.play()
                }
            }
        },
    ) {
        Text(if (thisPoiPlaying) s.pauseNarration else s.playNarration)
    }
}