package id.ac.itb.k09.historygo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.ui.components.AdaptiveButtonRow
import id.ac.itb.k09.historygo.ui.components.MapSheet
import id.ac.itb.k09.historygo.ui.components.StatusChip
import id.ac.itb.k09.historygo.ui.components.rememberAssetBitmap
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/**
 * Kartu saat POI terpicu dan aplikasi aktif (F02, UI02). DESIGN.md "Signature: Kartu POI terpicu":
 * tanpa ringkasan teks, narasi sebagai aksi utama (cerita datang ke pengguna).
 *
 * @param distanceM jarak pengguna ke POI, null kalau lokasi belum terbaca
 * @param onListen null kalau POI belum punya audio; tombol "Lihat detail" lalu menjadi aksi utama
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PoiCard(
    poi: Poi,
    distanceM: Int?,
    isPlaying: Boolean,
    onListen: (() -> Unit)?,
    onClose: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val extra = HistoryGoTheme.colors
    val category = poi.filter.firstOrNull()?.let { s.filterLabel(it) } ?: poi.kategori

    MapSheet(modifier = modifier) {
        Box {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.padding(end = 36.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    PoiThumbnail(poi)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(poi.nama, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            itemVerticalAlignment = Alignment.CenterVertically,
                        ) {
                            if (distanceM != null) {
                                Text(s.meters(distanceM), style = HistoryGoTheme.type.angka, color = colors.tertiary)
                            }
                            Text(category, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                            if (!poi.validated) StatusChip(s.notValidatedChip)
                            if (poi.dummy) StatusChip(s.dummyChip)
                        }
                    }
                }

                AdaptiveButtonRow(gap = 10.dp) {
                    if (onListen != null) {
                        Button(
                            onClick = onListen,
                            modifier = Modifier.height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            contentPadding = PaddingValues(start = 12.dp, end = 18.dp),
                        ) {
                            Box(
                                Modifier.size(34.dp).background(extra.playButton, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    painterResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play),
                                    contentDescription = null,
                                    tint = extra.onPlayButton,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                if (isPlaying) s.pauseStory else s.listenStory,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                            )
                        }
                        OutlinedButton(
                            onClick = onOpenDetail,
                            modifier = Modifier.height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.5.dp, colors.primary),
                        ) { Text(s.seeDetails, style = MaterialTheme.typography.labelLarge, maxLines = 1) }
                    } else {
                        Button(
                            onClick = onOpenDetail,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                        ) { Text(s.seeDetails, style = MaterialTheme.typography.labelLarge) }
                    }
                }
            }

            IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).size(48.dp).padding(0.dp)) {
                Icon(painterResource(R.drawable.ic_close), contentDescription = s.close, tint = colors.onSurfaceVariant)
            }
        }
    }
}

/** Foto kecil 76dp di kartu POI; kotak krem kalau foto belum ada. */
@Composable
private fun PoiThumbnail(poi: Poi) {
    val image = rememberAssetBitmap(poi.foto)?.getOrNull()
    val shape = RoundedCornerShape(12.dp)
    if (image != null) {
        Image(
            bitmap = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(76.dp).clip(shape),
        )
    } else {
        Box(Modifier.size(76.dp).clip(shape).background(MaterialTheme.colorScheme.primaryContainer))
    }
}
