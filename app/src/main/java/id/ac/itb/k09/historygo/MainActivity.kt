package id.ac.itb.k09.historygo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.account.AccountStore
import id.ac.itb.k09.historygo.data.Poi
import id.ac.itb.k09.historygo.data.ProfileStore
import id.ac.itb.k09.historygo.data.SettingsStore
import id.ac.itb.k09.historygo.i18n.AppStrings
import id.ac.itb.k09.historygo.i18n.LanguageStore
import id.ac.itb.k09.historygo.i18n.LocalLanguage
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.location.TriggerService
import id.ac.itb.k09.historygo.location.hasLocationPermission
import id.ac.itb.k09.historygo.location.isGpsEnabled
import id.ac.itb.k09.historygo.navigation.NavigationManager
import id.ac.itb.k09.historygo.ui.AuthScreen
import id.ac.itb.k09.historygo.ui.GameScreen
import id.ac.itb.k09.historygo.ui.HistoryScreen
import id.ac.itb.k09.historygo.ui.LocationScreen
import id.ac.itb.k09.historygo.ui.OnboardingScreen
import id.ac.itb.k09.historygo.ui.PoiDetailScreen
import id.ac.itb.k09.historygo.ui.ProfileScreen
import id.ac.itb.k09.historygo.ui.RecommendationScreen
import id.ac.itb.k09.historygo.ui.SearchScreen
import id.ac.itb.k09.historygo.ui.SettingsScreen
import id.ac.itb.k09.historygo.ui.components.HistoryGoNavBar
import id.ac.itb.k09.historygo.ui.components.MainTab
import id.ac.itb.k09.historygo.ui.components.ScreenKey
import id.ac.itb.k09.historygo.ui.components.rememberReduceMotion
import id.ac.itb.k09.historygo.ui.components.screenTransition
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
        // PB10: onboarding tampil sekali, dibaca sebelum layar pertama digambar.
        SettingsStore.load(this)
        // CL-03: akun lokal aktif (default tamu).
        AccountStore.load(this)
        setContent {
            // Bahasa aktif dibagikan ke semua layar. Saat diganti, semua layar langsung digambar ulang.
            val language by LanguageStore.language.collectAsStateWithLifecycle()
            CompositionLocalProvider(
                LocalLanguage provides language,
                LocalStrings provides AppStrings.of(language),
            ) {
                HistoryGoTheme {
                    // PB10: empat tab utama (navigation bar) + layar "di atas" tab.
                    // Layar di atas tab (yang paling atas menang): POI Detail, kuesioner profil, Pengaturan, Cari.
                    // Karena urutannya begini, Kembali dari detail akan kembali ke layar asalnya.
                    var tab by rememberSaveable { mutableStateOf(MainTab.EXPLORE) }
                    var showSearch by rememberSaveable { mutableStateOf(false) }
                    var showProfile by rememberSaveable { mutableStateOf(false) }
                    var showSettings by rememberSaveable { mutableStateOf(false) }
                    var detailPoiId by rememberSaveable { mutableStateOf<String?>(null) }

                    val onboardingDone by SettingsStore.onboardingDone.collectAsStateWithLifecycle()
                    val authDecided by AccountStore.authDecided.collectAsStateWithLifecycle()
                    var showAuth by rememberSaveable { mutableStateOf(false) }
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
                        showSettings = false
                        tab = MainTab.EXPLORE
                    }
                    val startNavigation: (Poi) -> Unit = { poi -> startRoute(listOf(poi)) }
                    val openDetail: (Poi) -> Unit = { poi -> detailPoiId = poi.id }

                    val overlayOpen = !onboardingDone || !authDecided || showAuth || detailPoiId != null || showProfile || firstTimeProfile || showSettings || showSearch

                    // Tombol Back di tab selain Jelajah kembali ke Jelajah.
                    BackHandler(enabled = !overlayOpen && tab != MainTab.EXPLORE) { tab = MainTab.EXPLORE }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        containerColor = MaterialTheme.colorScheme.background,
                        bottomBar = {
                            // Navigation bar turun saat layar dalam dibuka, naik lagi saat kembali.
                            AnimatedVisibility(
                                visible = !overlayOpen,
                                enter = slideInVertically { it } + fadeIn(),
                                exit = slideOutVertically { it } + fadeOut(),
                            ) { HistoryGoNavBar(selected = tab, onSelect = { tab = it }) }
                        },
                    ) { innerPadding ->
                        val screenModifier = Modifier.padding(innerPadding)
                        val poiId = detailPoiId
                        val reduceMotion = rememberReduceMotion()

                        // PB10: transisi antar layar (fade-through antar tab, geser saat masuk/keluar layar dalam).
                        val screen = when {
                            !onboardingDone -> ScreenKey("onboarding", -1)
                            !authDecided -> ScreenKey("auth-first", -1)
                            showAuth -> ScreenKey("auth", 3)
                            poiId != null -> ScreenKey("detail:$poiId", 4)
                            showProfile || firstTimeProfile -> ScreenKey("profile", 2)
                            showSettings -> ScreenKey("settings", 1)
                            showSearch -> ScreenKey("search", 1)
                            else -> ScreenKey("tab:${tab.name}", 0)
                        }
                        AnimatedContent(
                            targetState = screen,
                            transitionSpec = screenTransition(reduceMotion),
                            label = "screen",
                        ) { key ->
                            when {
                                // PB10: onboarding + penjelasan izin, sekali saja.
                                key.id == "onboarding" -> OnboardingScreen(onFinish = {}, modifier = screenModifier)

                                // CL-03: Masuk / Daftar / Lanjut tanpa akun, sekali setelah onboarding.
                                key.id == "auth-first" -> AuthScreen(onDone = {}, modifier = screenModifier)

                                key.id == "auth" -> AuthScreen(
                                    onDone = { showAuth = false },
                                    onBack = { showAuth = false },
                                    modifier = screenModifier,
                                )

                                key.id.startsWith("detail:") -> PoiDetailScreen(
                                    poiId = key.id.removePrefix("detail:"),
                                    onBack = { detailPoiId = null },
                                    onNavigate = startNavigation,
                                    modifier = screenModifier,
                                )

                                key.id == "profile" -> ProfileScreen(
                                    initial = profile,
                                    onSave = { newProfile ->
                                        ProfileStore.save(this@MainActivity, newProfile)
                                        showProfile = false
                                        showSettings = false
                                        tab = MainTab.ROUTE   // langsung tampilkan "rute awal"
                                    },
                                    onSkip = {
                                        if (firstTimeProfile) ProfileStore.skip(this@MainActivity)
                                        showProfile = false
                                    },
                                    modifier = screenModifier,
                                )

                                key.id == "settings" -> SettingsScreen(
                                    onBack = { showSettings = false },
                                    onEditProfile = { showProfile = true },
                                    onOpenAuth = { showAuth = true },
                                    modifier = screenModifier,
                                )

                                key.id == "search" -> SearchScreen(
                                    onBack = { showSearch = false },
                                    onOpenDetail = openDetail,
                                    modifier = screenModifier,
                                )

                                else -> when (MainTab.valueOf(key.id.removePrefix("tab:"))) {
                                    MainTab.EXPLORE -> LocationScreen(
                                        onOpenDetail = openDetail,
                                        onOpenSearch = { showSearch = true },
                                        onOpenSettings = { showSettings = true },
                                        onNavigate = startNavigation,
                                        onOpenRoute = { tab = MainTab.ROUTE },
                                        modifier = screenModifier,
                                    )

                                    MainTab.ROUTE -> RecommendationScreen(
                                        onOpenDetail = openDetail,
                                        onEditProfile = { showProfile = true },
                                        onStartRoute = startRoute,
                                        modifier = screenModifier,
                                    )

                                    MainTab.TRAIL -> GameScreen(
                                        onOpenDetail = openDetail,
                                        modifier = screenModifier,
                                    )

                                    MainTab.HISTORY -> HistoryScreen(modifier = screenModifier)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
