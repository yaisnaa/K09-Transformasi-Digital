package id.ac.itb.k09.historygo.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Menyimpan riwayat "POI yang dilewati" ke file JSON di memori internal aplikasi,
 * supaya tetap ada setelah aplikasi ditutup.
 */
object VisitHistoryStore {

    private const val FILE_NAME = "visit_history.json"
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(VisitRecord.serializer())

    // _history bisa diubah dari dalam object ini saja; dari luar hanya bisa dibaca lewat history.
    private val _history = MutableStateFlow<List<VisitRecord>>(emptyList())
    val history: StateFlow<List<VisitRecord>> = _history.asStateFlow()

    /** Akun yang riwayatnya sedang dimuat; null = belum dimuat (CL-03, progres per akun). */
    private var loadedFor: String? = null

    /** Membaca file sekali saja. Kalau file rusak, mulai dari daftar kosong (tidak crash). */
    @Synchronized
    fun load(context: Context) {
        val account = ActiveAccount.current(context)
        if (loadedFor == account) return
        val historyFile = file(context)
        _history.value = if (historyFile.exists()) {
            runCatching { json.decodeFromString(serializer, historyFile.readText()) }
                .getOrDefault(emptyList())
        } else {
            emptyList()
        }
        loadedFor = account
    }

    /** Menambah satu catatan (terbaru di paling atas) lalu menyimpannya ke file. */
    @Synchronized
    fun add(context: Context, record: VisitRecord) {
        load(context)
        val updated = listOf(record) + _history.value
        _history.value = updated
        file(context).writeText(json.encodeToString(serializer, updated))
    }

    /** Mengembalikan riwayat yang baru dihapus (tombol "Urungkan" di snackbar, PB10). */
    @Synchronized
    fun restore(context: Context, records: List<VisitRecord>) {
        load(context)
        _history.value = records
        file(context).writeText(json.encodeToString(serializer, records))
    }

    /** Menghapus seluruh riwayat (dipakai tombol "Hapus riwayat"). */
    @Synchronized
    fun clear(context: Context) {
        load(context)
        _history.value = emptyList()
        file(context).delete()
    }

    /** Dipanggil ActiveAccount saat berganti akun: buang riwayat akun lama, muat riwayat akun baru. */
    @Synchronized
    fun reload(context: Context) {
        loadedFor = null
        load(context)
    }

    /** Berkas riwayat milik akun aktif; akun tamu memakai nama berkas lama. */
    private fun file(context: Context) =
        File(context.filesDir, AccountNaming.scopedFile(FILE_NAME, ActiveAccount.current(context)))
}
