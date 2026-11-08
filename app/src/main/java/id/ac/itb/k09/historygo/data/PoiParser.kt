package id.ac.itb.k09.historygo.data

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/** Mengubah teks JSON (isi poi.json) menjadi daftar Poi. Kotlin murni, tanpa Android. */
object PoiParser {

    // ignoreUnknownKeys: kalau nanti JSON punya field tambahan, aplikasi tidak crash.
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(text: String): List<Poi> =
        json.decodeFromString(ListSerializer(Poi.serializer()), text)
}