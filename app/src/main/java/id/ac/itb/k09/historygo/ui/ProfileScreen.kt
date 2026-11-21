package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.data.PoiFilter
import id.ac.itb.k09.historygo.data.PoiRecommender
import id.ac.itb.k09.historygo.data.UserProfile
import id.ac.itb.k09.historygo.i18n.LocalStrings

/** Kuesioner profil pengguna (F07): durasi kunjungan dan minat. */
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

    // Tombol Back HP sama dengan "Lewati".
    BackHandler(onBack = onSkip)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Text(s.profileTitle, style = MaterialTheme.typography.headlineSmall)
        Text(
            s.profileIntro,
            style = MaterialTheme.typography.bodyMedium,
        )

        Spacer(Modifier.height(24.dp))
        Text(s.profileDurationQuestion, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PoiRecommender.DURATION_OPTIONS.forEach { minutes ->
                FilterChip(
                    selected = duration == minutes,
                    onClick = { duration = minutes },
                    label = { Text(s.minutes(minutes)) },
                )
            }
        }

        Spacer(Modifier.height(24.dp))
        Text(s.profileInterestQuestion, style = MaterialTheme.typography.titleMedium)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PoiFilter.entries.forEach { option ->
                val selected = option in interests
                FilterChip(
                    selected = selected,
                    onClick = {
                        interestNames = if (selected) interestNames - option.name else interestNames + option.name
                    },
                    label = { Text(s.filterLabel(option)) },
                )
            }
        }

        Spacer(Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            TextButton(onClick = onSkip) { Text(s.skip) }
            Button(
                onClick = { onSave(UserProfile(duration, interests)) },
                enabled = interests.isNotEmpty(),   // minimal satu minat
            ) {
                Text(s.seeRecommendations)
            }
        }
    }
}
