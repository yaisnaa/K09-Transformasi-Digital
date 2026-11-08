package id.ac.itb.k09.historygo.data

import android.content.Context

/**
 * Membaca assets/poi.json lalu mem-parse-nya.
 * Satu-satunya file di data/ yang memakai Android (butuh Context untuk membuka assets).
 */
fun loadPoisFromAssets(context: Context): List<Poi> =
    context.assets.open("poi.json")
        .bufferedReader()
        .use { reader -> PoiParser.parse(reader.readText()) }