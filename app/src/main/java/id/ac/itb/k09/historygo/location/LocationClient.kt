package id.ac.itb.k09.historygo.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import id.ac.itb.k09.historygo.data.LocationReading
import id.ac.itb.k09.historygo.trigger.TriggerConfig
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Cek apakah pengguna sudah memberi izin lokasi presisi. */
fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

/** Cek apakah layanan lokasi (GPS) di HP sedang menyala. */
fun isGpsEnabled(context: Context): Boolean {
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return LocationManagerCompat.isLocationEnabled(manager)
}

/** Membaca posisi pengguna lewat Fused Location Provider. */
class LocationClient(context: Context) {

    private val fused = LocationServices.getFusedLocationProviderClient(context)

    /**
     * Mengalirkan posisi terbaru setiap TriggerConfig.LOCATION_INTERVAL_MS.
     * Hanya boleh dipanggil setelah hasLocationPermission() bernilai true.
     */
    @SuppressLint("MissingPermission")
    fun locationUpdates(): Flow<LocationReading> = callbackFlow {
        // 1. Atur permintaan: akurasi tinggi, setiap 3 detik.
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            TriggerConfig.LOCATION_INTERVAL_MS
        ).build()

        // 2. "Penerima" yang dipanggil setiap kali posisi baru datang.
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    trySend(
                        LocationReading(
                            lat = loc.latitude,
                            lng = loc.longitude,
                            accuracyM = loc.accuracy.toDouble(),
                            timeMillis = loc.time,
                        )
                    )
                }
            }
        }

        // 3. Kirim posisi terakhir yang diketahui dulu, supaya marker cepat muncul.
        fused.lastLocation.addOnSuccessListener { loc ->
            if (loc != null) {
                trySend(LocationReading(loc.latitude, loc.longitude, loc.accuracy.toDouble(), loc.time))
            }
        }

        // 4. Mulai minta update rutin.
        fused.requestLocationUpdates(request, callback, Looper.getMainLooper())

        // 5. Saat layar berhenti mendengarkan, hentikan GPS supaya hemat baterai.
        awaitClose { fused.removeLocationUpdates(callback) }
    }
}