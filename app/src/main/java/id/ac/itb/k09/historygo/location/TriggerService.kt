package id.ac.itb.k09.historygo.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import id.ac.itb.k09.historygo.MainActivity
import id.ac.itb.k09.historygo.audio.NarrationService
import id.ac.itb.k09.historygo.audio.narrationItem
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.SettingsStore
import id.ac.itb.k09.historygo.data.VisitHistoryStore
import id.ac.itb.k09.historygo.data.VisitRecord
import id.ac.itb.k09.historygo.data.loadPoisFromAssets
import id.ac.itb.k09.historygo.trigger.TriggerEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * Foreground service yang membaca GPS dan menjalankan TriggerEngine,
 * baik saat aplikasi terbuka maupun di latar (PB05, DECISIONS.md).
 */
class TriggerService : Service() {

    /** "Ruang kerja" untuk tugas yang berjalan terus. Dibatalkan saat service mati. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var trackingJob: Job? = null
    /** Koneksi ke NarrationService untuk autoplay. Dibuat saat pertama kali dibutuhkan. */
    private var narrationFuture: ListenableFuture<MediaController>? = null
    override fun onBind(intent: Intent?): IBinder? = null   // service ini tidak di-"bind"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Tombol "Berhenti" di notifikasi.
        if (intent?.action == ACTION_STOP) {
            stopSelf()
            return START_NOT_STICKY
        }
        // Tanpa izin lokasi, service tidak boleh berjalan.
        if (!hasLocationPermission(this)) {
            stopSelf()
            return START_NOT_STICKY
        }

        createChannels()
        // Wajib dipanggil cepat (< 5 detik) setelah service dimulai.
        ServiceCompat.startForeground(
            this,
            NOTIF_ID_ONGOING,
            buildOngoingNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION,
        )

        // Mulai membaca GPS sekali saja, walau service "dimulai" berkali-kali.
        if (trackingJob == null) {
            val engine = TriggerEngine(loadPoisFromAssets(this))
            trackingJob = scope.launch {
                LocationClient(this@TriggerService).locationUpdates().collect { reading ->
                    TriggerState.latestReading.value = reading
                    val poi = engine.onReading(reading, System.currentTimeMillis())
                    if (poi != null) onPoiTriggered(poi)
                }
            }
        }
        // Kalau Android mematikan service, jangan dihidupkan ulang otomatis.
        return START_NOT_STICKY
    }

    /** Aplikasi di-swipe dari daftar aplikasi terbuka: hentikan deteksi. */
    override fun onTaskRemoved(rootIntent: Intent?) {
        stopSelf()
    }

    override fun onDestroy() {
        scope.cancel()   // menghentikan GPS (awaitClose di LocationClient ikut berjalan)
        // Putuskan koneksi ke pemutar. Narasi yang sedang berputar TIDAK ikut berhenti.
        narrationFuture?.let { MediaController.releaseFuture(it) }
        narrationFuture = null
        super.onDestroy()
    }

    private fun onPoiTriggered(poi: Poi) {
        // Selalu disimpan, supaya kartunya muncul saat aplikasi terbuka / dibuka.
        TriggerState.triggeredPoi.value = poi
        autoplayNarration(poi)
        val appVisible = ProcessLifecycleOwner.get().lifecycle.currentState
            .isAtLeast(Lifecycle.State.STARTED)

        if (!appVisible) {
            showPoiNotification(poi)
        }
        // Catat ke riwayat "POI yang dilewati" (F02 no. 4).
        VisitHistoryStore.add(
            this,
            VisitRecord(
                poiId = poi.id,
                poiNama = poi.nama,
                timeMillis = System.currentTimeMillis(),
                background = !appVisible,
            ),
        )
    }
    /** F04 no. 3: kalau autoplay aktif dan POI punya audio, putar narasinya otomatis. */
    private fun autoplayNarration(poi: Poi) {
        SettingsStore.load(this)
        if (!SettingsStore.autoplay.value) return   // default mati
        val audio = poi.audio ?: return             // POI ini belum punya audio

        // Buat koneksi sekali, lalu pakai ulang untuk POI-POI berikutnya.
        val future = narrationFuture ?: MediaController.Builder(
            this,
            SessionToken(this, ComponentName(this, NarrationService::class.java)),
        ).buildAsync().also { narrationFuture = it }

        future.addListener(
            {
                val controller = runCatching { future.get() }.getOrNull() ?: return@addListener
                controller.setMediaItem(narrationItem(poi, audio))
                controller.prepare()
                controller.play()
            },
            ContextCompat.getMainExecutor(this),
        )
    }

    @SuppressLint("MissingPermission")   // izin dicek lewat areNotificationsEnabled() di bawah
    private fun showPoiNotification(poi: Poi) {
        val manager = NotificationManagerCompat.from(this)
        // Izin notifikasi ditolak: tidak ada notifikasi; kartu muncul saat aplikasi dibuka.
        if (!manager.areNotificationsEnabled()) return
        manager.notify(poi.id.hashCode(), buildPoiNotification(poi))
    }

    // ---------- NOTIFIKASI ----------

    private fun createChannels() {
        val manager = NotificationManagerCompat.from(this)
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_TRACKING, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Deteksi lokasi")
                .setDescription("Notifikasi permanen saat HistoryGo mendeteksi POI")
                .build()
        )
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_POI, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("POI di sekitar")
                .setDescription("Pemberitahuan saat kamu berada di dekat bangunan bersejarah")
                .build()
        )
    }

    private fun buildOngoingNotification(): Notification {
        val stopIntent = PendingIntent.getService(
            this, 0,
            Intent(this, TriggerService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return NotificationCompat.Builder(this, CHANNEL_TRACKING)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("HistoryGo aktif")
            .setContentText("Mendeteksi bangunan bersejarah di sekitarmu")
            .setOngoing(true)
            .setContentIntent(openAppIntent())
            .addAction(0, "Berhenti", stopIntent)
            .build()
    }

    private fun buildPoiNotification(poi: Poi): Notification =
        NotificationCompat.Builder(this, CHANNEL_POI)
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentTitle("Kamu di dekat ${poi.nama}")
            .setContentText(poi.kategori)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)                 // hilang setelah diketuk
            .setContentIntent(openAppIntent())   // mengetuk notifikasi membuka aplikasi
            .build()

    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        this, 0,
        Intent(this, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
    )

    companion object {
        private const val CHANNEL_TRACKING = "tracking"
        private const val CHANNEL_POI = "poi"
        private const val NOTIF_ID_ONGOING = 1
        private const val ACTION_STOP = "id.ac.itb.k09.historygo.STOP_TRIGGER"

        /** Dipanggil dari layar saat aplikasi terlihat dan izin lokasi sudah ada. */
        fun start(context: Context) {
            ContextCompat.startForegroundService(context, Intent(context, TriggerService::class.java))
        }
    }
}