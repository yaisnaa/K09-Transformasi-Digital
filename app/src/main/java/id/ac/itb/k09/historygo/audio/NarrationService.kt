package id.ac.itb.k09.historygo.audio

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import id.ac.itb.k09.historygo.MainActivity

/**
 * Service pemutar narasi (PB06, F04).
 * Media3 otomatis menampilkan notifikasi media + kontrol lock screen selama audio berputar,
 * dan menjadikan service ini foreground supaya tidak dimatikan Android.
 */
class NarrationService : MediaSessionService() {

    private var session: MediaSession? = null

    override fun onCreate() {
        super.onCreate()

        val player = ExoPlayer.Builder(this)
            // Narasi = suara orang bicara. "true" = otomatis jeda saat ada telepon / aplikasi lain memutar audio.
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(C.USAGE_MEDIA)
                    .setContentType(C.AUDIO_CONTENT_TYPE_SPEECH)
                    .build(),
                true,
            )
            // Jeda otomatis saat earphone dicabut, supaya narasi tidak tiba-tiba keluar dari speaker.
            .setHandleAudioBecomingNoisy(true)
            .build()

        // Mengetuk notifikasi media membuka aplikasi.
        val openApp = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        session = MediaSession.Builder(this, player)
            .setSessionActivity(openApp)
            .build()
    }

    /** Dipanggil Media3 saat ada "remote control" (layar detail, notifikasi, headset) yang ingin terhubung. */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = session

    override fun onDestroy() {
        session?.run {
            player.release()   // lepaskan pemutar supaya memori dan audio dibebaskan
            release()
        }
        session = null
        super.onDestroy()
    }
}