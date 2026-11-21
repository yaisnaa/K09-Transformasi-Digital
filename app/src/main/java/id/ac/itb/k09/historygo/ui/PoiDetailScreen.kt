package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.audio.rememberNarration
import id.ac.itb.k09.historygo.data.Infografis
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.ui.components.DecoDivider
import id.ac.itb.k09.historygo.ui.components.StatusChip
import id.ac.itb.k09.historygo.ui.components.rememberAssetBitmap
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/** Audio yang masih placeholder (PB07 belum selesai) diberi label "Audio contoh". */
private const val PLACEHOLDER_AUDIO = "audio/placeholder.wav"

/**
 * POI Detail (UI03, F03, F04, F08). DESIGN.md: foto hero, judul serif, bar narasi,
 * plakat infografis, kartu misi, sejarah, sumber; aksi "Navigasi ke sini" tetap di bawah.
 * Semua elemen terjangkau tanpa tap tambahan dari kartu POI (F03 no. 2).
 */
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

    if (poi == null) {
        Column(modifier.fillMaxSize().padding(20.dp)) {
            BackCircle(onBack)
            Spacer(Modifier.height(16.dp))
            Text(s.poiNotFound, style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                HeroPhoto(poi)
                // Sheet membulat yang sedikit menimpa foto.
                Surface(
                    modifier = Modifier.fillMaxWidth().offset(y = (-24).dp),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    PoiDetailContent(poi)
                }
            }
            // Tombol kembali tetap terlihat walau halaman digulir.
            BackCircle(onBack, Modifier.padding(12.dp))
        }

        // F05 no. 4 + F06: aksi utama tetap di bawah (DESIGN.md, Layout).
        Surface(color = MaterialTheme.colorScheme.surfaceContainer) {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Button(
                    onClick = { onNavigate(poi) },
                    enabled = poi.hasCoordinates,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    Icon(painterResource(R.drawable.ic_navigate), null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (poi.hasCoordinates) s.navigateHere else s.navigateNoCoordinates,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun BackCircle(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    IconButton(
        onClick = onBack,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.primary,
        ),
    ) { Icon(painterResource(R.drawable.ic_back), contentDescription = s.back) }
}

/** Foto 260dp di atas halaman. Kotak krem dengan keterangan kalau foto sedang dimuat atau belum ada. */
@Composable
private fun HeroPhoto(poi: Poi) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val result = rememberAssetBitmap(poi.foto)
    val image = result?.getOrNull()
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = s.photoDescription(poi.nama),
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxWidth().height(260.dp),
        )
    } else {
        Column(
            modifier = Modifier.fillMaxWidth().height(260.dp).background(colors.primaryContainer).padding(top = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            if (result == null) {
                CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = colors.primary)
                Spacer(Modifier.height(8.dp))
                Text(s.photoLoading, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            } else {
                Icon(painterResource(R.drawable.ic_info), null, tint = colors.outline, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(8.dp))
                Text(s.photoUnavailable, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PoiDetailContent(poi: Poi) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // ----- Judul, ornamen, kategori, status -----
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(poi.nama, style = MaterialTheme.typography.headlineLarge, color = colors.onSurface)
            DecoDivider()
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text(poi.kategori, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                if (!poi.validated) StatusChip(s.notValidatedChip)
                if (poi.dummy) StatusChip(s.dummyChip)
            }
            if (poi.dummy) {
                Text(
                    s.dummyLong,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onErrorContainer,
                    modifier = Modifier.fillMaxWidth().background(colors.errorContainer, MaterialTheme.shapes.small).padding(12.dp),
                )
            }
        }

        // ----- Narasi (F04) -----
        NarrationBar(poi)

        // ----- Infografis (F03) -----
        InfografisPlaque(poi.infografis)

        // ----- Misi Jejak Sejarah (F08) -----
        CheckInSection(poi)

        // ----- Sejarah singkat -----
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(s.sectionHistory, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
            Text(poi.ringkasan ?: s.noSummary, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
        }

        // ----- Sumber (F03 no. 3, N01) -----
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            HorizontalDivider(color = colors.outlineVariant)
            Spacer(Modifier.height(8.dp))
            Text(s.sectionSources, style = MaterialTheme.typography.titleSmall, color = colors.onSurfaceVariant)
            if (poi.sumber.isEmpty()) {
                Text(s.noSources, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            } else {
                poi.sumber.forEach { Text(linkify(it), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant) }
            }
        }
    }
}

/** Bar narasi chestnut (DESIGN.md "Signature: Bar narasi"). */
@Composable
private fun NarrationBar(poi: Poi) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val extra = HistoryGoTheme.colors
    val audio = poi.audio

    if (audio == null) {
        // Audio belum ada: bar nonaktif.
        Row(
            modifier = Modifier.fillMaxWidth().background(colors.primaryContainer, MaterialTheme.shapes.large).padding(14.dp, 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(56.dp).border(2.dp, colors.outline, CircleShape))
            Spacer(Modifier.width(14.dp))
            Column {
                Text(s.sectionNarration, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(s.noAudio, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
        return
    }

    val narration = rememberNarration()
    val playing = narration.isPlaying(poi.id)
    Row(
        modifier = Modifier.fillMaxWidth().background(colors.primary, MaterialTheme.shapes.large).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            onClick = { narration.toggle(poi, audio, s.narrationArtist) },
            enabled = narration.ready,
            modifier = Modifier.size(56.dp),
            colors = IconButtonDefaults.iconButtonColors(containerColor = extra.playButton, contentColor = extra.onPlayButton),
        ) {
            Icon(
                painterResource(if (playing) R.drawable.ic_pause else R.drawable.ic_play),
                contentDescription = if (playing) s.pauseStory else s.listenStory,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(if (playing) s.pauseStory else s.listenStory, style = MaterialTheme.typography.titleMedium, color = colors.onPrimary, maxLines = 1)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (audio == PLACEHOLDER_AUDIO) {
                    Text(
                        s.audioSampleChip,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.onPrimary,
                        modifier = Modifier.background(extra.narrationChip, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp),
                        maxLines = 1,
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(s.keepsPlayingLocked, style = MaterialTheme.typography.bodySmall, color = colors.onPrimary.copy(alpha = 0.85f))
            }
        }
    }
}

/** Plakat infografis 2 kolom (DESIGN.md, Cards / Containers). */
@Composable
private fun InfografisPlaque(info: Infografis?) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    // "1895 (dirombak 1926, diperluas 1940)" -> angka besar "1895" + keterangan kecil.
    val year = info?.tahunDibangun
    val yearMatch = year?.let { Regex("""^(\S+)\s*\((.+)\)$""").find(it.trim()) }
    val yearMain = yearMatch?.groupValues?.get(1) ?: year
    val yearNote = yearMatch?.groupValues?.get(2)?.replaceFirstChar { it.uppercase() }

    val shape = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier.fillMaxWidth().clip(shape).border(1.dp, colors.outlineVariant, shape).background(colors.surfaceContainer),
    ) {
        // Tinggi baris mengikuti sel tertinggi, supaya garis tengah lurus dan teks tidak terpotong.
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            PlaqueCell(s.infoYearBuilt, yearMain, yearNote, big = true, modifier = Modifier.weight(1f))
            Box(Modifier.width(1.dp).fillMaxHeight().background(colors.outlineVariant))
            val style = info?.gayaArsitektur
            PlaqueCell(s.infoStyle, style, null, big = (style?.length ?: 0) <= 14, modifier = Modifier.weight(1f))
        }
        HorizontalDivider(color = colors.outlineVariant)
        PlaqueCell(s.infoArchitect, info?.arsitek, null, big = false)
        HorizontalDivider(color = colors.outlineVariant)
        PlaqueCell(s.infoRole, info?.peranHistoris, null, big = false)
    }
}

@Composable
private fun PlaqueCell(label: String, value: String?, note: String?, big: Boolean, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Text(label.uppercase(), style = HistoryGoTheme.type.labelPlakat, color = colors.tertiary)
        when {
            value == null -> Text(s.infoEmpty, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            big -> Text(value, style = MaterialTheme.typography.headlineMedium, color = colors.onSurface, maxLines = 2)
            else -> Text(value, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
        }
        if (note != null) Text(note, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
    }
}

/** Bagian "https://..." dalam teks sumber dijadikan tautan yang bisa diketuk. */
@Composable
private fun linkify(text: String): AnnotatedString {
    val linkColor = MaterialTheme.colorScheme.tertiary
    val match = Regex("""https?://\S+""").find(text) ?: return AnnotatedString(text)
    val url = match.value.trimEnd(')', '.', ',')
    return buildAnnotatedString {
        append(text.substring(0, match.range.first))
        withLink(
            LinkAnnotation.Url(
                url,
                TextLinkStyles(style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)),
            )
        ) { append(url) }
        append(text.substring(match.range.first + url.length))
    }
}
