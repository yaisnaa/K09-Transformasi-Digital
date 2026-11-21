package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.data.PoiFilter
import id.ac.itb.k09.historygo.data.PoiSearch
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.location.TriggerState
import id.ac.itb.k09.historygo.ui.components.Diamond
import id.ac.itb.k09.historygo.ui.components.StatusChip
import id.ac.itb.k09.historygo.ui.components.nbsp
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/** Cari tempat (F05): kata kunci parsial, filter kategori, hasil terdekat dulu dengan jarak dan waktu. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onOpenDetail: (Poi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
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

    Column(modifier = modifier.fillMaxSize()) {
        // ----- Kembali + kolom pencarian -----
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = s.back, tint = colors.primary)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text(s.searchHint, color = colors.onSurfaceVariant) },
                leadingIcon = { Icon(painterResource(R.drawable.ic_search), null, tint = colors.primary, modifier = Modifier.size(20.dp)) },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.bodyLarge,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surfaceContainer,
                    unfocusedContainerColor = colors.surfaceContainer,
                    unfocusedBorderColor = colors.outlineVariant,
                    focusedBorderColor = colors.primary,
                    cursorColor = colors.primary,
                ),
                modifier = Modifier.weight(1f),
            )
        }

        // ----- Filter kategori -----
        FlowRow(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CategoryChip(s.filterAll, selected = filter == null) { filter = null }
            PoiFilter.entries.forEach { option ->
                // Ketuk chip yang sudah terpilih = kembali ke "Semua".
                CategoryChip(s.filterLabel(option), selected = filter == option) {
                    filter = if (filter == option) null else option
                }
            }
        }

        // ----- Hasil -----
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        ) {
            item {
                Text(
                    if (current == null) s.noLocationSortedByName else s.resultsCount(results.size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 6.dp),
                )
            }
            if (results.isEmpty()) {
                item {
                    Column(Modifier.padding(top = 32.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(s.notFound, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                        Text(s.notFoundHint, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
                    }
                }
            }
            items(results, key = { it.poi.id }) { result ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 76.dp)
                        .clickable(role = Role.Button) { onOpenDetail(result.poi) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                        Diamond(12.dp, HistoryGoTheme.colors.markerFill)
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(result.poi.nama, style = HistoryGoTheme.type.namaTempat, color = colors.onSurface)
                        if (result.distanceM != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(s.meters(result.distanceM).nbsp(), style = HistoryGoTheme.type.angka, color = colors.onSurface)
                                Text(
                                    " · " + s.walkMinutes(result.walkMinutes ?: 0).nbsp(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = colors.onSurfaceVariant,
                                )
                            }
                        }
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), itemVerticalAlignment = Alignment.CenterVertically) {
                            Text(result.poi.kategori, style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                            if (result.poi.dummy) StatusChip(s.dummyChip)
                        }
                    }
                }
                HorizontalDivider(color = colors.outlineVariant)
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.bodyMedium) },
        leadingIcon = if (selected) {
            { Icon(painterResource(R.drawable.ic_check), null, modifier = Modifier.size(16.dp)) }
        } else null,
        shape = MaterialTheme.shapes.small,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = colors.primary,
            selectedLabelColor = colors.onPrimary,
            selectedLeadingIconColor = colors.onPrimary,
            labelColor = colors.primary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = colors.outline,
            selectedBorderColor = colors.primary,
        ),
        modifier = Modifier.heightIn(min = 40.dp),
    )
}
