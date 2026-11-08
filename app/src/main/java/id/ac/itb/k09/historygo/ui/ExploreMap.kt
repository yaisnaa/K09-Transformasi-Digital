package id.ac.itb.k09.historygo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.data.Poi

/** Tingkat zoom yang cukup dekat untuk melihat trotoar dan bangunan. */
private const val ZOOM_JALAN = 17f

/**
 * Peta Explore (UI01): marker merah untuk POI, marker biru + lingkaran akurasi untuk pengguna.
 */
@Composable
fun ExploreMap(
    pois: List<Poi>,
    reading: LocationReading?,
    modifier: Modifier = Modifier,
) {
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
            cameraState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(reading.lat, reading.lng), ZOOM_JALAN)
            )
            movedToUser = true
        }
    }

    GoogleMap(modifier = modifier, cameraPositionState = cameraState) {
        // Marker POI (merah, warna bawaan).
        for (poi in poisOnMap) {
            key(poi.id) {
                Marker(
                    state = rememberUpdatedMarkerState(LatLng(poi.lat!!, poi.lng!!)),
                    title = poi.nama,
                    snippet = poi.kategori,
                )
            }
        }

        // Marker pengguna (biru) + lingkaran akurasi GPS.
        if (reading != null) {
            val me = LatLng(reading.lat, reading.lng)
            Circle(
                center = me,
                radius = reading.accuracyM,
                fillColor = Color(0x332196F3),
                strokeColor = Color(0xFF2196F3),
                strokeWidth = 2f,
            )
            Marker(
                state = rememberUpdatedMarkerState(me),
                title = "Posisi kamu",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE),
            )
        }
    }
}