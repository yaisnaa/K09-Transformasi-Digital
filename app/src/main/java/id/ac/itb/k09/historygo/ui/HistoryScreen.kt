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
import id.ac.itb.k09.historygo.data.AppLanguage
import id.ac.itb.k09.historygo.data.VisitHistoryStore
import id.ac.itb.k09.historygo.i18n.LocalLanguage
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Layar daftar "POI yang dilewati" (F02 acceptance 4). */
@Composable
fun HistoryScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { VisitHistoryStore.load(context) }
    val history by VisitHistoryStore.history.collectAsStateWithLifecycle()
    val s = LocalStrings.current
    val language = LocalLanguage.current
    // Nama di riwayat disimpan dalam bahasa saat itu; tampilkan versi bahasa aktif kalau ada (A01).
    val pois = rememberPois()

    // Tombol Back di HP kembali ke peta, bukan menutup aplikasi.
    BackHandler(onBack = onBack)

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(s.back) }
            Text(s.historyTitle, style = MaterialTheme.typography.titleLarge)
        }

        if (history.isEmpty()) {
            Text(s.noHistory, modifier = Modifier.padding(top = 16.dp))
        } else {
            // POI di riwayat tidak direkomendasikan lagi (F07 no. 2); hapus riwayat untuk memulai tur baru.
            TextButton(onClick = { VisitHistoryStore.clear(context) }) { Text(s.clearHistory) }
            // LazyColumn = daftar yang bisa di-scroll dan hemat memori untuk daftar panjang.
            LazyColumn {
                items(history) { record ->
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        Text(
                            pois.firstOrNull { it.id == record.poiId }?.nama ?: record.poiNama,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            formatTime(record.timeMillis, language) +
                                if (record.background) s.detectedInBackground else s.detectedInForeground,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}

private fun formatTime(millis: Long, language: AppLanguage): String {
    val locale = Locale.forLanguageTag(if (language == AppLanguage.EN) "en-US" else "id-ID")
    return SimpleDateFormat("d MMM yyyy, HH:mm", locale).format(Date(millis))
}