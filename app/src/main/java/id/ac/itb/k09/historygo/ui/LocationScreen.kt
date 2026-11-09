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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.BuildConfig
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.SettingsStore
import id.ac.itb.k09.historygo.data.VisitHistoryStore
import id.ac.itb.k09.historygo.data.loadPoisFromAssets
import id.ac.itb.k09.historygo.location.TriggerService
import id.ac.itb.k09.historygo.location.TriggerState
import id.ac.itb.k09.historygo.location.hasLocationPermission
import id.ac.itb.k09.historygo.location.isGpsEnabled

/** Izin yang diminta. COARSE wajib ikut diminta bersama FINE sejak Android 12. */
private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
)

@Composable
fun LocationScreen(
    onOpenHistory: () -> Unit,
    onOpenDetail: (Poi) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = LocalActivity.current

    // ---------- STATE ----------
    var hasPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var gpsOn by remember { mutableStateOf(isGpsEnabled(context)) }
    var askedOnce by rememberSaveable { mutableStateOf(false) }
    var askedNotification by rememberSaveable { mutableStateOf(false) }

    // ---------- MINTA IZIN LOKASI ----------
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        askedOnce = true
        hasPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasPermission && !askedOnce) permissionLauncher.launch(LOCATION_PERMISSIONS)
    }

    // ---------- MINTA IZIN NOTIFIKASI (BARU PB05) ----------
    // Hanya Android 13 (API 33) ke atas yang butuh izin ini. Ditolak pun tidak apa-apa:
    // POI tetap disimpan dan kartunya muncul saat aplikasi dibuka (DECISIONS.md).
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { askedNotification = true }

    LaunchedEffect(hasPermission) {
        if (hasPermission && !askedNotification && Build.VERSION.SDK_INT >= 33) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // ---------- MULAI SERVICE (BARU PB05) ----------
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

    // ---------- BACA DARI TriggerState (BARU PB05) ----------
    // Layar tidak lagi membaca GPS sendiri; TriggerService yang membacanya.
    val reading by TriggerState.latestReading.collectAsStateWithLifecycle()
    val triggeredPoi by TriggerState.triggeredPoi.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { VisitHistoryStore.load(context) }
    val history by VisitHistoryStore.history.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { SettingsStore.load(context) }
    val autoplay by SettingsStore.autoplay.collectAsStateWithLifecycle()

    // ---------- DATA ----------
    val pois = remember { loadPoisFromAssets(context) }
    val current = reading
    val poiTanpaKoordinat = pois.count { !it.hasCoordinates }

    // ---------- TAMPILAN ----------
    Box(modifier = modifier.fillMaxSize()) {

        // Peta selalu tampil, walau izin lokasi ditolak (PRD: konten tetap bisa diakses manual).
        ExploreMap(pois = pois, reading = current, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier.align(Alignment.TopCenter).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when {
                !hasPermission -> StatusCard(
                    message = "HistoryGo butuh izin lokasi presisi untuk mengetahui bangunan bersejarah di sekitarmu.",
                    buttonText = "Izinkan lokasi",
                    onClick = {
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
                )

                !gpsOn -> StatusCard(
                    message = "GPS sedang mati. Nyalakan lokasi agar posisimu terbaca.",
                    buttonText = "Aktifkan GPS",
                    onClick = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
                )

                current == null -> StatusCard(message = "Mencari lokasi…")
            }

            if (!BuildConfig.HAS_MAPS_API_KEY) {
                StatusCard(message = "MAPS_API_KEY belum diisi di local.properties, peta tidak bisa tampil.")
            }

            if (poiTanpaKoordinat > 0) {
                StatusCard(message = "$poiTanpaKoordinat POI belum punya koordinat, belum tampil di peta.")
            }

            Button(onClick = onOpenHistory) { Text("POI yang dilewati (${history.size})") }

            // F04 no. 3: saklar autoplay narasi (default mati, DECISIONS.md).
            Card(modifier = Modifier.padding(top = 8.dp)) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Putar narasi otomatis")
                    Spacer(Modifier.width(12.dp))
                    Switch(
                        checked = autoplay,
                        onCheckedChange = { enabled -> SettingsStore.setAutoplay(context, enabled) },
                    )
                }
            }
        }

        // Kartu POI di bagian bawah saat sebuah POI terpicu (juga yang terpicu saat aplikasi di latar).
        val shownPoi = triggeredPoi
        if (shownPoi != null) {
            PoiCard(
                poi = shownPoi,
                onClose = { TriggerState.triggeredPoi.value = null },
                onOpenDetail = { onOpenDetail(shownPoi) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
            )
        }
    }
}

/** Kartu pesan kecil di atas peta, dengan tombol opsional. */
@Composable
private fun StatusCard(
    message: String,
    buttonText: String? = null,
    onClick: () -> Unit = {},
) {
    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(message, textAlign = TextAlign.Center)
            if (buttonText != null) {
                Spacer(Modifier.height(8.dp))
                Button(onClick = onClick) { Text(buttonText) }
            }
        }
    }
}