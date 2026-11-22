package id.ac.itb.k09.historygo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.navigation.NavigationState
import id.ac.itb.k09.historygo.ui.components.Diamond
import id.ac.itb.k09.historygo.ui.components.MapSheet
import id.ac.itb.k09.historygo.ui.components.nbsp
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/**
 * Kartu navigasi jalan kaki (F06) di bawah peta. State dari lembar "State: navigasi"
 * (DESIGN.md, Katalog state): berjalan, menghitung, menunggu lokasi, menghitung ulang,
 * gagal, tiba, dan rute beberapa titik (F07).
 */
@Composable
fun NavigationCard(
    nav: NavigationState,
    hasLocation: Boolean,
    onStop: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    // Nama POI dalam bahasa aktif, walau navigasi dimulai sebelum bahasa diganti (A01).
    val pois = rememberPois()
    fun name(poi: Poi) = pois.firstOrNull { it.id == poi.id }?.nama ?: poi.nama

    MapSheet(modifier) {
        if (nav.arrived) {
            ArrivedContent(
                message = if (nav.isMultiStop) s.navAllVisited(nav.stops.size) else s.navArrivedAt(name(nav.destination)),
                onDone = onStop,
            )
            return@MapSheet
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Rute rekomendasi: urutan titik + progres belah ketupat.
            if (nav.isMultiStop) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        s.navRouteHeader(nav.currentStopIndex + 1, nav.stops.size),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.secondary,
                        modifier = Modifier.weight(1f),
                    )
                    nav.stops.indices.forEach { i ->
                        Spacer(Modifier.width(8.dp))
                        val gold = HistoryGoTheme.colors.ornament
                        if (i < nav.currentStopIndex) Diamond(10.dp, gold)
                        else Box(Modifier.size(10.dp).rotate(45f).border(1.5.dp, gold))
                    }
                }
            }

            Text(name(nav.destination), style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)

            // Sudah mencapai satu titik di tengah rute.
            val reached = nav.lastReachedIndex
            if (nav.isMultiStop && reached != null) {
                InfoRow(R.drawable.ic_check, s.navReached(name(nav.stops[reached])), positive = true)
            }

            when {
                nav.error != null -> InfoRow(R.drawable.ic_info, s.routeError(nav.error), positive = false)
                !hasLocation && nav.remainingM == null -> Progress(s.navWaitingLocation)
                nav.loading && nav.route != null -> Progress(s.navRerouting)
                nav.loading || nav.route == null -> Progress(s.navCalculating)
                nav.remainingM != null -> {
                    Row(verticalAlignment = Alignment.Bottom) {
                        val big = if (nav.isMultiStop) nav.toNextStopM ?: nav.remainingM else nav.remainingM
                        Text(s.meters(big).nbsp(), style = HistoryGoTheme.type.angkaBesar, color = colors.onSurface)
                        Spacer(Modifier.width(14.dp))
                        Text(
                            s.walkMinutes(nav.remainingMinutes ?: 0).nbsp(),
                            style = MaterialTheme.typography.bodyLarge,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    if (nav.isMultiStop) {
                        Text(
                            s.navProgressMulti(nav.toNextStopM ?: 0, nav.remainingM, nav.remainingMinutes ?: 0),
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                }
            }

            if (nav.rerouteCount > 0) {
                Text(s.navRerouteCount(nav.rerouteCount), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (nav.error != null) {
                    Button(onClick = onRetry, modifier = Modifier.weight(1f).height(52.dp), shape = MaterialTheme.shapes.medium) {
                        Text(s.retry, style = MaterialTheme.typography.labelLarge)
                    }
                }
                OutlinedButton(
                    onClick = onStop,
                    modifier = (if (nav.error != null) Modifier else Modifier.fillMaxWidth()).height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.5.dp, colors.primary),
                ) { Text(s.endNavigation, style = MaterialTheme.typography.labelLarge, maxLines = 1) }
            }
        }
    }
}

@Composable
private fun ArrivedContent(message: String, onDone: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val extra = HistoryGoTheme.colors
    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(34.dp).rotate(45f).background(extra.markerActiveFill).border(2.dp, extra.markerActiveCore),
                contentAlignment = Alignment.Center,
            ) { Box(Modifier.size(12.dp).background(extra.markerActiveCore)) }
        }
        Text(s.navArrived, style = MaterialTheme.typography.headlineMedium, color = colors.onSurface)
        Text(message, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant, textAlign = TextAlign.Center)
        Button(onClick = onDone, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) {
            Text(s.done, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun Progress(text: String) {
    val colors = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearProgressIndicator(Modifier.fillMaxWidth(), color = colors.secondary, trackColor = colors.secondaryContainer)
        Text(text, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
    }
}

@Composable
private fun InfoRow(icon: Int, text: String, positive: Boolean) {
    val colors = MaterialTheme.colorScheme
    val bg = if (positive) colors.secondaryContainer else colors.errorContainer
    val fg = if (positive) colors.onSecondaryContainer else colors.onErrorContainer
    Row(
        modifier = Modifier.fillMaxWidth().background(bg, MaterialTheme.shapes.medium).padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(painterResource(icon), null, tint = fg, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = fg)
    }
}
