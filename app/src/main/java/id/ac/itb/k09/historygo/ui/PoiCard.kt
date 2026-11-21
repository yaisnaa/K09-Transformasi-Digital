package id.ac.itb.k09.historygo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.i18n.LocalStrings

/** Kartu pop-up saat POI terpicu dan aplikasi sedang aktif (F02, UI02). */
@Composable
fun PoiCard(
    poi: Poi,
    onClose: () -> Unit,
    onOpenDetail: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(s.youAreNear, style = MaterialTheme.typography.labelMedium)
            Text(poi.nama, style = MaterialTheme.typography.titleLarge)
            Text(poi.kategori, style = MaterialTheme.typography.bodyMedium)

            Spacer(Modifier.height(8.dp))

            Text(
                poi.ringkasan ?: s.noSummary,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,   // kartu hanya cuplikan; teks lengkap ada di halaman detail
            )
            if (!poi.validated) {
                Text(s.notValidated, style = MaterialTheme.typography.labelSmall)
            }
            if (poi.dummy) {
                Text(s.dummyShort, style = MaterialTheme.typography.labelSmall)
            }

            // Tombol Tutup di kiri, Lihat detail di kanan.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                TextButton(onClick = onClose) { Text(s.close) }
                Button(onClick = onOpenDetail) { Text(s.seeDetails) }
            }
        }
    }
}
