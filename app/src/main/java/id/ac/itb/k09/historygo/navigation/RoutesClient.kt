package id.ac.itb.k09.historygo.navigation

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.pm.PackageInfoCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest

/**
 * Meminta rute jalan kaki ke Google Routes API (computeRoutes).
 * API key yang dipakai sama dengan Google Maps: dibaca dari meta-data di Manifest,
 * yang diisi dari MAPS_API_KEY di local.properties (key tidak ditulis di kode).
 */
object RoutesClient {

    private const val TAG = "RoutesClient"
    private const val ENDPOINT = "https://routes.googleapis.com/directions/v2:computeRoutes"
    private const val FIELD_MASK = "routes.distanceMeters,routes.duration,routes.polyline.encodedPolyline"
    private const val TIMEOUT_MS = 10_000

    private val json = Json { ignoreUnknownKeys = true }

    // Bentuk jawaban Routes API (hanya field yang diminta lewat FIELD_MASK).
    @Serializable private data class RoutesResponse(val routes: List<RouteDto> = emptyList())
    @Serializable private data class RouteDto(
        val distanceMeters: Int = 0,
        val duration: String = "0s",           // contoh: "165s"
        val polyline: PolylineDto? = null,
    )
    @Serializable private data class PolylineDto(val encodedPolyline: String = "")

    suspend fun computeWalkingRoute(context: Context, from: GeoPoint, to: GeoPoint): Result<Route> =
        withContext(Dispatchers.IO) {
            runCatching {
                val apiKey = readApiKey(context)
                    ?: error("MAPS_API_KEY belum diisi di local.properties")

                val body = """
                    {"origin":{"location":{"latLng":{"latitude":${from.lat},"longitude":${from.lng}}}},
                     "destination":{"location":{"latLng":{"latitude":${to.lat},"longitude":${to.lng}}}},
                     "travelMode":"WALK","languageCode":"id"}
                """.trimIndent()

                val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = TIMEOUT_MS
                    readTimeout = TIMEOUT_MS
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json")
                    setRequestProperty("X-Goog-Api-Key", apiKey)
                    setRequestProperty("X-Goog-FieldMask", FIELD_MASK)
                    // Dibutuhkan kalau API key dibatasi untuk aplikasi Android tertentu.
                    setRequestProperty("X-Android-Package", context.packageName)
                    signingSha1(context)?.let { setRequestProperty("X-Android-Cert", it) }
                }
                try {
                    connection.outputStream.use { it.write(body.toByteArray()) }
                    val code = connection.responseCode
                    if (code !in 200..299) {
                        // Detail teknis ke Logcat (tag "RoutesClient"), pesan ramah ke layar.
                        val detail = connection.errorStream?.bufferedReader()?.use { it.readText() }.orEmpty()
                        Log.w(TAG, "computeRoutes HTTP $code: $detail")
                        error(
                            when (code) {
                                403 -> "Routes API belum aktif atau API key belum diizinkan memakai Routes API."
                                429 -> "Kuota Routes API habis. Coba lagi nanti."
                                else -> "Layanan rute sedang bermasalah (HTTP $code)."
                            }
                        )
                    }
                    val text = connection.inputStream.bufferedReader().use { it.readText() }
                    val route = json.decodeFromString(RoutesResponse.serializer(), text).routes.firstOrNull()
                        ?: error("Rute jalan kaki tidak ditemukan")
                    Route(
                        points = decodePolyline(route.polyline?.encodedPolyline.orEmpty()),
                        distanceM = route.distanceMeters.toDouble(),
                        durationS = route.duration.removeSuffix("s").toDoubleOrNull() ?: 0.0,
                    )
                } finally {
                    connection.disconnect()
                }
            }.recoverCatching { e ->
                // Tanpa internet atau server tidak terjangkau: pesan yang bisa dipahami pengguna.
                if (e is java.io.IOException) error("Tidak ada koneksi internet.") else throw e
            }
        }

    private fun readApiKey(context: Context): String? {
        @Suppress("DEPRECATION")
        val info = context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
        return info.metaData?.getString("com.google.android.geo.API_KEY")?.takeIf { it.isNotBlank() }
    }

    /** SHA-1 sertifikat penandatangan aplikasi, huruf besar tanpa titik dua (format header X-Android-Cert). */
    private fun signingSha1(context: Context): String? = runCatching {
        val signature = PackageInfoCompat.getSignatures(context.packageManager, context.packageName).first()
        MessageDigest.getInstance("SHA-1").digest(signature.toByteArray())
            .joinToString("") { "%02X".format(it) }
    }.getOrNull()
}
