package id.ac.itb.k09.historygo.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.game.CheckInResult
import id.ac.itb.k09.historygo.game.GameStore
import id.ac.itb.k09.historygo.game.JejakSejarah
import id.ac.itb.k09.historygo.i18n.AppStrings
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.location.TriggerState
import id.ac.itb.k09.historygo.trigger.TriggerConfig
import id.ac.itb.k09.historygo.ui.components.Diamond
import id.ac.itb.k09.historygo.ui.components.StatusChip
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/**
 * Kartu "Misi Jejak Sejarah" di POI Detail (F08): check-in di lokasi lalu kuis.
 * Tampilan dari lembar "State: check-in, kuis, Jejak Sejarah" (DESIGN.md, Katalog state).
 */
@Composable
fun CheckInSection(poi: Poi, modifier: Modifier = Modifier) {
    if (!poi.hasCoordinates) return
    val s = LocalStrings.current
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    LaunchedEffect(Unit) { GameStore.load(context) }
    val progress by GameStore.progress.collectAsStateWithLifecycle()
    val reading by TriggerState.latestReading.collectAsStateWithLifecycle()

    // Hasil terakhir setelah menekan tombol, disimpan sebagai KODE (mis. "checkin:5") supaya
    // teksnya ikut berganti saat bahasa diganti (A01). Lihat Feedback() di bawah.
    var feedback by rememberSaveable(poi.id) { mutableStateOf<String?>(null) }

    val checkedIn = poi.id in progress.checkedIn
    val answered = progress.answers[poi.id]
    val distance = JejakSejarah.distanceTo(poi, reading, System.currentTimeMillis())
    val radius = TriggerConfig.ENTRY_RADIUS_M.toInt()
    val inRadius = distance != null && distance <= TriggerConfig.ENTRY_RADIUS_M

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = colors.surfaceContainer,
        border = BorderStroke(1.5.dp, HistoryGoTheme.colors.ornament),
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Diamond(14.dp, HistoryGoTheme.colors.ornament)
                Spacer(Modifier.width(12.dp))
                Text(s.missionCardTitle, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
            }

            Feedback(feedback, poi, s)

            when {
                !checkedIn -> {
                    Text(
                        when {
                            distance == null -> s.checkInNoLocation
                            inRadius -> s.checkInReady
                            else -> s.checkInTooFar(distance, radius)
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.onSurfaceVariant,
                    )
                    FilledTonalButton(
                        enabled = inRadius,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = MaterialTheme.shapes.medium,
                        // Tombol tonal krem (DESIGN.md, Buttons). Teal hanya untuk lokasi.
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = colors.primaryContainer,
                            contentColor = colors.onPrimaryContainer,
                        ),
                        onClick = {
                            feedback = when (val result = JejakSejarah.checkIn(progress, poi, reading, System.currentTimeMillis())) {
                                is CheckInResult.Success -> {
                                    GameStore.save(context, result.progress)
                                    "checkin:${result.pointsEarned}"
                                }
                                is CheckInResult.TooFar -> "far:${result.distanceM}"
                                CheckInResult.NoLocation -> "noloc"
                                CheckInResult.AlreadyCheckedIn -> null
                            }
                        },
                    ) {
                        Text(s.checkIn, style = MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            s.pointsShort(JejakSejarah.CHECK_IN_POINTS),
                            style = HistoryGoTheme.type.angka,
                            color = if (inRadius) colors.tertiary else colors.onSurfaceVariant,
                        )
                    }
                }

                answered == null && poi.kuis != null -> QuizQuestion(poi) { choice ->
                    val result = JejakSejarah.answer(progress, poi, choice) ?: return@QuizQuestion
                    GameStore.save(context, result.progress)
                    feedback = if (result.correct) "correct:${result.pointsEarned}" else "wrong"
                }

                else -> Row(
                    modifier = Modifier.fillMaxWidth().background(colors.secondaryContainer, MaterialTheme.shapes.small).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painterResource(R.drawable.ic_check), null, tint = colors.onSecondaryContainer, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(s.missionDone(answered), style = MaterialTheme.typography.titleSmall, color = colors.onSecondaryContainer)
                }
            }
        }
    }
}

/** Hasil terakhir (check-in, jawaban) dalam bahasa aktif, dengan warna sesuai jenisnya. */
@Composable
private fun Feedback(code: String?, poi: Poi, s: AppStrings) {
    if (code == null) return
    val colors = MaterialTheme.colorScheme
    val value = code.substringAfter(':', "").toIntOrNull() ?: 0
    val (text, positive) = when (code.substringBefore(':')) {
        "checkin" -> s.checkInSuccess(value) to true
        "correct" -> s.answerCorrect(value) to true
        "far" -> s.checkInStillFar(value) to false
        "noloc" -> s.locationNotRead to false
        "wrong" -> (poi.kuis?.let { s.answerWrong(it.pilihan[it.jawaban]) } ?: return) to false
        else -> return
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (positive) colors.secondaryContainer else colors.errorContainer, MaterialTheme.shapes.small)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (positive) {
            Icon(painterResource(R.drawable.ic_check), null, tint = colors.onSecondaryContainer, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
        }
        Text(
            text,
            style = MaterialTheme.typography.titleSmall,
            color = if (positive) colors.onSecondaryContainer else colors.onErrorContainer,
        )
    }
}

/** Kuis pilihan ganda. Dimulai TANPA pilihan; "Jawab" aktif setelah memilih (critique v2, P1). */
@Composable
private fun QuizQuestion(poi: Poi, onAnswer: (Int) -> Unit) {
    val quiz = poi.kuis ?: return
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    var selected by rememberSaveable(poi.id) { mutableStateOf<Int?>(null) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (quiz.dummy) StatusChip(s.quizDummyChip)
        Text(quiz.pertanyaan, style = MaterialTheme.typography.titleMedium, color = colors.onSurface)
        Column(Modifier.selectableGroup(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            quiz.pilihan.forEachIndexed { index, option ->
                val isSelected = selected == index
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp)
                        .border(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) colors.primary else colors.outlineVariant,
                            MaterialTheme.shapes.medium,
                        )
                        .background(if (isSelected) colors.background else colors.surfaceContainer, MaterialTheme.shapes.medium)
                        .selectable(selected = isSelected, role = Role.RadioButton, onClick = { selected = index })
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(selectedColor = colors.primary, unselectedColor = colors.outline),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(option, style = MaterialTheme.typography.bodyLarge, color = colors.onSurface)
                }
            }
        }
        Button(
            enabled = selected != null,
            onClick = { selected?.let(onAnswer) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.medium,
        ) { Text(s.answer, style = MaterialTheme.typography.labelLarge) }
    }
}
