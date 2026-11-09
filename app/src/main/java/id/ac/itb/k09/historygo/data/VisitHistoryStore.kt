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

    private var loaded = false

    /** Membaca file sekali saja. Kalau file rusak, mulai dari daftar kosong (tidak crash). */
    @Synchronized
    fun load(context: Context) {
        if (loaded) return
        val file = File(context.filesDir, FILE_NAME)
        _history.value = if (file.exists()) {
            runCatching { json.decodeFromString(serializer, file.readText()) }
                .getOrDefault(emptyList())
        } else {
            emptyList()
        }
        loaded = true
    }

    /** Menambah satu catatan (terbaru di paling atas) lalu menyimpannya ke file. */
    @Synchronized
    fun add(context: Context, record: VisitRecord) {
        load(context)
        val updated = listOf(record) + _history.value
        _history.value = updated
        File(context.filesDir, FILE_NAME).writeText(json.encodeToString(serializer, updated))
    }
}