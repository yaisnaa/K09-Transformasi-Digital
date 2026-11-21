package id.ac.itb.k09.historygo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.navigation.Route
import id.ac.itb.k09.historygo.trigger.TriggerConfig
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/** Tingkat zoom yang cukup dekat untuk melihat trotoar dan bangunan. */
private const val ZOOM_JALAN = 17f

/**
 * Peta Explore (UI01) dengan gaya "Jurnal Penjelajah Deco" (DESIGN.md, Peta):
 * marker belah ketupat, titik posisi teal, radius 25 m putus-putus, garis rute teal.
 *
 * @param activePoiId POI yang sedang terpicu atau menjadi tujuan navigasi: marker emas besar + label nama.
 * @param radiusPoiId POI yang radius masuknya (25 m) digambar: saat mendekati atau terpicu.
 * @param recenterRequest naikkan angkanya untuk memindahkan kamera kembali ke posisi pengguna.
 * @param contentPadding ruang yang tertutup bar atas dan sheet bawah, supaya logo Google tetap terlihat.
 */
@Composable
fun ExploreMap(
    pois: List<Poi>,
    reading: LocationReading?,
    modifier: Modifier = Modifier,
    onPoiClick: (Poi) -> Unit = {},
    route: Route? = null,
    activePoiId: String? = null,
    radiusPoiId: String? = null,
    recenterRequest: Int = 0,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val extra = HistoryGoTheme.colors
    val colors = MaterialTheme.colorScheme
    val dark = isSystemInDarkTheme()
    val s = LocalStrings.current

    // Hanya POI yang koordinatnya sudah diisi yang bisa digambar.
    val poisOnMap = pois.filter { it.hasCoordinates }

    // Posisi awal kamera: titik tengah semua POI yang punya koordinat.
    val cameraState = rememberCameraPositionState {
        position = if (poisOnMap.isNotEmpty()) {
            val center = LatLng(
                poisOnMap.map { it.lat!! }.average(),
                poisOnMap.map { it.lng!! }.average(),
            )
            CameraPosition.fromLatLngZoom(center, ZOOM_JALAN)
        } else {
            // Belum ada koordinat POI sama sekali: tampilkan peta dunia.
            CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 1f)
        }
    }

    // Saat posisi pengguna pertama kali terbaca, gerakkan kamera ke sana (sekali saja).
    var movedToUser by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(reading != null) {
        if (reading != null && !movedToUser) {
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(reading.lat, reading.lng), ZOOM_JALAN))
            movedToUser = true
        }
    }

    // Tombol "Kembali ke posisi saya".
    val latestReading by rememberUpdatedState(reading)
    LaunchedEffect(recenterRequest) {
        val r = latestReading ?: return@LaunchedEffect
        if (recenterRequest > 0) {
            cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(r.lat, r.lng), ZOOM_JALAN))
        }
    }

    // F06 no. 1: saat rute baru datang, arahkan kamera supaya seluruh rute terlihat.
    LaunchedEffect(route) {
        val points = route?.points ?: return@LaunchedEffect
        if (points.size < 2) return@LaunchedEffect
        val bounds = LatLngBounds.builder()
        points.forEach { bounds.include(LatLng(it.lat, it.lng)) }
        runCatching { cameraState.animate(CameraUpdateFactory.newLatLngBounds(bounds.build(), 150)) }
    }

    // Gaya peta JSON lokal (DECISIONS.md, "Gaya peta"), terang atau gelap mengikuti tema sistem.
    val mapStyle = remember(dark) {
        MapStyleOptions.loadRawResourceStyle(context, if (dark) R.raw.map_style_dark else R.raw.map_style)
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        contentPadding = contentPadding,
        properties = MapProperties(mapStyleOptions = mapStyle),
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
        ),
    ) {
        // Garis rute jalan kaki (F06): casing terang + garis teal, di bawah marker.
        val routePoints = route?.points.orEmpty()
        if (routePoints.size >= 2) {
            val latLngs = routePoints.map { LatLng(it.lat, it.lng) }
            Polyline(points = latLngs, color = colors.surfaceContainer, width = with(density) { 11.dp.toPx() })
            Polyline(points = latLngs, color = colors.secondary, width = with(density) { 6.dp.toPx() })
        }

        // Radius masuk 25 m di sekitar POI yang sedang didekati / terpicu.
        poisOnMap.firstOrNull { it.id == radiusPoiId }?.let { poi ->
            Circle(
                center = LatLng(poi.lat!!, poi.lng!!),
                radius = TriggerConfig.ENTRY_RADIUS_M,
                fillColor = extra.ornament.copy(alpha = 0.12f),
                strokeColor = extra.ornament,
                strokeWidth = with(density) { 1.5.dp.toPx() },
                strokePattern = listOf(Dash(24f), Gap(16f)),
            )
        }

        // Marker POI: belah ketupat. Akses manual (PRD): tap marker -> nama muncul -> tap nama -> POI Detail.
        for (poi in poisOnMap) {
            key(poi.id) {
                val active = poi.id == activePoiId
                MarkerComposable(
                    poi.id, active, extra.markerFill.toArgb(), extra.markerActiveFill.toArgb(),
                    state = rememberUpdatedMarkerState(LatLng(poi.lat!!, poi.lng!!)),
                    title = poi.nama,
                    snippet = s.markerSnippet(poi.kategori),
                    onInfoWindowClick = { onPoiClick(poi) },
                    // Marker aktif: ujung bawah belah ketupat tepat di titik POI; marker biasa: di tengah.
                    anchor = if (active) Offset(0.5f, ACTIVE_ANCHOR_Y) else Offset(0.5f, 0.5f),
                    zIndex = if (active) 2f else 1f,
                ) {
                    if (active) ActivePoiMarker(poi.nama) else PoiMarker()
                }
            }
        }

        // Posisi pengguna: lingkaran akurasi teal tipis + titik teal bercincin.
        if (reading != null) {
            val me = LatLng(reading.lat, reading.lng)
            Circle(
                center = me,
                radius = reading.accuracyM,
                fillColor = colors.secondary.copy(alpha = 0.12f),
                strokeColor = colors.secondary.copy(alpha = 0.35f),
                strokeWidth = with(density) { 1.dp.toPx() },
            )
            MarkerComposable(
                "me", colors.secondary.toArgb(),
                state = rememberUpdatedMarkerState(me),
                title = s.yourPosition,
                anchor = Offset(0.5f, 0.5f),
                zIndex = 3f,
            ) { UserDot() }
        }
    }
}

/** Tinggi label + jarak + kotak marker aktif, untuk menghitung anchor (lihat ActivePoiMarker). */
private const val ACTIVE_LABEL_DP = 30f
private const val ACTIVE_GAP_DP = 6f
private const val ACTIVE_BOX_DP = 32f
private const val ACTIVE_ANCHOR_Y = (ACTIVE_LABEL_DP + ACTIVE_GAP_DP + ACTIVE_BOX_DP / 2) /
    (ACTIVE_LABEL_DP + ACTIVE_GAP_DP + ACTIVE_BOX_DP)

/** Marker bangunan: belah ketupat 14dp berbingkai emas (kotak 22dp agar sudutnya tidak terpotong). */
@Composable
private fun PoiMarker() {
    val extra = HistoryGoTheme.colors
    Box(Modifier.size(22.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(14.dp).rotate(45f)
                .background(extra.markerFill)
                .border(2.dp, extra.markerRing),
        )
    }
}

/** Marker aktif: label nama di atas belah ketupat emas 22dp berinti gelap. */
@Composable
private fun ActivePoiMarker(name: String) {
    val extra = HistoryGoTheme.colors
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(ACTIVE_GAP_DP.dp)) {
        Box(
            Modifier.height(ACTIVE_LABEL_DP.dp)
                .background(colors.surfaceContainer, RoundedCornerShape(8.dp))
                .border(1.dp, colors.outlineVariant, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            // Nama panjang (mis. versi Inggris) dipotong agar label tidak menutupi peta.
            Text(
                name,
                style = MaterialTheme.typography.titleSmall,
                color = colors.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 200.dp),
            )
        }
        Box(Modifier.size(ACTIVE_BOX_DP.dp), contentAlignment = Alignment.Center) {
            Box(
                Modifier.size(22.dp).rotate(45f)
                    .background(extra.markerActiveFill)
                    .border(2.dp, extra.markerActiveCore),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.size(8.dp).background(extra.markerActiveCore))
            }
        }
    }
}

/** Titik posisi pengguna: lingkaran teal 18dp dengan cincin 3dp. */
@Composable
private fun UserDot() {
    val extra = HistoryGoTheme.colors
    Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
        Box(
            Modifier.size(20.dp)
                .background(extra.userDotRing, CircleShape)
                .padding(3.dp)
                .background(MaterialTheme.colorScheme.secondary, CircleShape),
        )
    }
}
