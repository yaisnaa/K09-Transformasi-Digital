package id.ac.itb.k09.historygo.data

import kotlinx.serialization.Serializable

/** Satu catatan "POI yang dilewati". Kotlin murni. */
@Serializable
data class VisitRecord(
    val poiId: String,
    val poiNama: String,
    val timeMillis: Long,      // kapan POI terpicu
    val background: Boolean,   // true = terpicu saat aplikasi di latar
)