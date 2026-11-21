package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.data.AppLanguage
import id.ac.itb.k09.historygo.data.VisitHistoryStore
import id.ac.itb.k09.historygo.i18n.LocalLanguage
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/** Tempat yang kamu lewati (F02 no. 4): dikelompokkan per hari, hapus dengan "Urungkan". */
@Composable
fun HistoryScreen(onBack: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    LaunchedEffect(Unit) { VisitHistoryStore.load(context) }
    val history by VisitHistoryStore.history.collectAsStateWithLifecycle()
    val s = LocalStrings.current
    val language = LocalLanguage.current
    val colors = MaterialTheme.colorScheme
    // Nama di riwayat disimpan dalam bahasa saat itu; tampilkan versi bahasa aktif kalau ada (A01).
    val pois = rememberPois()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Tombol Back di HP kembali ke peta, bukan menutup aplikasi.
    BackHandler(enabled = onBack != null) { onBack?.invoke() }

    val locale = Locale.forLanguageTag(if (language == AppLanguage.EN) "en-US" else "id-ID")
    val timeFormat = remember(language) { SimpleDateFormat(if (language == AppLanguage.EN) "HH:mm" else "HH.mm", locale) }
    val dayFormat = remember(language) { SimpleDateFormat("d MMM yyyy", locale) }
    val grouped = history.groupBy { dayKey(it.timeMillis) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 96.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onBack != null) TextButton(onClick = onBack) { Text(s.back) }
                        Text(s.historyTitle, style = MaterialTheme.typography.headlineLarge, color = colors.onSurface)
                    }
                    Text(s.historySubtitle, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                }
            }

            if (history.isEmpty()) {
                item {
                    Column(Modifier.padding(top = 40.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                            Box(Modifier.size(26.dp).rotate(45f).border(1.5.dp, HistoryGoTheme.colors.ornament))
                        }
                        Text(s.noHistory, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                        Text(s.emptyHistoryHint, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                    }
                }
            }

            grouped.entries.forEachIndexed { groupIndex, (day, records) ->
                item(key = "day-$day") {
                    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (day == dayKey(System.currentTimeMillis())) s.today else dayFormat.format(Date(records.first().timeMillis)),
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                        )
                        // POI di riwayat tidak direkomendasikan lagi (F07 no. 2); hapus riwayat untuk memulai tur baru.
                        if (groupIndex == 0) {
                            TextButton(
                                onClick = {
                                    val backup = history
                                    VisitHistoryStore.clear(context)
                                    scope.launch {
                                        val result = snackbar.showSnackbar(s.historyCleared, actionLabel = s.undo, duration = SnackbarDuration.Long)
                                        if (result == SnackbarResult.ActionPerformed) VisitHistoryStore.restore(context, backup)
                                    }
                                },
                                modifier = Modifier.heightIn(min = 48.dp),
                            ) { Text(s.clearHistory, style = MaterialTheme.typography.labelLarge, color = colors.tertiary) }
                        }
                    }
                }
                items(records, key = { "${it.poiId}-${it.timeMillis}" }) { record ->
                    Row(Modifier.fillMaxWidth().heightIn(min = 72.dp).padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            timeFormat.format(Date(record.timeMillis)),
                            style = HistoryGoTheme.type.angka,
                            color = colors.tertiary,
                            modifier = Modifier.width(52.dp),
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                pois.firstOrNull { it.id == record.poiId }?.nama ?: record.poiNama,
                                style = HistoryGoTheme.type.namaTempat,
                                color = colors.onSurface,
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    painterResource(if (record.background) R.drawable.ic_phone else R.drawable.ic_eye),
                                    null,
                                    tint = colors.onSurfaceVariant,
                                    modifier = Modifier.size(14.dp),
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    if (record.background) s.detectedBackgroundShort else s.detectedForegroundShort,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = colors.outlineVariant)
                }
            }
        }

        SnackbarHost(snackbar, modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp))
    }
}

/** Kunci hari (tahun*1000 + hari ke-n) untuk mengelompokkan riwayat. */
private fun dayKey(millis: Long): Int {
    val c = Calendar.getInstance().apply { timeInMillis = millis }
    return c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR)
}
