package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.PoiFilter
import id.ac.itb.k09.historygo.data.PoiSearch
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.location.TriggerState

/** Layar pencarian dan filter POI (F05). Tetap bisa dipakai tanpa izin lokasi. */
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenDetail: (Poi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val pois = rememberPois()   // dalam bahasa aktif (A01)
    // Posisi terakhir dari TriggerService; null kalau izin lokasi belum ada atau GPS mati.
    val reading by TriggerState.latestReading.collectAsStateWithLifecycle()

    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<PoiFilter?>(null) }

    val current = reading
    val results = remember(pois, query, filter, current) {
        PoiSearch.search(pois, query, filter, current?.lat, current?.lng)
    }

    // Tombol Back di HP kembali ke peta.
    BackHandler(onBack = onBack)

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(s.back) }
            Text(s.searchTitle, style = MaterialTheme.typography.titleLarge)
        }

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text(s.searchHint) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        // Chip filter; bisa digeser ke samping kalau layar sempit.
        Row(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = filter == null,
                onClick = { filter = null },
                label = { Text(s.filterAll) },
            )
            PoiFilter.entries.forEach { option ->
                FilterChip(
                    selected = filter == option,
                    // Ketuk chip yang sudah terpilih = kembali ke "Semua".
                    onClick = { filter = if (filter == option) null else option },
                    label = { Text(s.filterLabel(option)) },
                )
            }
        }

        if (current == null) {
            Text(
                s.noLocationSortedByName,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        if (results.isEmpty()) {
            Text(s.notFound, modifier = Modifier.padding(top = 16.dp))
        } else {
            LazyColumn {
                items(results, key = { it.poi.id }) { result ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDetail(result.poi) }
                            .padding(vertical = 12.dp),
                    ) {
                        Text(result.poi.nama, style = MaterialTheme.typography.titleMedium)
                        Text(result.poi.kategori, style = MaterialTheme.typography.bodySmall)
                        if (result.distanceM != null) {
                            Text(
                                s.distanceAndWalk(result.distanceM, result.walkMinutes ?: 0),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}
