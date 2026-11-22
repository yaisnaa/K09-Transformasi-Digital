package id.ac.itb.k09.historygo.data

/**
 * Memecah teks "tahun dibangun" menjadi angka utama (ditampilkan besar di plakat)
 * dan keterangan (ditampilkan kecil di bawahnya). Kotlin murni, tanpa dependensi Android.
 *
 * Contoh:
 * - "1895 (dirombak 1926, diperluas 1940)" -> "1895" + "Dirombak 1926, diperluas 1940"
 * - "1939 (bangunan sekarang); Hotel Homann sudah ada sejak abad ke-19"
 *   -> "1939" + "Bangunan sekarang; Hotel Homann sudah ada sejak abad ke-19"
 * - "1871 atau 1877 (sumber berbeda)" -> "1871 atau 1877" + "Sumber berbeda"
 * - "Awal abad ke-19; penataan terakhir 2014" -> tanpa angka utama (main = null)
 */
data class YearText(val main: String?, val note: String?)

object YearTextSplitter {
    private val leadingYear = Regex("""^(\d{4}(?:\s*(?:[-–]|atau|or)\s*\d{4})?)(.*)$""", RegexOption.IGNORE_CASE)

    fun split(text: String?): YearText {
        val value = text?.trim().orEmpty()
        if (value.isEmpty()) return YearText(null, null)
        val match = leadingYear.find(value) ?: return YearText(null, value)
        val main = match.groupValues[1].trim()
        val note = match.groupValues[2]
            .replace("(", "")
            .replace(")", "")
            .trim()
            .trimStart(';', ',', ':', '-', '–')
            .trim()
            .replaceFirstChar { it.uppercase() }
        return YearText(main, note.ifEmpty { null })
    }
}
