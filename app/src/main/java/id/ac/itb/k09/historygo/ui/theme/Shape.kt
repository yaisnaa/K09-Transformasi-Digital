package id.ac.itb.k09.historygo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/*
 * Bentuk dari DESIGN.md: sudut membulat lembut.
 * extraSmall 8dp  = chip status
 * small      10dp = chip filter
 * medium     16dp = tombol, search bar
 * large      20dp = kartu dan sheet
 * extraLarge 24dp = sheet yang menimpa foto
 */
val Shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(24.dp),
)
