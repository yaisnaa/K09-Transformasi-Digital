package id.ac.itb.k09.historygo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/** Bar atas layar turunan (Pengaturan, Cari, detail digulir): tombol kembali 48dp + judul serif. */
@Composable
fun HgTopBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier) {
    val s = LocalStrings.current
    Row(
        modifier = modifier.fillMaxWidth().height(64.dp).padding(start = 4.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack, modifier = Modifier.size(48.dp)) {
            Icon(painterResource(R.drawable.ic_back), contentDescription = s.back, tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.width(4.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, maxLines = 1)
    }
}

/** Belah ketupat: motif tunggal HistoryGo (marker, progres, ornamen). */
@Composable
fun Diamond(size: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(modifier.size(size).rotate(45f).background(color))
}

/** Ornamen pembatas Deco: garis pendek, belah ketupat, garis panjang. */
@Composable
fun DecoDivider(modifier: Modifier = Modifier) {
    val gold = HistoryGoTheme.colors.ornament
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.width(32.dp).height(1.dp).background(gold))
        Spacer(Modifier.width(12.dp))
        Diamond(7.dp, gold)
        Spacer(Modifier.width(12.dp))
        Box(Modifier.weight(1f).height(1.dp).background(gold))
    }
}

/** Chip status kecil (DESIGN.md, Chips): "Belum tervalidasi", "Konten contoh". */
@Composable
fun StatusChip(text: String, modifier: Modifier = Modifier, warning: Boolean = true) {
    val c = MaterialTheme.colorScheme
    Box(
        modifier = modifier
            .background(if (warning) c.errorContainer else c.primaryContainer, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 10.dp, vertical = 5.dp),
    ) {
        Text(text, style = MaterialTheme.typography.labelMedium, color = if (warning) c.onErrorContainer else c.onPrimaryContainer)
    }
}

/**
 * Dua tombol berdampingan; kalau tidak muat (teks Inggris panjang, ukuran huruf besar),
 * keduanya bertumpuk selebar penuh supaya label tidak terpotong (DESIGN.md: label tombol
 * tidak terpotong baris). Tombol pertama mengisi sisa lebar saat berdampingan.
 * Dengan satu anak, anak itu dibuat selebar penuh.
 */
@Composable
fun AdaptiveButtonRow(gap: Dp, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Layout(content = content, modifier = modifier.fillMaxWidth()) { measurables, constraints ->
        val max = constraints.maxWidth
        val gapPx = gap.roundToPx()
        if (measurables.size < 2) {
            val p = measurables.map { it.measure(Constraints.fixedWidth(max).copy(minHeight = 0, maxHeight = constraints.maxHeight)) }
            return@Layout layout(max, p.sumOf { it.height }) { var y = 0; p.forEach { it.place(0, y); y += it.height } }
        }
        val (first, second) = measurables
        val firstWant = first.maxIntrinsicWidth(constraints.maxHeight)
        val secondWant = second.maxIntrinsicWidth(constraints.maxHeight)
        if (firstWant + gapPx + secondWant <= max) {
            val sp = second.measure(Constraints.fixedWidth(secondWant))
            val fp = first.measure(Constraints.fixedWidth(max - gapPx - secondWant))
            val h = maxOf(fp.height, sp.height)
            layout(max, h) {
                fp.place(0, 0)
                sp.place(max - sp.width, 0)
            }
        } else {
            val fp = first.measure(Constraints.fixedWidth(max))
            val sp = second.measure(Constraints.fixedWidth(max))
            layout(max, fp.height + gapPx + sp.height) {
                fp.place(0, 0)
                sp.place(0, fp.height + gapPx)
            }
        }
    }
}
