package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.data.PoiFilter
import id.ac.itb.k09.historygo.data.PoiRecommender
import id.ac.itb.k09.historygo.data.UserProfile
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.ui.components.DecoDivider

/** Kuesioner profil (F07): durasi kunjungan dan minat. */
@Composable
fun ProfileScreen(
    initial: UserProfile?,
    onSave: (UserProfile) -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var duration by rememberSaveable { mutableStateOf(initial?.durationMinutes ?: 60) }
    // Disimpan sebagai daftar nama enum supaya bisa bertahan saat layar diputar.
    var interestNames by rememberSaveable {
        mutableStateOf(initial?.interests?.map { it.name } ?: emptyList())
    }
    val interests = interestNames.map { PoiFilter.valueOf(it) }

    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme

    // Tombol Back HP sama dengan "Lewati".
    BackHandler(onBack = onSkip)

    Column(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 32.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.profileTitle, style = MaterialTheme.typography.displaySmall, color = colors.onSurface)
                DecoDivider()
                Text(s.profileIntro, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }

            // ----- Durasi: satu pilihan -----
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(s.profileDurationQuestion, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                Row(Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PoiRecommender.DURATION_OPTIONS.forEach { minutes ->
                        val selected = duration == minutes
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .background(if (selected) colors.primary else colors.surfaceContainer, MaterialTheme.shapes.medium)
                                .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, MaterialTheme.shapes.medium)
                                .selectable(selected = selected, role = Role.RadioButton) { duration = minutes },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(
                                minutes.toString(),
                                style = MaterialTheme.typography.headlineSmall,
                                color = if (selected) colors.onPrimary else colors.onSurface,
                            )
                            Text(
                                s.minutesUnit,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) colors.onPrimary else colors.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            // ----- Minat: boleh lebih dari satu -----
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(s.profileInterestQuestion, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
                Text(s.interestMultiHint, style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                PoiFilter.entries.forEach { option ->
                    val selected = option in interests
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 60.dp)
                            .background(if (selected) colors.surfaceContainer else colors.background, MaterialTheme.shapes.medium)
                            .border(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant, MaterialTheme.shapes.medium)
                            .toggleable(value = selected, role = Role.Checkbox) {
                                interestNames = if (selected) interestNames - option.name else interestNames + option.name
                            }
                            .padding(horizontal = 6.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = selected,
                            onCheckedChange = null,
                            colors = CheckboxDefaults.colors(checkedColor = colors.primary, uncheckedColor = colors.outline),
                        )
                        Spacer(Modifier.width(6.dp))
                        Column {
                            Text(s.filterLabel(option), style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
                            Text(s.filterHelp(option), style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // ----- Aksi tetap di bawah -----
        Surface(color = colors.surfaceContainer) {
            Column {
                HorizontalDivider(color = colors.outlineVariant)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TextButton(onClick = onSkip, modifier = Modifier.height(52.dp)) {
                        Text(s.skip, style = MaterialTheme.typography.labelLarge, color = colors.tertiary)
                    }
                    Button(
                        onClick = { onSave(UserProfile(duration, interests)) },
                        enabled = interests.isNotEmpty(),   // minimal satu minat
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = MaterialTheme.shapes.medium,
                    ) { Text(s.seeRecommendations, style = MaterialTheme.typography.labelLarge) }
                }
            }
        }
    }
}
