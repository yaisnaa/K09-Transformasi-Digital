package id.ac.itb.k09.historygo.location

import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * "Papan pengumuman" bersama antara TriggerService (yang menulis) dan layar (yang membaca).
 * StateFlow = nilai yang bisa diamati; setiap kali berubah, layar otomatis digambar ulang.
 */
object TriggerState {
    /** Posisi pengguna terakhir dari GPS. null = belum ada. */
    val latestReading = MutableStateFlow<LocationReading?>(null)

    /** POI yang baru terpicu dan kartunya belum ditutup. null = tidak ada kartu. */
    val triggeredPoi = MutableStateFlow<Poi?>(null)
}