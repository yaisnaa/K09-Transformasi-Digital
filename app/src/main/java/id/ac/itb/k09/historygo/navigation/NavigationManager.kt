package id.ac.itb.k09.historygo.navigation

import android.content.Context
import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.location.TriggerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Keadaan navigasi yang ditampilkan di layar. null di NavigationManager.state = tidak sedang bernavigasi. */
data class NavigationState(
    val stops: List<Poi>,                // titik berurutan; satu titik untuk "Navigasi ke sini"
    val currentStopIndex: Int = 0,       // titik yang sedang dituju
    val lastReachedIndex: Int? = null,   // titik terakhir yang baru dicapai (untuk pesan "Sudah sampai di ...")
    val route: Route? = null,
    val loading: Boolean = false,        // sedang meminta rute ke Routes API
    val rerouteCount: Int = 0,           // berapa kali rute dihitung ulang (F06 no. 4)
    val remainingM: Int? = null,         // sisa jarak sampai titik terakhir
    val remainingMinutes: Int? = null,
    val toNextStopM: Int? = null,
    val arrived: Boolean = false,        // F06 no. 3: titik terakhir sudah dicapai
    val error: RouteError? = null,       // pesannya diterjemahkan di layar (A01)
) {
    /** POI yang sedang dituju. */
    val destination: Poi get() = stops[currentStopIndex]
    val isMultiStop: Boolean get() = stops.size > 1
}

/**
 * Pengelola navigasi (F06). Dipanggil dari layar (mulai/berhenti) dan dari TriggerService
 * (setiap bacaan GPS), jadi navigasi tetap jalan walau layar berganti.
 */
object NavigationManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var engine: NavigationEngine? = null
    private var fetchJob: Job? = null
    private lateinit var appContext: Context

    private val _state = MutableStateFlow<NavigationState?>(null)
    val state: StateFlow<NavigationState?> = _state.asStateFlow()

    /** Navigasi ke satu POI (tombol "Navigasi ke sini"). */
    fun start(context: Context, poi: Poi) = startRoute(context, listOf(poi))

    /** Navigasi melewati beberapa POI berurutan (rute rekomendasi F07). */
    fun startRoute(context: Context, pois: List<Poi>) {
        val stops = pois.filter { it.hasCoordinates }
        if (stops.isEmpty()) return
        appContext = context.applicationContext
        fetchJob?.cancel()
        engine = NavigationEngine(stops.map { GeoPoint(it.lat!!, it.lng!!) })
        _state.value = NavigationState(stops = stops)
        TriggerState.latestReading.value?.let { fetchRoute(it, isReroute = false) }
    }

    fun stop() {
        fetchJob?.cancel()
        engine = null
        _state.value = null
    }

    /** Tombol "Coba lagi" setelah gagal mengambil rute. */
    fun retry() {
        val reading = TriggerState.latestReading.value ?: return
        if (_state.value != null) fetchRoute(reading, isReroute = false)
    }

    /** Dipanggil TriggerService untuk setiap bacaan GPS. */
    fun onReading(reading: LocationReading) {
        val nav = engine ?: return
        val current = _state.value ?: return

        // Rute pertama diminta saat posisi pertama datang (kalau navigasi dimulai sebelum lokasi terbaca).
        if (current.route == null && !current.loading && current.error == null) {
            fetchRoute(reading, isReroute = false)
        }

        val update = nav.onReading(reading, System.currentTimeMillis()) ?: return
        _state.value = _state.value?.let { s ->
            s.copy(
                currentStopIndex = update.currentStopIndex,
                lastReachedIndex = update.reachedStopIndex ?: s.lastReachedIndex,
                remainingM = update.remainingM,
                remainingMinutes = update.remainingMinutes,
                toNextStopM = update.toNextStopM,
                arrived = s.arrived || update.arrived,
            )
        }

        // Setelah gagal, jangan terus mengulang otomatis; pengguna menekan "Coba lagi".
        if (update.needsReroute && !update.arrived && fetchJob?.isActive != true && _state.value?.error == null) {
            fetchRoute(reading, isReroute = true)
        }
    }

    private fun fetchRoute(from: LocationReading, isReroute: Boolean) {
        val nav = engine ?: return
        val stopsAtRequest = _state.value?.stops ?: return
        // Rute diminta melewati titik yang BELUM dicapai saja.
        val remaining = nav.remainingStops
        _state.value = _state.value?.copy(loading = true, error = null)
        fetchJob = scope.launch {
            val result = RoutesClient.computeWalkingRoute(appContext, GeoPoint(from.lat, from.lng), remaining)
            // Navigasi bisa saja dihentikan atau diganti saat menunggu jawaban.
            val stillActive = engine === nav && _state.value?.stops == stopsAtRequest
            if (!stillActive) return@launch
            result
                .onSuccess { route ->
                    nav.setRoute(route, requestedFrom = GeoPoint(from.lat, from.lng))
                    _state.value = _state.value?.copy(
                        route = route,
                        loading = false,
                        rerouteCount = (_state.value?.rerouteCount ?: 0) + if (isReroute) 1 else 0,
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value?.copy(
                        loading = false,
                        error = (e as? RouteException)?.error ?: RouteError.UNKNOWN,
                    )
                }
        }
    }
}
