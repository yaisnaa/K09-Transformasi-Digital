package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.game.GameStore
import id.ac.itb.k09.historygo.game.JejakSejarah
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.ui.components.Diamond
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/** Jejak Sejarah (F08): progres misi x/N, poin, badge, daftar misi. */
@Composable
fun GameScreen(onBack: (() -> Unit)? = null, onOpenDetail: (Poi) -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val extra = HistoryGoTheme.colors
    val pois = rememberPois().filter { it.hasCoordinates }   // dalam bahasa aktif (A01)
    LaunchedEffect(Unit) { GameStore.load(context) }
    val progress by GameStore.progress.collectAsStateWithLifecycle()
    var confirmReset by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = onBack != null) { onBack?.invoke() }

    val done = JejakSejarah.completedCount(progress, pois)
    val badge = JejakSejarah.badgeUnlocked(progress, pois)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onBack != null) TextButton(onClick = onBack) { Text(s.back) }
                Text(s.historyTrail, style = MaterialTheme.typography.headlineLarge, color = colors.onSurface)
            }
            Text(s.gameIntro, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        }

        // ----- Progres x/N dan poin (F08 no. 2-3) -----
        Column(
            modifier = Modifier.fillMaxWidth().background(colors.primary, MaterialTheme.shapes.large).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text("$done/${pois.size}", style = HistoryGoTheme.type.angkaBesar, color = colors.onPrimary)
                Spacer(Modifier.width(8.dp))
                Text(s.missionsDoneLabel, style = MaterialTheme.typography.bodyLarge, color = colors.onPrimary.copy(alpha = 0.85f), modifier = Modifier.weight(1f).padding(bottom = 4.dp))
                Text(s.pointsLabel(progress.points), style = HistoryGoTheme.type.angka, color = colors.inversePrimary, modifier = Modifier.padding(bottom = 4.dp))
            }
            // Belah ketupat per misi; jumlahnya sudah dibacakan lewat angka x/N di atas.
            Row(Modifier.fillMaxWidth().clearAndSetSemantics { }, horizontalArrangement = Arrangement.SpaceBetween) {
                pois.indices.forEach { i ->
                    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
                        if (i < done) Diamond(14.dp, extra.ornament)
                        else Box(Modifier.size(14.dp).rotate(45f).border(1.5.dp, extra.ornament))
                    }
                }
            }
        }

        // ----- Badge (F08 no. 3) -----
        Surface(
            shape = MaterialTheme.shapes.large,
            color = colors.surfaceContainer,
            border = BorderStroke(if (badge) 1.5.dp else 1.dp, if (badge) extra.ornament else colors.outlineVariant),
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                BadgeEmblem(unlocked = badge)
                Spacer(Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!badge) {
                            Icon(painterResource(R.drawable.ic_lock), null, tint = colors.onSurfaceVariant, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(
                            if (badge) s.badgeUnlocked else s.badgeLocked,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (badge) colors.tertiary else colors.onSurfaceVariant,
                        )
                    }
                    Text(s.badgeName, style = MaterialTheme.typography.headlineSmall, color = colors.onSurface)
                    Text(
                        if (badge) s.badgeUnlockedText else s.badgeLockedText(pois.size - done),
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.onSurfaceVariant,
                    )
                }
            }
        }

        // ----- Daftar misi -----
        Column {
            pois.forEach { poi ->
                val checkedIn = poi.id in progress.checkedIn
                val answered = progress.answers[poi.id]
                val (icon, bg, fg) = when {
                    JejakSejarah.isMissionComplete(progress, poi) -> Triple(R.drawable.ic_check, colors.secondaryContainer, colors.onSecondaryContainer)
                    checkedIn -> Triple(R.drawable.ic_question, colors.primaryContainer, colors.tertiary)
                    else -> Triple(R.drawable.ic_pin, colors.background, colors.outline)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .clickable(role = Role.Button) { onOpenDetail(poi) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(Modifier.size(36.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(painterResource(icon), null, tint = fg, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(14.dp))
                    Column(Modifier.weight(1f)) {
                        Text(poi.nama, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                        Text(
                            when {
                                !checkedIn -> s.statusNotCheckedIn
                                answered == null -> s.statusQuizPending
                                answered -> s.statusQuizCorrect
                                else -> s.statusQuizWrong
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.onSurfaceVariant,
                        )
                    }
                    Icon(painterResource(R.drawable.ic_chevron_right), null, tint = colors.outline, modifier = Modifier.size(18.dp))
                }
                HorizontalDivider(color = colors.outlineVariant)
            }
        }

        TextButton(onClick = { confirmReset = true }, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(s.resetMissions, style = MaterialTheme.typography.labelLarge, color = colors.error)
        }
    }

    // Ulangi misi menghapus semua progres, jadi minta konfirmasi dulu.
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(s.resetConfirmTitle, style = MaterialTheme.typography.headlineSmall) },
            text = { Text(s.resetConfirmBody, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = { GameStore.reset(context); confirmReset = false }) {
                    Text(s.resetMissions, color = colors.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text(s.cancel) } },
            containerColor = colors.surfaceContainer,
        )
    }
}

/** Emblem belah ketupat bertingkat: garis abu saat terkunci, emas penuh saat terbuka. */
@Composable
private fun BadgeEmblem(unlocked: Boolean) {
    val colors = MaterialTheme.colorScheme
    val extra = HistoryGoTheme.colors
    val line = if (unlocked) extra.markerActiveCore else colors.outline
    Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(44.dp).rotate(45f)
                .background(if (unlocked) extra.markerActiveFill else colors.surfaceContainer)
                .border(2.dp, line),
            contentAlignment = Alignment.Center,
        ) {
            Box(Modifier.size(26.dp).border(1.5.dp, line), contentAlignment = Alignment.Center) {
                Box(Modifier.size(10.dp).background(line))
            }
        }
    }
}
