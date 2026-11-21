package id.ac.itb.k09.historygo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.navigation.NavigationState

/** Panel navigasi di atas peta (F06): tujuan, sisa jarak dan waktu, status tiba, hitung ulang rute. */
@Composable
fun NavigationCard(
    nav: NavigationState,
    hasLocation: Boolean,
    onStop: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    // Nama POI dalam bahasa aktif, walau navigasi dimulai sebelum bahasa diganti (A01).
    val pois = rememberPois()
    fun name(poi: Poi) = pois.firstOrNull { it.id == poi.id }?.nama ?: poi.nama

    Card(modifier = modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            val header = when {
                nav.arrived -> s.navArrived
                // Rute rekomendasi: tampilkan urutan titik, mis. "Rute rekomendasi · 2 dari 3".
                nav.isMultiStop -> s.navRouteHeader(nav.currentStopIndex + 1, nav.stops.size)
                else -> s.navWalkingTo
            }
            Text(header, style = MaterialTheme.typography.labelMedium)
            Text(name(nav.destination), style = MaterialTheme.typography.titleMedium)

            // Setelah mencapai satu titik di tengah rute, beri tahu lalu lanjut ke titik berikutnya.
            val reached = nav.lastReachedIndex
            if (nav.isMultiStop && !nav.arrived && reached != null) {
                Text(s.navReached(name(nav.stops[reached])), style = MaterialTheme.typography.labelSmall)
            }

            val status = when {
                nav.arrived && nav.isMultiStop -> s.navAllVisited(nav.stops.size)
                nav.arrived -> s.navArrivedAt(name(nav.destination))
                nav.error != null -> s.routeError(nav.error)
                !hasLocation && nav.remainingM == null -> s.navWaitingLocation
                nav.loading && nav.route != null -> s.navRerouting
                nav.loading || nav.route == null -> s.navCalculating
                nav.remainingM != null && nav.isMultiStop ->
                    s.navProgressMulti(nav.toNextStopM ?: 0, nav.remainingM, nav.remainingMinutes ?: 0)
                nav.remainingM != null -> s.navProgress(nav.remainingM, nav.remainingMinutes ?: 0)
                else -> s.navCalculating
            }
            Text(status, style = MaterialTheme.typography.bodyMedium)

            if (nav.rerouteCount > 0 && !nav.arrived) {
                Text(s.navRerouteCount(nav.rerouteCount), style = MaterialTheme.typography.labelSmall)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (nav.error != null) {
                    TextButton(onClick = onRetry) { Text(s.retry) }
                }
                TextButton(onClick = onStop) { Text(if (nav.arrived) s.done else s.endNavigation) }
            }
        }
    }
}
