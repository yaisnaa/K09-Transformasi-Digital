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
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
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
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.game.GameStore
import id.ac.itb.k09.historygo.game.JejakSejarah

/** Layar "Jejak Sejarah" (F08): progres misi x/N, poin, badge, dan status tiap POI. */
@Composable
fun GameScreen(onBack: () -> Unit, onOpenDetail: (Poi) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val s = LocalStrings.current
    val pois = rememberPois().filter { it.hasCoordinates }   // dalam bahasa aktif (A01)
    LaunchedEffect(Unit) { GameStore.load(context) }
    val progress by GameStore.progress.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    val done = JejakSejarah.completedCount(progress, pois)
    val badge = JejakSejarah.badgeUnlocked(progress, pois)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text(s.back) }
            Text(s.historyTrail, style = MaterialTheme.typography.titleLarge)
        }
        Text(
            s.gameIntro,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(16.dp))
        // F08 no. 2: progres misi x/N.
        Text(s.missionsDone(done, pois.size), style = MaterialTheme.typography.titleMedium)
        LinearProgressIndicator(
            progress = { if (pois.isEmpty()) 0f else done.toFloat() / pois.size },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
        Text(s.points(progress.points), style = MaterialTheme.typography.titleMedium)

        Spacer(Modifier.height(12.dp))
        // F08 no. 3: badge terbuka setelah semua misi selesai.
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(if (badge) s.badgeUnlocked else s.badgeLocked, style = MaterialTheme.typography.labelLarge)
                Text(s.badgeName, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (badge) s.badgeUnlockedText else s.badgeLockedText(pois.size - done),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        pois.forEach { poi ->
            val checkedIn = poi.id in progress.checkedIn
            val answered = progress.answers[poi.id]
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenDetail(poi) }
                    .padding(vertical = 10.dp),
            ) {
                Text(
                    (if (JejakSejarah.isMissionComplete(progress, poi)) "✓ " else "○ ") + poi.nama,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    when {
                        !checkedIn -> s.statusNotCheckedIn
                        answered == null -> s.statusQuizPending
                        answered -> s.statusQuizCorrect
                        else -> s.statusQuizWrong
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            HorizontalDivider()
        }

        Spacer(Modifier.height(12.dp))
        TextButton(onClick = { GameStore.reset(context) }) { Text(s.resetMissions) }
    }
}
