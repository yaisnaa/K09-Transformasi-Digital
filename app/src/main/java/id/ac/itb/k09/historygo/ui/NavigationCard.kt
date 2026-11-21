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
    Card(modifier = modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                if (nav.arrived) "Tiba di tujuan" else "Navigasi jalan kaki ke",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(nav.destination.nama, style = MaterialTheme.typography.titleMedium)

            val status = when {
                nav.arrived -> "Kamu sudah sampai di ${nav.destination.nama}."
                nav.error != null -> "Gagal mengambil rute. ${nav.error}"
                !hasLocation && nav.remainingM == null -> "Menunggu lokasi… pastikan izin lokasi dan GPS aktif."
                nav.loading && nav.route != null -> "Kamu keluar dari rute, menghitung ulang…"
                nav.loading || nav.route == null -> "Menghitung rute jalan kaki…"
                nav.remainingM != null -> "Sisa ${nav.remainingM} m · ±${nav.remainingMinutes} menit"
                else -> "Menghitung rute jalan kaki…"
            }
            Text(status, style = MaterialTheme.typography.bodyMedium)

            if (nav.rerouteCount > 0 && !nav.arrived) {
                Text("Rute sudah dihitung ulang ${nav.rerouteCount}×", style = MaterialTheme.typography.labelSmall)
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (nav.error != null) {
                    TextButton(onClick = onRetry) { Text("Coba lagi") }
                }
                TextButton(onClick = onStop) { Text(if (nav.arrived) "Selesai" else "Akhiri navigasi") }
            }
        }
    }
}
