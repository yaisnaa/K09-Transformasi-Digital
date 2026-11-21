package id.ac.itb.k09.historygo.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.game.CheckInResult
import id.ac.itb.k09.historygo.game.GameStore
import id.ac.itb.k09.historygo.game.JejakSejarah
import id.ac.itb.k09.historygo.i18n.AppStrings
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.location.TriggerState
import id.ac.itb.k09.historygo.trigger.TriggerConfig

/** Bagian "Jejak Sejarah" di halaman POI Detail (F08): check-in lalu kuis satu soal. */
@Composable
fun CheckInSection(poi: Poi, modifier: Modifier = Modifier) {
    if (!poi.hasCoordinates) return
    val s = LocalStrings.current
    val context = LocalContext.current
    LaunchedEffect(Unit) { GameStore.load(context) }
    val progress by GameStore.progress.collectAsStateWithLifecycle()
    val reading by TriggerState.latestReading.collectAsStateWithLifecycle()

    // Hasil terakhir setelah menekan tombol, disimpan sebagai KODE (mis. "checkin:5") supaya
    // teksnya ikut berganti saat bahasa diganti (A01). Lihat feedbackText() di bawah.
    var feedback by rememberSaveable(poi.id) { mutableStateOf<String?>(null) }

    val checkedIn = poi.id in progress.checkedIn
    val answered = progress.answers[poi.id]
    val distance = JejakSejarah.distanceTo(poi, reading, System.currentTimeMillis())
    val radius = TriggerConfig.ENTRY_RADIUS_M.toInt()
    val inRadius = distance != null && distance <= TriggerConfig.ENTRY_RADIUS_M

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(s.missionCardTitle, style = MaterialTheme.typography.titleMedium)

            when {
                !checkedIn -> {
                    Text(
                        when {
                            distance == null -> s.checkInNoLocation
                            inRadius -> s.checkInReady
                            else -> s.checkInTooFar(distance, radius)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(
                        enabled = inRadius,
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
                    ) { Text(s.checkIn) }
                }

                answered == null && poi.kuis != null -> QuizQuestion(poi) { choice ->
                    val result = JejakSejarah.answer(progress, poi, choice) ?: return@QuizQuestion
                    GameStore.save(context, result.progress)
                    feedback = if (result.correct) "correct:${result.pointsEarned}" else "wrong"
                }

                else -> Text(s.missionDone(answered), style = MaterialTheme.typography.bodyMedium)
            }

            feedbackText(feedback, poi, s)?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/** Mengubah kode hasil menjadi teks dalam bahasa aktif. */
private fun feedbackText(code: String?, poi: Poi, s: AppStrings): String? {
    if (code == null) return null
    val value = code.substringAfter(':', "").toIntOrNull() ?: 0
    return when (code.substringBefore(':')) {
        "checkin" -> s.checkInSuccess(value)
        "far" -> s.checkInStillFar(value)
        "noloc" -> s.locationNotRead
        "correct" -> s.answerCorrect(value)
        "wrong" -> poi.kuis?.let { s.answerWrong(it.pilihan[it.jawaban]) }
        else -> null
    }
}

@Composable
private fun QuizQuestion(poi: Poi, onAnswer: (Int) -> Unit) {
    val quiz = poi.kuis ?: return
    val s = LocalStrings.current
    var selected by rememberSaveable(poi.id) { mutableStateOf<Int?>(null) }

    Text(quiz.pertanyaan, style = MaterialTheme.typography.bodyLarge)
    if (quiz.dummy) Text(s.quizDummy, style = MaterialTheme.typography.labelSmall)
    quiz.pilihan.forEachIndexed { index, option ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(selected = selected == index, onClick = { selected = index })
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected == index, onClick = { selected = index })
            Text(option)
        }
    }
    Button(enabled = selected != null, onClick = { selected?.let(onAnswer) }) { Text(s.answer) }
}
