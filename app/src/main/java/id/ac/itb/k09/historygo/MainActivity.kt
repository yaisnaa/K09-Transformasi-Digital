package id.ac.itb.k09.historygo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import id.ac.itb.k09.historygo.navigation.NavigationManager
import id.ac.itb.k09.historygo.ui.HistoryScreen
import id.ac.itb.k09.historygo.ui.LocationScreen
import id.ac.itb.k09.historygo.ui.PoiDetailScreen
import id.ac.itb.k09.historygo.ui.SearchScreen
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HistoryGoTheme {
                // Layar mana yang tampil (yang paling atas menang):
                // detailPoiId terisi -> POI Detail; showSearch -> Cari POI; showHistory -> Riwayat; selain itu -> Peta.
                // Karena urutannya begini, Kembali dari detail yang dibuka lewat pencarian akan kembali ke pencarian.
                var showHistory by rememberSaveable { mutableStateOf(false) }
                var showSearch by rememberSaveable { mutableStateOf(false) }
                var detailPoiId by rememberSaveable { mutableStateOf<String?>(null) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val screenModifier = Modifier.padding(innerPadding)
                    val poiId = detailPoiId

                    when {
                        poiId != null -> PoiDetailScreen(
                            poiId = poiId,
                            onBack = { detailPoiId = null },
                            // F06: mulai navigasi lalu kembali ke peta supaya rutenya terlihat.
                            onNavigate = { poi ->
                                NavigationManager.start(this@MainActivity, poi)
                                detailPoiId = null
                                showSearch = false
                                showHistory = false
                            },
                            modifier = screenModifier,
                        )

                        showSearch -> SearchScreen(
                            onBack = { showSearch = false },
                            onOpenDetail = { poi -> detailPoiId = poi.id },
                            modifier = screenModifier,
                        )

                        showHistory -> HistoryScreen(
                            onBack = { showHistory = false },
                            modifier = screenModifier,
                        )

                        else -> LocationScreen(
                            onOpenHistory = { showHistory = true },
                            onOpenDetail = { poi -> detailPoiId = poi.id },
                            onOpenSearch = { showSearch = true },
                            modifier = screenModifier,
                        )
                    }
                }
            }
        }
    }
}