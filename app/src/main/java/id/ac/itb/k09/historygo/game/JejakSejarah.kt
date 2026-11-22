package id.ac.itb.k09.historygo.game

import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.trigger.TriggerConfig
import id.ac.itb.k09.historygo.trigger.distanceMeters
import id.ac.itb.k09.historygo.trigger.isValidReading
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

/** Progres treasure hunt "Jejak Sejarah" (F08). Disimpan di perangkat. */
@Serializable
data class GameProgress(
    val checkedIn: Set<String> = emptySet(),          // ID POI yang sudah di-check-in
    val answers: Map<String, Boolean> = emptyMap(),   // ID POI -> jawaban kuis benar/salah (hanya jawaban pertama)
    val points: Int = 0,
)

/** Hasil percobaan check-in. */
sealed interface CheckInResult {
    data class Success(val progress: GameProgress, val pointsEarned: Int) : CheckInResult
    data class TooFar(val distanceM: Int) : CheckInResult          // F08 no. 1: di luar radius
    data object NoLocation : CheckInResult                          // lokasi belum terbaca / tidak valid
    data object AlreadyCheckedIn : CheckInResult
}

/** Hasil menjawab kuis. */
data class AnswerResult(val progress: GameProgress, val correct: Boolean, val pointsEarned: Int)

/** Aturan "Jejak Sejarah" (F08). Kotlin murni, tanpa Android. */
object JejakSejarah {

    const val CHECK_IN_POINTS = 5
    const val CORRECT_ANSWER_POINTS = 10
    const val BADGE_NAME = "Penjelajah Jejak Sejarah"

    /** Misi = semua POI yang punya koordinat (bisa di-check-in). */
    fun missionIds(pois: List<Poi>): List<String> = pois.filter { it.hasCoordinates }.map { it.id }

    /** Jarak pengguna ke POI (meter), atau null kalau bacaan tidak ada / tidak valid. */
    fun distanceTo(poi: Poi, reading: LocationReading?, nowMillis: Long): Int? {
        if (reading == null || !isValidReading(reading, nowMillis) || !poi.hasCoordinates) return null
        return distanceMeters(reading.lat, reading.lng, poi.lat!!, poi.lng!!).roundToInt()
    }

    /** F08 no. 1: check-in hanya berhasil di dalam radius POI (radius yang sama dengan trigger). */
    fun checkIn(progress: GameProgress, poi: Poi, reading: LocationReading?, nowMillis: Long): CheckInResult {
        if (poi.id in progress.checkedIn) return CheckInResult.AlreadyCheckedIn
        val distance = distanceTo(poi, reading, nowMillis) ?: return CheckInResult.NoLocation
        if (distance > TriggerConfig.ENTRY_RADIUS_M) return CheckInResult.TooFar(distance)
        return CheckInResult.Success(
            progress = progress.copy(
                checkedIn = progress.checkedIn + poi.id,
                points = progress.points + CHECK_IN_POINTS,
            ),
            pointsEarned = CHECK_IN_POINTS,
        )
    }

    /**
     * F08 no. 3: jawaban benar menambah poin. Kuis baru bisa dijawab setelah check-in,
     * dan hanya jawaban pertama yang dihitung (tidak bisa menebak berulang untuk poin).
     * @return null kalau belum check-in atau sudah pernah menjawab.
     */
    fun answer(progress: GameProgress, poi: Poi, choiceIndex: Int): AnswerResult? {
        val quiz = poi.kuis ?: return null
        if (poi.id !in progress.checkedIn || poi.id in progress.answers) return null
        val correct = choiceIndex == quiz.jawaban
        val earned = if (correct) CORRECT_ANSWER_POINTS else 0
        return AnswerResult(
            progress = progress.copy(
                answers = progress.answers + (poi.id to correct),
                points = progress.points + earned,
            ),
            correct = correct,
            pointsEarned = earned,
        )
    }

    /** Misi satu POI selesai = sudah check-in dan sudah menjawab kuisnya (atau POI itu tidak punya kuis). */
    fun isMissionComplete(progress: GameProgress, poi: Poi): Boolean =
        poi.id in progress.checkedIn && (poi.kuis == null || poi.id in progress.answers)

    /** F08 no. 2: progres misi x/N. */
    fun completedCount(progress: GameProgress, pois: List<Poi>): Int {
        val missions = missionIds(pois).toSet()
        return pois.count { it.id in missions && isMissionComplete(progress, it) }
    }

    /** F08 no. 3: semua misi selesai membuka badge. */
    fun badgeUnlocked(progress: GameProgress, pois: List<Poi>): Boolean {
        val missions = pois.filter { it.hasCoordinates }
        return missions.isNotEmpty() && missions.all { isMissionComplete(progress, it) }
    }
}
