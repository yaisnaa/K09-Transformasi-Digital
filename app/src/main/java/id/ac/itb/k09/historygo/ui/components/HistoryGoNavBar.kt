package id.ac.itb.k09.historygo.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.i18n.LocalStrings

/** Empat tujuan utama aplikasi (DESIGN.md, Navigation). */
enum class MainTab(@DrawableRes val icon: Int) {
    EXPLORE(R.drawable.ic_nav_explore),
    ROUTE(R.drawable.ic_nav_route),
    TRAIL(R.drawable.ic_nav_trail),
    HISTORY(R.drawable.ic_nav_history),
}

/**
 * Navigation bar Material 3: 4 tujuan, ikon garis + label, indikator pil Krem Tua.
 * Pengaturan tidak menjadi tab; dibuka dari ikon di bar atas peta.
 */
@Composable
fun HistoryGoNavBar(
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        HorizontalDivider(color = colors.outlineVariant)
        NavigationBar(containerColor = colors.surfaceContainer) {
            MainTab.entries.forEach { tab ->
                val isSelected = tab == selected
                val label = when (tab) {
                    MainTab.EXPLORE -> s.tabExplore
                    MainTab.ROUTE -> s.tabRoute
                    MainTab.TRAIL -> s.tabTrail
                    MainTab.HISTORY -> s.tabHistory
                }
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelect(tab) },
                    icon = { Icon(painterResource(tab.icon), contentDescription = null) },
                    label = {
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            maxLines = 1,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = colors.onSurface,
                        selectedTextColor = colors.onSurface,
                        indicatorColor = colors.primaryContainer,
                        unselectedIconColor = colors.onSurfaceVariant,
                        unselectedTextColor = colors.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}
