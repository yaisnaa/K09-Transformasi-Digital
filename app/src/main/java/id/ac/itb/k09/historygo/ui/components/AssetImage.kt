package id.ac.itb.k09.historygo.ui.components

import android.graphics.BitmapFactory
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Memuat gambar dari folder assets (mis. "foto/p01.jpg") di latar.
 * Hasil: null = masih dimuat; Result.success = gambar; Result.failure = gagal.
 * Kalau path null, hasilnya langsung failure (foto belum tersedia).
 */
@Composable
fun rememberAssetBitmap(path: String?): Result<ImageBitmap>? {
    val context = LocalContext.current
    val result by produceState<Result<ImageBitmap>?>(initialValue = null, path) {
        value = if (path == null) {
            Result.failure(IllegalStateException("Foto belum tersedia"))
        } else {
            withContext(Dispatchers.IO) {
                runCatching {
                    context.assets.open(path).use { stream ->
                        BitmapFactory.decodeStream(stream)?.asImageBitmap()
                            ?: error("Format gambar tidak dikenali")
                    }
                }
            }
        }
    }
    return result
}
