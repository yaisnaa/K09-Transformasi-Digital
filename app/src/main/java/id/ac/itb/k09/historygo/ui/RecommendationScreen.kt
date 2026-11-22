package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.data.AppLanguage
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.PoiRecommender
import id.ac.itb.k09.historygo.data.ProfileStore
import id.ac.itb.k09.historygo.data.VisitHistoryStore
import id.ac.itb.k09.historygo.i18n.LocalLanguage
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.location.TriggerState
import id.ac.itb.k09.historygo.ui.components.nbsp
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Rute rekomendasi (F07, CL-02): maksimal 3 tempat sebagai satu rute berurutan.
 * Waktu ditampilkan sebagai jam perkiraan tiba (critique v2), bukan "menit ke-X".
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RecommendationScreen(
    onBack: (() -> Unit)? = null,
    onOpenDetail: (Poi) -> Unit,
    onEditProfile: () -> Unit,
    onStartRoute: (List<Poi>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val s = LocalStrings.current
    val language = LocalLanguage.current
    val colors = MaterialTheme.colorScheme
    val pois = rememberPois()   // dalam bahasa aktif (A01)
    LaunchedEffect(Unit) { VisitHistoryStore.load(context) }
    val profile by ProfileStore.profile.collectAsStateWithLifecycle()
    val history by VisitHistoryStore.history.collectAsStateWithLifecycle()
    val reading by TriggerState.latestReading.collectAsStateWithLifecycle()

    BackHandler(enabled = onBack != null) { onBack?.invoke() }

    // Jam mulai rute = saat layar dibuka. Format jam mengikuti bahasa (14.07 / 14:07).
    val startTime = remember { LocalTime.now() }
    val timeFormat = remember(language) { DateTimeFormatter.ofPattern(if (language == AppLanguage.EN) "HH:mm" else "HH.mm") }
    fun eta(minutesFromNow: Int) = startTime.plusMinutes(minutesFromNow.toLong()).format(timeFormat)

    val currentProfile = profile
    val current = reading
    val visitedIds = history.map { it.poiId }.toSet()
    val recommendations = remember(pois, currentProfile, current, visitedIds) {
        currentProfile?.let { PoiRecommender.recommend(pois, it, current?.lat, current?.lng, visitedIds) }.orEmpty()
    }

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) TextButton(onClick = onBack) { Text(s.back) }
                Text(s.recTitle, style = MaterialTheme.typography.headlineLarge, color = colors.onSurface)
            }

            if (currentProfile == null) {
                EmptyRouteCard(text = s.recNeedProfile, action = s.fillProfile, primary = true, onAction = onEditProfile)
                return@Column
            }

            // ----- Ringkasan profil -----
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    s.minutes(currentProfile.durationMinutes) + " · " + currentProfile.interests.joinToString(", ") { s.filterLabel(it) },
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.onPrimaryContainer,
                    modifier = Modifier.background(colors.primaryContainer, MaterialTheme.shapes.small).padding(horizontal = 14.dp, vertical = 9.dp),
                )
                Spacer(Modifier.width(4.dp))
                TextButton(onClick = onEditProfile, modifier = Modifier.height(48.dp)) {
                    Text(s.change, style = MaterialTheme.typography.labelLarge, color = colors.tertiary)
                }
            }

            // ----- Catatan kontekstual -----
            if (current == null) InfoNote(R.drawable.ic_location_off, s.recNoLocation)
            if (visitedIds.isNotEmpty()) InfoNote(R.drawable.ic_nav_history, s.recVisitedExcluded(visitedIds.size))

            if (recommendations.isEmpty()) {
                EmptyRouteCard(
                    text = s.recEmpty(currentProfile.durationMinutes),
                    action = s.changeDuration,
                    primary = false,
                    onAction = onEditProfile,
                )
                return@Column
            }

            Text(s.recOrderNote, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)

            // ----- Timeline belah ketupat -----
            Column {
                recommendations.forEachIndexed { index, rec ->
                    val last = index == recommendations.lastIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .clickable(role = Role.Button) { onOpenDetail(rec.poi) },
                    ) {
                        // Nomor urut + garis titik-titik ke tempat berikutnya.
                        Column(Modifier.width(40.dp).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.padding(top = 6.dp).size(34.dp), contentAlignment = Alignment.Center) {
                                Box(
                                    Modifier.size(24.dp).rotate(45f)
                                        .background(HistoryGoTheme.colors.markerFill)
                                        .border(2.dp, HistoryGoTheme.colors.ornament),
                                )
                                Text((index + 1).toString(), style = HistoryGoTheme.type.angka, color = colors.onPrimary)
                            }
                            if (!last) {
                                val gold = HistoryGoTheme.colors.ornament
                                Canvas(Modifier.weight(1f).width(2.dp).padding(vertical = 6.dp)) {
                                    drawLine(
                                        color = gold,
                                        start = Offset(size.width / 2, 0f),
                                        end = Offset(size.width / 2, size.height),
                                        strokeWidth = size.width,
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 6f)),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(
                            modifier = Modifier.weight(1f).padding(bottom = if (last) 0.dp else 22.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(rec.poi.nama, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                            Text(
                                if (rec.matchesInterest) s.recMatchesInterest else s.recFiller,
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.onPrimaryContainer,
                                modifier = Modifier.background(colors.primaryContainer, MaterialTheme.shapes.extraSmall).padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row {
                                    Text(s.meters(rec.distanceM).nbsp(), style = HistoryGoTheme.type.angka, color = colors.onSurface)
                                    Text(
                                        " · " + s.walkMinutes(rec.walkMinutes).nbsp(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = colors.onSurfaceVariant,
                                    )
                                }
                                Text(
                                    s.etaAt(eta(rec.cumulativeMinutes)).nbsp(),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = colors.onSurface,
                                )
                            }
                        }
                    }
                }
            }
            Text(s.recVisitNote(PoiRecommender.VISIT_MINUTES_PER_POI), style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
        }

        // ----- Ringkasan + mulai rute, tetap di bawah -----
        if (currentProfile != null && recommendations.isNotEmpty()) {
            Surface(color = colors.surfaceContainer) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        s.recFinish(eta(recommendations.last().cumulativeMinutes), recommendations.last().cumulativeMinutes, currentProfile.durationMinutes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                    // Satu rute melewati semua rekomendasi: titik awal -> 1 -> 2 -> 3.
                    Button(
                        onClick = { onStartRoute(recommendations.map { it.poi }) },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) { Text(s.startRoute(recommendations.size), style = MaterialTheme.typography.labelLarge) }
                }
                HorizontalDivider(color = colors.outlineVariant)
            }
        }
    }
}

@Composable
private fun InfoNote(icon: Int, text: String) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().background(colors.primaryContainer, MaterialTheme.shapes.medium).padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(painterResource(icon), null, tint = colors.onPrimaryContainer, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = colors.onPrimaryContainer)
    }
}

/** State kosong: belah ketupat putus-putus + satu kalimat + satu aksi (DESIGN.md, Katalog state). */
@Composable
private fun EmptyRouteCard(text: String, action: String, primary: Boolean, onAction: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainer,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) {
                Box(Modifier.size(26.dp).rotate(45f).border(1.5.dp, HistoryGoTheme.colors.ornament))
            }
            Text(text, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
            if (primary) {
                Button(onClick = onAction, modifier = Modifier.fillMaxWidth().height(52.dp), shape = MaterialTheme.shapes.medium) {
                    Text(action, style = MaterialTheme.typography.labelLarge)
                }
            } else {
                OutlinedButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = MaterialTheme.shapes.medium,
                    border = BorderStroke(1.5.dp, colors.primary),
                ) { Text(action, style = MaterialTheme.typography.labelLarge) }
            }
        }
    }
}
