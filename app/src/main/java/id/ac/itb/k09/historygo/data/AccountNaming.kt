package id.ac.itb.k09.historygo.data

/**
 * Penamaan penyimpanan per akun (CL-03). Kotlin murni, diuji di AccountNamingTest.
 * Akun tamu memakai nama lama tanpa akhiran, supaya progres yang sudah ada di HP tidak hilang.
 */
object AccountNaming {
    const val GUEST_ID = "tamu"

    /** Id akun hanya boleh huruf kecil dan angka, supaya aman dipakai sebagai nama berkas. */
    fun isValidId(id: String): Boolean = id.isNotEmpty() && id.length <= 64 && id.all { it in 'a'..'z' || it in '0'..'9' }

    /** "historygo_game" + akun "a1b2" -> "historygo_game_a1b2"; akun tamu -> "historygo_game". */
    fun scoped(baseName: String, accountId: String): String {
        if (accountId == GUEST_ID) return baseName
        require(isValidId(accountId)) { "Id akun tidak valid: $accountId" }
        return "${baseName}_$accountId"
    }

    /** Sama seperti [scoped], untuk nama berkas berekstensi: "visit_history.json" -> "visit_history_a1b2.json". */
    fun scopedFile(fileName: String, accountId: String): String {
        val dot = fileName.lastIndexOf('.')
        if (dot <= 0) return scoped(fileName, accountId)
        return scoped(fileName.substring(0, dot), accountId) + fileName.substring(dot)
    }
}
