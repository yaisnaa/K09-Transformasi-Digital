package id.ac.itb.k09.historygo.trigger

/**
 * Semua parameter trigger dikumpulkan di sini (aturan CLAUDE.md no. 3).
 * Nilainya mengikuti usulan DECISIONS.md dan belum final sampai uji lapangan.
 */
object TriggerConfig {
    /** Jarak maksimal ke POI agar dianggap "masuk" (meter). */
    const val ENTRY_RADIUS_M = 25.0

    /** POI bisa terpicu lagi setelah pengguna menjauh lebih dari ini (meter). */
    const val REARM_DISTANCE_M = 40.0

    /** Jumlah bacaan GPS berturut-turut di dalam radius sebelum POI terpicu. */
    const val CONFIRM_READINGS = 2

    /** Seberapa sering GPS dibaca (milidetik). 3_000 = 3 detik. */
    const val LOCATION_INTERVAL_MS = 3_000L

    /** Bacaan dengan akurasi lebih buruk dari ini dianggap tidak valid (meter). */
    const val MAX_ACCURACY_M = 30.0

    /** Bacaan yang lebih tua dari ini dianggap kedaluwarsa (milidetik). */
    const val MAX_AGE_MS = 15_000L
}