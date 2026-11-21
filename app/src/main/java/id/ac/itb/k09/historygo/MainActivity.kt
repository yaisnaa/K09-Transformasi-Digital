package id.ac.itb.k09.historygo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.ProfileStore
import id.ac.itb.k09.historygo.i18n.AppStrings
import id.ac.itb.k09.historygo.i18n.LanguageStore
import id.ac.itb.k09.historygo.i18n.LocalLanguage
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.location.TriggerService
import id.ac.itb.k09.historygo.location.hasLocationPermission
import id.ac.itb.k09.historygo.location.isGpsEnabled
import id.ac.itb.k09.historygo.navigation.NavigationManager
import id.ac.itb.k09.historygo.ui.GameScreen
import id.ac.itb.k09.historygo.ui.HistoryScreen
import id.ac.itb.k09.historygo.ui.LocationScreen
import id.ac.itb.k09.historygo.ui.PoiDetailScreen
import id.ac.itb.k09.historygo.ui.ProfileScreen
import id.ac.itb.k09.historygo.ui.RecommendationScreen
import id.ac.itb.k09.historygo.ui.SearchScreen
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

class MainActivity : ComponentActivity() {

    /**
     * Mulai membaca GPS begitu aplikasi terlihat, apa pun layar pertamanya (mis. kuesioner profil),
     * supaya rekomendasi (F07) dan pencarian (F05) langsung tahu posisi pengguna.
     * Aman dipanggil berkali-kali; LocationScreen juga memanggilnya.
     */
    override fun onStart() {
        super.onStart()
        if (hasLocationPermission(this) && isGpsEnabled(this)) TriggerService.start(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // F07: baca profil tersimpan sebelum layar pertama digambar.
        ProfileStore.load(this)
        // A01: baca bahasa tersimpan sebelum layar pertama digambar.
        LanguageStore.load(this)
        setContent {
            // Bahasa aktif dibagikan ke semua layar. Saat diganti, semua layar langsung digambar ulang.
            val language by LanguageStore.language.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalLanguage provides language,
                LocalStrings provides AppStrings.of(language),
            ) {
                HistoryGoTheme {
                    // Layar mana yang tampil (yang paling atas menang):
                    // detailPoiId -> POI Detail; kuesioner profil; rekomendasi; Cari POI; Riwayat; selain itu -> Peta.
                    // Karena urutannya begini, Kembali dari detail akan kembali ke layar asalnya (pencarian/rekomendasi).
                    var showHistory by rememberSaveable { mutableStateOf(false) }
                    var showSearch by rememberSaveable { mutableStateOf(false) }
                    var showProfile by rememberSaveable { mutableStateOf(false) }
                    var showRecommendations by rememberSaveable { mutableStateOf(false) }
                    var showGame by rememberSaveable { mutableStateOf(false) }
                    var detailPoiId by rememberSaveable { mutableStateOf<String?>(null) }

                    val profile by ProfileStore.profile.collectAsStateWithLifecycle()
                    val profileSkipped by ProfileStore.skipped.collectAsStateWithLifecycle()
                    // Alur PRD no. 1: kuesioner muncul otomatis saat pertama kali, sampai diisi atau dilewati.
                    val firstTimeProfile = profile == null && !profileSkipped

                    // Mulai navigasi lalu kembali ke peta supaya rutenya terlihat (F06).
                    // Satu POI = "Navigasi ke sini"; beberapa POI = rute rekomendasi (F07).
                    val startRoute: (List<Poi>) -> Unit = { pois ->
                        NavigationManager.startRoute(this@MainActivity, pois)
                        detailPoiId = null
                        showSearch = false
                        showHistory = false
                        showRecommendations = false
                        showGame = false
                    }
                    val startNavigation: (Poi) -> Unit = { poi -> startRoute(listOf(poi)) }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        val screenModifier = Modifier.padding(innerPadding)
                        val poiId = detailPoiId

                        when {
                            poiId != null -> PoiDetailScreen(
                                poiId = poiId,
                                onBack = { detailPoiId = null },
                                onNavigate = startNavigation,
                                modifier = screenModifier,
                            )

                            showProfile || firstTimeProfile -> ProfileScreen(
                                initial = profile,
                                onSave = { newProfile ->
                                    ProfileStore.save(this@MainActivity, newProfile)
                                    showProfile = false
                                    showRecommendations = true   // langsung tampilkan "rute awal"
                                },
                                onSkip = {
                                    if (firstTimeProfile) ProfileStore.skip(this@MainActivity)
                                    showProfile = false
                                },
                                modifier = screenModifier,
                            )

                            showRecommendations -> RecommendationScreen(
                                onBack = { showRecommendations = false },
                                onOpenDetail = { poi -> detailPoiId = poi.id },
                                onEditProfile = { showProfile = true },
                                onStartRoute = startRoute,
                                modifier = screenModifier,
                            )

                            showGame -> GameScreen(
                                onBack = { showGame = false },
                                onOpenDetail = { poi -> detailPoiId = poi.id },
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
                                onOpenRecommendations = { showRecommendations = true },
                                onOpenGame = { showGame = true },
                                modifier = screenModifier,
                            )
                        }
                    }
                }
            }
        }
    }
}