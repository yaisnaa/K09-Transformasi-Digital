package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.data.VisitHistoryStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Layar daftar "POI yang dilewati" (F02 acceptance 4). */
@Composable
fun HistoryScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { VisitHistoryStore.load(context) }
    val history by VisitHistoryStore.history.collectAsStateWithLifecycle()

    // Tombol Back di HP kembali ke peta, bukan menutup aplikasi.
    BackHandler(onBack = onBack)

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Kembali") }
            Text("POI yang dilewati", style = MaterialTheme.typography.titleLarge)
        }

        if (history.isEmpty()) {
            Text("Belum ada POI yang dilewati.", modifier = Modifier.padding(top = 16.dp))
        } else {
            // LazyColumn = daftar yang bisa di-scroll dan hemat memori untuk daftar panjang.
            LazyColumn {
                items(history) { record ->
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(record.poiNama, style = MaterialTheme.typography.titleMedium)
                        Text(
                            formatTime(record.timeMillis) +
                                    if (record.background) " · terdeteksi di latar" else " · terdeteksi saat aplikasi terbuka",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun formatTime(millis: Long): String =
    SimpleDateFormat("d MMM yyyy, HH:mm", Locale.forLanguageTag("id-ID")).format(Date(millis))