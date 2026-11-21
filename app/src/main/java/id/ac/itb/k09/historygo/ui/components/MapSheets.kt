package id.ac.itb.k09.historygo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/** Status deteksi yang tampil sebagai chip di bar atas peta. */
enum class MapStatus { DETECTING, SEARCHING, LOCATION_OFF, DETECTION_OFF }

/** Bayangan lembut sepia untuk elemen yang mengambang di atas peta (DESIGN.md, Elevation). */
@Composable
private fun Modifier.floating(elevation: Dp): Modifier {
    val shadow = HistoryGoTheme.colors.shadow
    return this.shadow(elevation, MaterialTheme.shapes.large, ambientColor = shadow, spotColor = shadow)
}

/** Kartu/sheet di atas peta: latar Kartu, garis tipis, sudut 20dp, bayangan lembut. */
@Composable
fun MapSheet(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth().floating(10.dp),
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

/** Bar atas peta: nama aplikasi, chip status, tombol Pengaturan. */
@Composable
fun MapTopBar(status: MapStatus, onOpenSettings: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth().height(56.dp).floating(3.dp),
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(modifier = Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("HistoryGo", style = MaterialTheme.typography.headlineSmall, color = colors.onSurface, modifier = Modifier.weight(1f))
            when (status) {
                MapStatus.DETECTING -> StatusPill(s.chipDetecting, colors.secondaryContainer, colors.onSecondaryContainer) {
                    Box(Modifier.size(6.dp).background(colors.onSecondaryContainer, CircleShape))
                }
                MapStatus.SEARCHING -> StatusPill(s.searchingLocation, colors.primaryContainer, colors.onPrimaryContainer) {
                    CircularProgressIndicator(modifier = Modifier.size(10.dp), strokeWidth = 1.5.dp, color = colors.onPrimaryContainer)
                }
                MapStatus.LOCATION_OFF -> StatusPill(s.chipLocationOff, colors.errorContainer, colors.onErrorContainer) {
                    Icon(painterResource(R.drawable.ic_location_off), null, tint = colors.onErrorContainer, modifier = Modifier.size(14.dp))
                }
                MapStatus.DETECTION_OFF -> StatusPill(s.chipDetectionOff, colors.primaryContainer, colors.onPrimaryContainer) {}
            }
            IconButton(onClick = onOpenSettings, modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_settings_tune), contentDescription = s.settingsTitle, tint = colors.primary)
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    leading: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier
            .background(container, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        leading()
        Text(text, style = MaterialTheme.typography.labelMedium, color = content, maxLines = 1)
    }
}

/** Search bar di atas peta. Mengetuknya membuka layar Cari (F05). */
@Composable
fun MapSearchButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.fillMaxWidth().height(52.dp).floating(3.dp),
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
        onClick = onClick,
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(R.drawable.ic_search), contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(s.searchPlaceholder, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, maxLines = 1)
        }
    }
}

/** Tombol "Kembali ke posisi saya" (56dp) di atas sheet. */
@Composable
fun RecenterButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    Surface(
        modifier = modifier.size(56.dp).floating(4.dp),
        shape = MaterialTheme.shapes.medium,
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
        onClick = onClick,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(R.drawable.ic_locate), contentDescription = s.recenter, tint = colors.secondary)
        }
    }
}

/** Sheet izin lokasi ditolak / GPS mati: judul serif, penjelasan, aksi utama, dan jalan alternatif. */
@Composable
fun LocationProblemSheet(
    title: String,
    body: String,
    actionText: String,
    onAction: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    MapSheet(modifier) {
        Text(title, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
        Spacer(Modifier.height(10.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onAction, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) {
            Text(actionText, style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text(s.exploreWithoutLocation, style = MaterialTheme.typography.labelLarge, color = colors.tertiary)
        }
    }
}

/** Pita "Mendekati …" (state prototype PRD "mendekati POI"). */
@Composable
fun ApproachStrip(name: String, distanceM: Int, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val gold = HistoryGoTheme.colors.ornament
    MapSheet(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier.size(26.dp).rotate(45f).border(1.5.dp, gold),
                    contentAlignment = Alignment.Center,
                ) { Box(Modifier.size(10.dp).background(HistoryGoTheme.colors.markerFill)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(s.approaching(name), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                Text(s.approachingHint, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
            Spacer(Modifier.width(12.dp))
            Text(s.meters(distanceM), style = HistoryGoTheme.type.angka, color = colors.tertiary)
        }
    }
}

/** Sheet "di luar area": bangunan terdekat, arahkan ke sana, dan pintu ke rute rekomendasi. */
@Composable
fun IdleSheet(
    nearestName: String?,
    nearestDistanceM: Int?,
    nearestWalkMinutes: Int?,
    poisWithoutCoordinates: Int,
    onDirections: (() -> Unit)?,
    onOpenRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    MapSheet(modifier) {
        Text(s.idleTitle, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
        if (nearestName != null && nearestDistanceM != null && nearestWalkMinutes != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                buildAnnotatedString {
                    append(s.nearestPlace(nearestName))
                    append(",\n")
                    // Pasangan jarak dan waktu tidak boleh terpotong baris (DESIGN.md, Layout):
                    // dirangkai dengan spasi tak-terputus.
                    withStyle(SpanStyle(color = colors.onSurface)) {
                        withStyle(HistoryGoTheme.type.angka.toSpanStyle()) { append(s.meters(nearestDistanceM).nbsp()) }
                        append(" · ")
                        append(s.walkMinutes(nearestWalkMinutes).nbsp())
                    }
                },
                style = MaterialTheme.typography.bodyLarge,
                color = colors.onSurfaceVariant,
            )
        }
        if (poisWithoutCoordinates > 0) {
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(painterResource(R.drawable.ic_info), null, tint = colors.tertiary, modifier = Modifier.size(18.dp).padding(top = 1.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.poisWithoutCoordinates(poisWithoutCoordinates), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
            }
        }
        if (onDirections != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onDirections,
                modifier = Modifier.height(48.dp),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(1.5.dp, colors.primary),
            ) {
                Icon(painterResource(R.drawable.ic_navigate), null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.directionsThere, style = MaterialTheme.typography.labelLarge)
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(1.dp).background(colors.primaryContainer))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clickable(role = Role.Button, onClick = onOpenRoute),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(s.freeTimeRoute, style = MaterialTheme.typography.titleSmall, color = colors.tertiary, modifier = Modifier.weight(1f))
            Icon(painterResource(R.drawable.ic_chevron_right), null, tint = colors.tertiary, modifier = Modifier.size(18.dp))
        }
    }
}

/** Ganti spasi biasa dengan spasi tak-terputus, agar teks pendek tidak dipotong ke baris baru. */
fun String.nbsp(): String = replace(' ', ' ')

/** Peringatan khusus pengembang: MAPS_API_KEY kosong. */
@Composable
fun DevWarning(text: String, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().background(colors.errorContainer, MaterialTheme.shapes.medium).padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(painterResource(R.drawable.ic_info), null, tint = colors.onErrorContainer, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = colors.onErrorContainer)
    }
}
