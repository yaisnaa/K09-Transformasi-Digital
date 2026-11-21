package id.ac.itb.k09.historygo.ui

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import id.ac.itb.k09.historygo.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.BuildConfig
import id.ac.itb.k09.historygo.audio.rememberNarration
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.PoiSearch
import id.ac.itb.k09.historygo.data.SettingsStore
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.i18n.rememberPois
import id.ac.itb.k09.historygo.location.TriggerService
import id.ac.itb.k09.historygo.location.TriggerState
import id.ac.itb.k09.historygo.location.hasLocationPermission
import id.ac.itb.k09.historygo.location.isGpsEnabled
import id.ac.itb.k09.historygo.navigation.NavigationManager
import id.ac.itb.k09.historygo.trigger.distanceMeters
import id.ac.itb.k09.historygo.trigger.findApproaching
import id.ac.itb.k09.historygo.trigger.findNearest
import id.ac.itb.k09.historygo.ui.components.ApproachStrip
import id.ac.itb.k09.historygo.ui.components.DevWarning
import id.ac.itb.k09.historygo.ui.components.IdleSheet
import id.ac.itb.k09.historygo.ui.components.LocationProblemSheet
import id.ac.itb.k09.historygo.ui.components.MapSearchButton
import id.ac.itb.k09.historygo.ui.components.MapStatus
import id.ac.itb.k09.historygo.ui.components.MapTopBar
import id.ac.itb.k09.historygo.ui.components.RecenterButton
import kotlin.math.roundToInt

/** Izin yang diminta. COARSE wajib ikut diminta bersama FINE sejak Android 12. */
private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

/**
 * Explore Map (UI01) dalam empat state prototype PRD (PB10):
 * di luar area (sheet "Belum ada bangunan"), mendekati (pita "Mendekati …"),
 * terpicu (kartu POI), dan akses konten (kartu -> POI Detail).
 * Ditambah state izin ditolak, GPS mati, dan mencari lokasi (DESIGN.md, Katalog state).
 */
@Composable
fun LocationScreen(
    onOpenDetail: (Poi) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onNavigate: (Poi) -> Unit,
    onOpenRoute: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val density = LocalDensity.current

    // ---------- STATE ----------
    var hasPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var gpsOn by remember { mutableStateOf(isGpsEnabled(context)) }
    var askedOnce by rememberSaveable { mutableStateOf(false) }
    var askedNotification by rememberSaveable { mutableStateOf(false) }
    // "Jelajah tanpa lokasi": sheet izin/GPS ditutup, peta tetap bisa dipakai manual.
    var locationSheetDismissed by rememberSaveable { mutableStateOf(false) }
    var recenterRequest by remember { mutableIntStateOf(0) }
    var bottomHeightPx by remember { mutableIntStateOf(0) }

    // ---------- MINTA IZIN LOKASI ----------
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        askedOnce = true
        hasPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    // PB10: izin TIDAK diminta otomatis di sini; onboarding sudah menjelaskan dan memintanya.
    // Kalau pengguna memilih "Jelajah tanpa lokasi", sheet di bawah peta menawarkan izin lagi.

    // ---------- MINTA IZIN NOTIFIKASI (PB05) ----------
    // Hanya Android 13 (API 33) ke atas yang butuh izin ini. Ditolak pun tidak apa-apa:
    // POI tetap disimpan dan kartunya muncul saat aplikasi dibuka (DECISIONS.md).
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        askedNotification = true
        SettingsStore.setNotificationAsked(context)
    }

    LaunchedEffect(hasPermission) {
        // Sudah pernah diminta (mis. di onboarding): jangan diminta lagi.
        SettingsStore.load(context)
        if (hasPermission && !askedNotification && !SettingsStore.notificationAsked.value && Build.VERSION.SDK_INT >= 33) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ---------- MULAI SERVICE (PB05) ----------
    // Dimulai saat aplikasi terlihat dan izin lokasi ada (DECISIONS.md).
    LaunchedEffect(hasPermission, gpsOn) {
        if (hasPermission && gpsOn) TriggerService.start(context)
    }

    LifecycleResumeEffect(Unit) {
        hasPermission = hasLocationPermission(context)
        gpsOn = isGpsEnabled(context)
        // Kalau service sempat dihentikan lewat tombol "Berhenti", nyalakan lagi saat aplikasi dibuka.
        if (hasPermission && gpsOn) TriggerService.start(context)
        onPauseOrDispose { }
    }

    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                gpsOn = isGpsEnabled(context)
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
        onDispose { context.unregisterReceiver(receiver) }
    }

    // ---------- BACA DARI TriggerState (PB05) ----------
    // Layar tidak membaca GPS sendiri; TriggerService yang membacanya.
    val reading by TriggerState.latestReading.collectAsStateWithLifecycle()
    val triggeredPoi by TriggerState.triggeredPoi.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { SettingsStore.load(context) }
    val detectionOn by SettingsStore.detection.collectAsStateWithLifecycle()
    // F06: keadaan navigasi (null = tidak sedang bernavigasi).
    val navigation by NavigationManager.state.collectAsStateWithLifecycle()
    val narration = rememberNarration()

    // ---------- DATA ----------
    val s = LocalStrings.current
    val pois = rememberPois()   // dalam bahasa aktif (A01)
    val current = reading
    val poiTanpaKoordinat = pois.count { !it.hasCoordinates }
    val now = System.currentTimeMillis()

    // Service menyimpan POI versi asli; tampilkan versi bahasa aktif (A01).
    val shownPoi = triggeredPoi?.let { raw -> pois.firstOrNull { it.id == raw.id } ?: raw }
    val approaching = current?.let { findApproaching(pois, it, now) }
    val nearest = current?.let { findNearest(pois, it) }
    val navTarget = navigation?.let { it.stops.getOrNull(it.currentStopIndex) }

    val status = when {
        !detectionOn -> MapStatus.DETECTION_OFF
        !hasPermission || !gpsOn -> MapStatus.LOCATION_OFF
        current == null -> MapStatus.SEARCHING
        else -> MapStatus.DETECTING
    }

    // ---------- TAMPILAN ----------
    Box(modifier = modifier.fillMaxSize()) {

        // Peta selalu tampil, walau izin lokasi ditolak (PRD: konten tetap bisa diakses manual).
        ExploreMap(
            pois = pois,
            reading = current,
            modifier = Modifier.fillMaxSize(),
            onPoiClick = onOpenDetail,
            route = navigation?.route,
            activePoiId = shownPoi?.id ?: navTarget?.id,
            radiusPoiId = shownPoi?.id ?: approaching?.poi?.id,
            recenterRequest = recenterRequest,
            contentPadding = PaddingValues(top = 136.dp, bottom = with(density) { bottomHeightPx.toDp() }),
        )

        // ----- Atas: bar aplikasi, pencarian, navigasi berjalan -----
        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MapTopBar(status = status, onOpenSettings = onOpenSettings)
            MapSearchButton(onClick = onOpenSearch)
            // F06: pita teal "Menuju …" selama navigasi berjalan; kartunya di bawah.
            navTarget?.let { target ->
                NavHeader(pois.firstOrNull { it.id == target.id }?.nama ?: target.nama)
            }
            if (!BuildConfig.HAS_MAPS_API_KEY) DevWarning(s.mapsKeyMissing)
        }

        // ----- Bawah: satu sheet sesuai state, plus tombol kembali ke posisi -----
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { bottomHeightPx = it.height }
                .padding(12.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (current != null) RecenterButton(onClick = { recenterRequest++ })

            when {
                // Terpicu (F02): kartu POI, juga untuk POI yang terpicu saat aplikasi di latar.
                shownPoi != null -> {
                    val distance = current?.let {
                        if (shownPoi.hasCoordinates) distanceMeters(it.lat, it.lng, shownPoi.lat!!, shownPoi.lng!!).roundToInt() else null
                    }
                    val audio = shownPoi.audio
                    PoiCard(
                        poi = shownPoi,
                        distanceM = distance,
                        isPlaying = narration.isPlaying(shownPoi.id),
                        onListen = audio?.let { path -> { narration.toggle(shownPoi, path, s.narrationArtist) } },
                        onClose = { TriggerState.triggeredPoi.value = null },
                        onOpenDetail = { onOpenDetail(shownPoi) },
                    )
                }

                !hasPermission && !locationSheetDismissed -> LocationProblemSheet(
                    title = s.locationNotReadTitle,
                    body = "${s.permissionNeeded} ${s.manualAccessHint}",
                    actionText = s.allowLocation,
                    onAction = {
                        val canAskAgain = activity?.shouldShowRequestPermissionRationale(
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == true
                        if (!askedOnce || canAskAgain) {
                            permissionLauncher.launch(LOCATION_PERMISSIONS)
                        } else {
                            context.startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null),
                                )
                            )
                        }
                    },
                    onDismiss = { locationSheetDismissed = true },
                )

                hasPermission && !gpsOn && !locationSheetDismissed -> LocationProblemSheet(
                    title = s.gpsOffTitle,
                    body = s.gpsOffBody,
                    actionText = s.enableGps,
                    onAction = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                    onDismiss = { locationSheetDismissed = true },
                )

                // F06: kartu navigasi (berjalan, menghitung, gagal, tiba, beberapa titik).
                navigation != null -> NavigationCard(
                    nav = navigation!!,
                    hasLocation = current != null,
                    onStop = { NavigationManager.stop() },
                    onRetry = { NavigationManager.retry() },
                )

                // Mendekati (PB10): pita kecil, peta tetap terlihat.
                approaching != null -> ApproachStrip(
                    name = approaching.poi.nama,
                    distanceM = approaching.distanceM.roundToInt(),
                )

                // Di luar area: bangunan terdekat.
                current != null -> IdleSheet(
                    nearestName = nearest?.poi?.nama,
                    nearestDistanceM = nearest?.distanceM?.roundToInt(),
                    nearestWalkMinutes = nearest?.distanceM?.let { PoiSearch.walkMinutes(it) },
                    poisWithoutCoordinates = poiTanpaKoordinat,
                    onDirections = nearest?.poi?.let { poi -> { onNavigate(poi) } },
                    onOpenRoute = onOpenRoute,
                )
            }
        }
    }
}

/** Pita teal di atas peta selama navigasi berjalan (DESIGN.md: teal = lokasi dan rute). */
@Composable
private fun NavHeader(destinationName: String) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = Modifier.fillMaxWidth().background(colors.secondary, MaterialTheme.shapes.medium).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(painterResource(R.drawable.ic_navigate), null, tint = colors.onSecondary, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(s.navTowards(destinationName), style = MaterialTheme.typography.titleMedium, color = colors.onSecondary, maxLines = 1)
    }
}
