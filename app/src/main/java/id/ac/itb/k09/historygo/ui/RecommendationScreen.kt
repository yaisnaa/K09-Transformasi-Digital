package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.PoiRecommender
import id.ac.itb.k09.historygo.data.ProfileStore
import id.ac.itb.k09.historygo.data.VisitHistoryStore
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.location.TriggerState

/** Daftar rekomendasi (F07): maksimal 3 POI berurutan sebagai rute, sesuai profil dan durasi. */
@Composable
fun RecommendationScreen(
    onBack: () -> Unit,
    onOpenDetail: (Poi) -> Unit,
    onEditProfile: () -> Unit,
    onStartRoute: (List<Poi>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val s = LocalStrings.current
    val pois = rememberPois()   // dalam bahasa aktif (A01)
    LaunchedEffect(Unit) { VisitHistoryStore.load(context) }
    val profile by ProfileStore.profile.collectAsStateWithLifecycle()
    val history by VisitHistoryStore.history.collectAsStateWithLifecycle()
    val reading by TriggerState.latestReading.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(s.back) }
            Text(s.recTitle, style = MaterialTheme.typography.titleLarge)
        }

        val currentProfile = profile
        if (currentProfile == null) {
            Text(s.recNeedProfile)
            Spacer(Modifier.height(12.dp))
            Button(onClick = onEditProfile) { Text(s.fillProfile) }
            return@Column
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                s.minutes(currentProfile.durationMinutes) + " · " + currentProfile.interests.joinToString { s.filterLabel(it) },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onEditProfile) { Text(s.change) }
        }

        val current = reading
        val visitedIds = history.map { it.poiId }.toSet()
        val recommendations = remember(pois, currentProfile, current, visitedIds) {
            PoiRecommender.recommend(pois, currentProfile, current?.lat, current?.lng, visitedIds)
        }

        if (current == null) {
            Text(
                s.recNoLocation,
                style = MaterialTheme.typography.bodySmall,
            )
        }
        if (visitedIds.isNotEmpty()) {
            Text(
                s.recVisitedExcluded(visitedIds.size),
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(Modifier.height(8.dp))
        if (recommendations.isEmpty()) {
            Text(
                s.recEmpty(currentProfile.durationMinutes),
                modifier = Modifier.padding(top = 8.dp),
            )
            return@Column
        }

        recommendations.forEachIndexed { index, rec ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetail(rec.poi) }
                    .padding(vertical = 12.dp),
            ) {
                Text("${index + 1}. ${rec.poi.nama}", style = MaterialTheme.typography.titleMedium)
                Text(
                    s.recLine(rec.distanceM, rec.walkMinutes, rec.cumulativeMinutes),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    if (rec.matchesInterest) s.recMatchesInterest else s.recFiller,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            HorizontalDivider()
        }

        Spacer(Modifier.height(12.dp))
        Text(
            s.recTotal(
                recommendations.last().cumulativeMinutes,
                currentProfile.durationMinutes,
                PoiRecommender.VISIT_MINUTES_PER_POI,
            ),
            style = MaterialTheme.typography.bodySmall,
        )
        Spacer(Modifier.height(12.dp))
        // Satu rute melewati semua rekomendasi: titik awal -> 1 -> 2 -> 3.
        Button(onClick = { onStartRoute(recommendations.map { it.poi }) }) {
            Text(s.startRoute(recommendations.size))
        }
    }
}
