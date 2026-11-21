package id.ac.itb.k09.historygo.i18n

import id.ac.itb.k09.historygo.data.AppLanguage
import id.ac.itb.k09.historygo.data.PoiFilter
import id.ac.itb.k09.historygo.navigation.RouteError

/**
 * Semua teks antarmuka (A01). Setiap bahasa wajib mengisi SEMUA teks:
 * kalau ada yang terlewat, aplikasi tidak bisa di-build. Kotlin murni.
 */
interface AppStrings {
    // Umum
    val back: String
    val close: String
    val change: String
    val switchLanguage: String          // label tombol untuk pindah ke bahasa LAIN

    // Navigasi utama dan Pengaturan (PB10)
    val tabExplore: String
    val tabRoute: String
    val tabTrail: String
    val tabHistory: String
    val settingsTitle: String
    val settingsVisitPlan: String
    val settingsVisitPlanUsed: String
    val settingsVisitPlanEmpty: String
    fun settingsVisitPlanSummary(minutes: Int, interests: String): String
    val settingsLanguage: String
    val languageIndonesian: String
    val languageEnglish: String
    val settingsStoriesDetection: String
    val autoplayHelp: String
    val detectionLabel: String
    val detectionHelp: String
    val aboutContent: String
    val notValidatedChip: String
    val dummyChip: String
    val aboutNotValidated: String
    val aboutDummy: String
    val aboutSources: String

    // Peta (Explore Map)
    val permissionNeeded: String
    val allowLocation: String
    val gpsOff: String
    val enableGps: String
    val searchingLocation: String
    val mapsKeyMissing: String
    fun poisWithoutCoordinates(count: Int): String
    val searchPoi: String
    val recommendations: String
    fun visitedPois(count: Int): String
    val historyTrail: String
    val autoplayNarration: String
    fun markerSnippet(category: String): String
    val yourPosition: String

    // Kartu POI
    val youAreNear: String
    val noSummary: String
    val notValidated: String
    val dummyShort: String
    val seeDetails: String

    // POI Detail
    val poiNotFound: String
    val dummyLong: String
    val navigateHere: String
    val navigateNoCoordinates: String
    val sectionPhoto: String
    val sectionHistory: String
    val sectionInfographic: String
    val sectionNarration: String
    val sectionSources: String
    val noAudio: String
    val noSources: String
    val photoUnavailable: String
    val photoLoading: String
    fun photoNotFound(path: String): String
    fun photoDescription(name: String): String
    val infoYearBuilt: String
    val infoArchitect: String
    val infoStyle: String
    val infoRole: String
    val infoEmpty: String
    val playNarration: String
    val pauseNarration: String

    // Riwayat
    val historyTitle: String
    val noHistory: String
    val clearHistory: String
    val detectedInBackground: String
    val detectedInForeground: String

    // Pencarian (F05)
    val searchTitle: String
    val searchHint: String
    val filterAll: String
    fun filterLabel(filter: PoiFilter): String
    val noLocationSortedByName: String
    val notFound: String
    fun distanceAndWalk(meters: Int, minutes: Int): String

    // Navigasi (F06)
    val navArrived: String
    fun navRouteHeader(current: Int, total: Int): String
    val navWalkingTo: String
    fun navReached(name: String): String
    fun navAllVisited(total: Int): String
    fun navArrivedAt(name: String): String
    fun routeError(error: RouteError): String
    val navWaitingLocation: String
    val navRerouting: String
    val navCalculating: String
    fun navProgressMulti(toNext: Int, total: Int, minutes: Int): String
    fun navProgress(total: Int, minutes: Int): String
    fun navRerouteCount(count: Int): String
    val retry: String
    val done: String
    val endNavigation: String

    // Profil dan rekomendasi (F07)
    val profileTitle: String
    val profileIntro: String
    val profileDurationQuestion: String
    fun minutes(value: Int): String
    val profileInterestQuestion: String
    val skip: String
    val seeRecommendations: String
    val recTitle: String
    val recNeedProfile: String
    val fillProfile: String
    val recNoLocation: String
    fun recVisitedExcluded(count: Int): String
    fun recEmpty(duration: Int): String
    fun recLine(meters: Int, walk: Int, finishAt: Int): String
    val recMatchesInterest: String
    val recFiller: String
    fun recTotal(total: Int, duration: Int, visit: Int): String
    fun startRoute(count: Int): String

    // Jejak Sejarah (F08)
    val gameIntro: String
    fun missionsDone(done: Int, total: Int): String
    fun points(value: Int): String
    val badgeUnlocked: String
    val badgeLocked: String
    val badgeName: String
    val badgeUnlockedText: String
    fun badgeLockedText(remaining: Int): String
    val statusNotCheckedIn: String
    val statusQuizPending: String
    val statusQuizCorrect: String
    val statusQuizWrong: String
    val resetMissions: String
    val missionCardTitle: String
    val checkInNoLocation: String
    val checkInReady: String
    fun checkInTooFar(distance: Int, radius: Int): String
    val checkIn: String
    fun checkInSuccess(points: Int): String
    fun checkInStillFar(distance: Int): String
    val locationNotRead: String
    fun answerCorrect(points: Int): String
    fun answerWrong(correctAnswer: String): String
    fun missionDone(correct: Boolean?): String
    val quizDummy: String
    val answer: String

    // Notifikasi dan narasi
    val channelTrackingName: String
    val channelTrackingDescription: String
    val channelPoiName: String
    val channelPoiDescription: String
    val trackingTitle: String
    val trackingText: String
    val stop: String
    fun nearPoiTitle(name: String): String
    val narrationArtist: String

    companion object {
        fun of(language: AppLanguage): AppStrings = when (language) {
            AppLanguage.ID -> IdStrings
            AppLanguage.EN -> EnStrings
        }
    }
}

object IdStrings : AppStrings {
    override val back = "← Kembali"
    override val close = "Tutup"
    override val change = "Ubah"

    override val tabExplore = "Jelajah"
    override val tabRoute = "Rute"
    override val tabTrail = "Jejak Sejarah"
    override val tabHistory = "Riwayat"
    override val settingsTitle = "Pengaturan"
    override val settingsVisitPlan = "Rencana kunjungan"
    override val settingsVisitPlanUsed = "Dipakai untuk rute rekomendasi"
    override val settingsVisitPlanEmpty = "Belum diisi"
    override fun settingsVisitPlanSummary(minutes: Int, interests: String) = "$minutes menit · $interests"
    override val settingsLanguage = "Bahasa"
    override val languageIndonesian = "Indonesia"
    override val languageEnglish = "English"
    override val settingsStoriesDetection = "Cerita dan deteksi"
    override val autoplayHelp = "Cerita langsung dibacakan saat kamu tiba di sebuah bangunan."
    override val detectionLabel = "Deteksi bangunan sekitar"
    override val detectionHelp = "Matikan untuk menghentikan pembacaan GPS dan notifikasi."
    override val aboutContent = "Tentang konten"
    override val notValidatedChip = "Belum tervalidasi"
    override val dummyChip = "Konten contoh"
    override val aboutNotValidated = "Konten belum diperiksa dan disahkan oleh tim."
    override val aboutDummy = "Teks sementara, bukan data sejarah asli. Akan diganti sebelum rilis."
    override val aboutSources = "Sumber rujukan tercantum di bagian bawah setiap halaman tempat."
    override val switchLanguage = "🌐 English"

    override val permissionNeeded = "HistoryGo butuh izin lokasi presisi untuk mengetahui bangunan bersejarah di sekitarmu."
    override val allowLocation = "Izinkan lokasi"
    override val gpsOff = "GPS sedang mati. Nyalakan lokasi agar posisimu terbaca."
    override val enableGps = "Aktifkan GPS"
    override val searchingLocation = "Mencari lokasi…"
    override val mapsKeyMissing = "MAPS_API_KEY belum diisi di local.properties, peta tidak bisa tampil."
    override fun poisWithoutCoordinates(count: Int) = "$count POI belum punya koordinat, belum tampil di peta."
    override val searchPoi = "Cari POI"
    override val recommendations = "Rekomendasi"
    override fun visitedPois(count: Int) = "POI yang dilewati ($count)"
    override val historyTrail = "Jejak Sejarah"
    override val autoplayNarration = "Putar narasi otomatis"
    override fun markerSnippet(category: String) = "$category · ketuk untuk detail"
    override val yourPosition = "Posisi kamu"

    override val youAreNear = "Kamu sedang di dekat"
    override val noSummary = "Ringkasan sejarah belum tersedia."
    override val notValidated = "Konten belum divalidasi tim."
    override val dummyShort = "Konten contoh (dummy)."
    override val seeDetails = "Lihat detail"

    override val poiNotFound = "POI tidak ditemukan."
    override val dummyLong = "Konten contoh (dummy), bukan data sejarah asli."
    override val navigateHere = "Navigasi ke sini"
    override val navigateNoCoordinates = "Navigasi (koordinat belum ada)"
    override val sectionPhoto = "Foto"
    override val sectionHistory = "Sejarah singkat"
    override val sectionInfographic = "Infografis"
    override val sectionNarration = "Narasi audio"
    override val sectionSources = "Sumber"
    override val noAudio = "Audio belum tersedia."
    override val noSources = "Sumber rujukan belum dicantumkan."
    override val photoUnavailable = "Foto belum tersedia."
    override val photoLoading = "Memuat foto…"
    override fun photoNotFound(path: String) = "Foto tidak ditemukan: $path"
    override fun photoDescription(name: String) = "Foto $name"
    override val infoYearBuilt = "Tahun dibangun"
    override val infoArchitect = "Arsitek"
    override val infoStyle = "Gaya arsitektur"
    override val infoRole = "Peran historis"
    override val infoEmpty = "belum diisi"
    override val playNarration = "▶  Putar narasi"
    override val pauseNarration = "⏸  Jeda narasi"

    override val historyTitle = "POI yang dilewati"
    override val noHistory = "Belum ada POI yang dilewati."
    override val clearHistory = "Hapus riwayat"
    override val detectedInBackground = " · terdeteksi di latar"
    override val detectedInForeground = " · terdeteksi saat aplikasi terbuka"

    override val searchTitle = "Cari POI"
    override val searchHint = "Nama tempat, mis. merdeka"
    override val filterAll = "Semua"
    override fun filterLabel(filter: PoiFilter) = when (filter) {
        PoiFilter.HERITAGE -> "Heritage"
        PoiFilter.SPOT_FOTO -> "Spot Foto"
        PoiFilter.ISTIRAHAT_KULINER -> "Istirahat/Kuliner"
    }
    override val noLocationSortedByName = "Lokasi belum terbaca: hasil diurutkan menurut nama, tanpa jarak."
    override val notFound = "POI tidak ditemukan."
    override fun distanceAndWalk(meters: Int, minutes: Int) = "$meters m · ±$minutes menit jalan kaki"

    override val navArrived = "Tiba di tujuan"
    override fun navRouteHeader(current: Int, total: Int) = "Rute rekomendasi · $current dari $total"
    override val navWalkingTo = "Navigasi jalan kaki ke"
    override fun navReached(name: String) = "Sudah sampai di $name. Lanjut ke titik berikutnya."
    override fun navAllVisited(total: Int) = "Semua $total tempat rekomendasi sudah dikunjungi."
    override fun navArrivedAt(name: String) = "Kamu sudah sampai di $name."
    override fun routeError(error: RouteError) = "Gagal mengambil rute. " + when (error) {
        RouteError.NO_API_KEY -> "MAPS_API_KEY belum diisi di local.properties."
        RouteError.NOT_AUTHORIZED -> "Routes API belum aktif atau API key belum diizinkan memakai Routes API."
        RouteError.QUOTA -> "Kuota Routes API habis. Coba lagi nanti."
        RouteError.SERVICE -> "Layanan rute sedang bermasalah."
        RouteError.NO_ROUTE -> "Rute jalan kaki tidak ditemukan."
        RouteError.NO_INTERNET -> "Tidak ada koneksi internet."
        RouteError.UNKNOWN -> "Terjadi kesalahan."
    }
    override val navWaitingLocation = "Menunggu lokasi… pastikan izin lokasi dan GPS aktif."
    override val navRerouting = "Kamu keluar dari rute, menghitung ulang…"
    override val navCalculating = "Menghitung rute jalan kaki…"
    override fun navProgressMulti(toNext: Int, total: Int, minutes: Int) =
        "±$toNext m lagi ke titik ini · sisa total $total m · ±$minutes menit"
    override fun navProgress(total: Int, minutes: Int) = "Sisa $total m · ±$minutes menit"
    override fun navRerouteCount(count: Int) = "Rute sudah dihitung ulang $count×"
    override val retry = "Coba lagi"
    override val done = "Selesai"
    override val endNavigation = "Akhiri navigasi"

    override val profileTitle = "Ceritakan rencanamu"
    override val profileIntro = "HistoryGo akan menyarankan hingga 3 tempat yang cocok dan muat dalam waktumu."
    override val profileDurationQuestion = "Berapa lama waktumu di kawasan Asia Afrika?"
    override fun minutes(value: Int) = "$value menit"
    override val profileInterestQuestion = "Apa yang ingin kamu lihat? (boleh lebih dari satu)"
    override val skip = "Lewati"
    override val seeRecommendations = "Lihat rekomendasi"
    override val recTitle = "Rekomendasi untukmu"
    override val recNeedProfile = "Isi profil singkat dulu supaya HistoryGo bisa memberi rekomendasi."
    override val fillProfile = "Isi profil"
    override val recNoLocation = "Lokasi belum terbaca: jarak dihitung dari tengah kawasan."
    override fun recVisitedExcluded(count: Int) = "$count POI yang sudah dilewati tidak direkomendasikan lagi."
    override fun recEmpty(duration: Int) =
        "Belum ada POI yang muat dalam $duration menit, atau semua POI sudah dilewati. Coba durasi yang lebih panjang."
    override fun recLine(meters: Int, walk: Int, finishAt: Int) =
        "$meters m · ±$walk menit jalan · selesai di menit ke-$finishAt"
    override val recMatchesInterest = "Sesuai minatmu"
    override val recFiller = "Tempat terdekat yang masih muat"
    override fun recTotal(total: Int, duration: Int, visit: Int) =
        "Total ±$total dari $duration menit (termasuk ±$visit menit di setiap tempat). " +
            "Urutan dimulai dari tempat terdekat. Waktu jalan diperkirakan dari jarak garis lurus."
    override fun startRoute(count: Int) = "Mulai rute ($count tempat)"

    override val gameIntro = "Datangi setiap tempat, check-in di lokasi, lalu jawab kuisnya."
    override fun missionsDone(done: Int, total: Int) = "Misi selesai: $done/$total"
    override fun points(value: Int) = "Poin: $value"
    override val badgeUnlocked = "🏅 Badge terbuka"
    override val badgeLocked = "🔒 Badge terkunci"
    override val badgeName = "Penjelajah Jejak Sejarah"
    override val badgeUnlockedText = "Selamat! Kamu sudah menyelesaikan semua misi di koridor Asia Afrika."
    override fun badgeLockedText(remaining: Int) = "Selesaikan $remaining misi lagi untuk membuka badge ini."
    override val statusNotCheckedIn = "Belum check-in"
    override val statusQuizPending = "Sudah check-in · kuis belum dijawab"
    override val statusQuizCorrect = "Check-in ✓ · kuis benar"
    override val statusQuizWrong = "Check-in ✓ · kuis salah"
    override val resetMissions = "Ulangi misi dari awal"
    override val missionCardTitle = "Misi Jejak Sejarah"
    override val checkInNoLocation = "Lokasi belum terbaca. Check-in hanya bisa dilakukan di lokasi."
    override val checkInReady = "Kamu sudah di lokasi. Check-in untuk membuka kuis!"
    override fun checkInTooFar(distance: Int, radius: Int) =
        "Kamu ±$distance m dari sini. Mendekatlah sampai $radius m untuk check-in."
    override val checkIn = "Check-in"
    override fun checkInSuccess(points: Int) = "Check-in berhasil! +$points poin"
    override fun checkInStillFar(distance: Int) = "Masih ±$distance m, belum di dalam radius."
    override val locationNotRead = "Lokasi belum terbaca."
    override fun answerCorrect(points: Int) = "Benar! +$points poin"
    override fun answerWrong(correctAnswer: String) = "Kurang tepat. Jawabannya: $correctAnswer"
    override fun missionDone(correct: Boolean?) = "Misi selesai ✓" + when (correct) {
        true -> " · kuis dijawab benar"
        false -> " · kuis dijawab salah"
        null -> ""
    }
    override val quizDummy = "Soal contoh (dummy)."
    override val answer = "Jawab"

    override val channelTrackingName = "Deteksi lokasi"
    override val channelTrackingDescription = "Notifikasi permanen saat HistoryGo mendeteksi POI"
    override val channelPoiName = "POI di sekitar"
    override val channelPoiDescription = "Pemberitahuan saat kamu berada di dekat bangunan bersejarah"
    override val trackingTitle = "HistoryGo aktif"
    override val trackingText = "Mendeteksi bangunan bersejarah di sekitarmu"
    override val stop = "Berhenti"
    override fun nearPoiTitle(name: String) = "Kamu di dekat $name"
    override val narrationArtist = "HistoryGo · Narasi sejarah"
}

object EnStrings : AppStrings {
    override val back = "← Back"
    override val close = "Close"
    override val change = "Edit"

    override val tabExplore = "Explore"
    override val tabRoute = "Route"
    override val tabTrail = "History Trail"
    override val tabHistory = "History"
    override val settingsTitle = "Settings"
    override val settingsVisitPlan = "Visit plan"
    override val settingsVisitPlanUsed = "Used for recommended routes"
    override val settingsVisitPlanEmpty = "Not set yet"
    override fun settingsVisitPlanSummary(minutes: Int, interests: String) = "$minutes min · $interests"
    override val settingsLanguage = "Language"
    override val languageIndonesian = "Indonesia"
    override val languageEnglish = "English"
    override val settingsStoriesDetection = "Stories and detection"
    override val autoplayHelp = "Stories play automatically when you arrive at a building."
    override val detectionLabel = "Detect nearby buildings"
    override val detectionHelp = "Turn off to stop GPS and notifications."
    override val aboutContent = "About the content"
    override val notValidatedChip = "Not yet validated"
    override val dummyChip = "Sample content"
    override val aboutNotValidated = "Content not yet reviewed and approved by the team."
    override val aboutDummy = "Placeholder text, not real historical data. Will be replaced before release."
    override val aboutSources = "References are listed at the bottom of each place page."
    override val switchLanguage = "🌐 Bahasa Indonesia"

    override val permissionNeeded = "HistoryGo needs precise location access to find historic buildings around you."
    override val allowLocation = "Allow location"
    override val gpsOff = "GPS is off. Turn on location so your position can be found."
    override val enableGps = "Turn on GPS"
    override val searchingLocation = "Finding your location…"
    override val mapsKeyMissing = "MAPS_API_KEY is not set in local.properties, so the map cannot load."
    override fun poisWithoutCoordinates(count: Int) =
        "$count place${if (count == 1) "" else "s"} without coordinates, not shown on the map yet."
    override val searchPoi = "Search"
    override val recommendations = "Recommendations"
    override fun visitedPois(count: Int) = "Visited places ($count)"
    override val historyTrail = "History Trail"
    override val autoplayNarration = "Auto-play narration"
    override fun markerSnippet(category: String) = "$category · tap for details"
    override val yourPosition = "Your location"

    override val youAreNear = "You are near"
    override val noSummary = "History summary not available yet."
    override val notValidated = "Content not yet validated by the team."
    override val dummyShort = "Sample content (dummy)."
    override val seeDetails = "See details"

    override val poiNotFound = "Place not found."
    override val dummyLong = "Sample content (dummy), not real historical data."
    override val navigateHere = "Navigate here"
    override val navigateNoCoordinates = "Navigate (no coordinates yet)"
    override val sectionPhoto = "Photo"
    override val sectionHistory = "Brief history"
    override val sectionInfographic = "Infographic"
    override val sectionNarration = "Audio narration"
    override val sectionSources = "Sources"
    override val noAudio = "Audio not available yet."
    override val noSources = "No references listed yet."
    override val photoUnavailable = "Photo not available yet."
    override val photoLoading = "Loading photo…"
    override fun photoNotFound(path: String) = "Photo not found: $path"
    override fun photoDescription(name: String) = "Photo of $name"
    override val infoYearBuilt = "Year built"
    override val infoArchitect = "Architect"
    override val infoStyle = "Architectural style"
    override val infoRole = "Historical role"
    override val infoEmpty = "not filled yet"
    override val playNarration = "▶  Play narration"
    override val pauseNarration = "⏸  Pause narration"

    override val historyTitle = "Visited places"
    override val noHistory = "No places visited yet."
    override val clearHistory = "Clear history"
    override val detectedInBackground = " · detected in the background"
    override val detectedInForeground = " · detected while the app was open"

    override val searchTitle = "Search places"
    override val searchHint = "Place name, e.g. merdeka"
    override val filterAll = "All"
    override fun filterLabel(filter: PoiFilter) = when (filter) {
        PoiFilter.HERITAGE -> "Heritage"
        PoiFilter.SPOT_FOTO -> "Photo spot"
        PoiFilter.ISTIRAHAT_KULINER -> "Rest & food"
    }
    override val noLocationSortedByName = "Location not available: results sorted by name, without distance."
    override val notFound = "No places found."
    override fun distanceAndWalk(meters: Int, minutes: Int) = "$meters m · ~$minutes min walk"

    override val navArrived = "Arrived"
    override fun navRouteHeader(current: Int, total: Int) = "Recommended route · $current of $total"
    override val navWalkingTo = "Walking directions to"
    override fun navReached(name: String) = "You reached $name. On to the next stop."
    override fun navAllVisited(total: Int) = "You have visited all $total recommended places."
    override fun navArrivedAt(name: String) = "You have arrived at $name."
    override fun routeError(error: RouteError) = "Could not get a route. " + when (error) {
        RouteError.NO_API_KEY -> "MAPS_API_KEY is not set in local.properties."
        RouteError.NOT_AUTHORIZED -> "Routes API is not enabled, or the API key is not allowed to use it."
        RouteError.QUOTA -> "Routes API quota exceeded. Please try again later."
        RouteError.SERVICE -> "The routing service is having problems."
        RouteError.NO_ROUTE -> "No walking route found."
        RouteError.NO_INTERNET -> "No internet connection."
        RouteError.UNKNOWN -> "Something went wrong."
    }
    override val navWaitingLocation = "Waiting for location… make sure location permission and GPS are on."
    override val navRerouting = "You left the route, recalculating…"
    override val navCalculating = "Calculating walking route…"
    override fun navProgressMulti(toNext: Int, total: Int, minutes: Int) =
        "~$toNext m to this stop · $total m left in total · ~$minutes min"
    override fun navProgress(total: Int, minutes: Int) = "$total m left · ~$minutes min"
    override fun navRerouteCount(count: Int) = "Route recalculated $count×"
    override val retry = "Try again"
    override val done = "Done"
    override val endNavigation = "End navigation"

    override val profileTitle = "Tell us your plan"
    override val profileIntro = "HistoryGo will suggest up to 3 places that match your interests and fit your time."
    override val profileDurationQuestion = "How much time do you have in the Asia Afrika area?"
    override fun minutes(value: Int) = "$value min"
    override val profileInterestQuestion = "What would you like to see? (choose one or more)"
    override val skip = "Skip"
    override val seeRecommendations = "See recommendations"
    override val recTitle = "Recommended for you"
    override val recNeedProfile = "Fill in a short profile first so HistoryGo can recommend places."
    override val fillProfile = "Fill in profile"
    override val recNoLocation = "Location not available: distances are measured from the center of the area."
    override fun recVisitedExcluded(count: Int) =
        "$count visited place${if (count == 1) " is" else "s are"} no longer recommended."
    override fun recEmpty(duration: Int) =
        "No place fits in $duration minutes, or you have visited them all. Try a longer duration."
    override fun recLine(meters: Int, walk: Int, finishAt: Int) =
        "$meters m · ~$walk min walk · done at minute $finishAt"
    override val recMatchesInterest = "Matches your interests"
    override val recFiller = "Nearest place that still fits"
    override fun recTotal(total: Int, duration: Int, visit: Int) =
        "Total ~$total of $duration minutes (including ~$visit minutes at each place). " +
            "The order starts from the nearest place. Walking time is estimated from straight-line distance."
    override fun startRoute(count: Int) = "Start route ($count places)"

    override val gameIntro = "Visit each place, check in on site, then answer its quiz."
    override fun missionsDone(done: Int, total: Int) = "Missions completed: $done/$total"
    override fun points(value: Int) = "Points: $value"
    override val badgeUnlocked = "🏅 Badge unlocked"
    override val badgeLocked = "🔒 Badge locked"
    override val badgeName = "History Trail Explorer"
    override val badgeUnlockedText = "Congratulations! You have completed every mission in the Asia Afrika corridor."
    override fun badgeLockedText(remaining: Int) =
        "Complete $remaining more mission${if (remaining == 1) "" else "s"} to unlock this badge."
    override val statusNotCheckedIn = "Not checked in"
    override val statusQuizPending = "Checked in · quiz not answered"
    override val statusQuizCorrect = "Checked in ✓ · quiz correct"
    override val statusQuizWrong = "Checked in ✓ · quiz wrong"
    override val resetMissions = "Restart missions"
    override val missionCardTitle = "History Trail mission"
    override val checkInNoLocation = "Location not available. You can only check in on site."
    override val checkInReady = "You are on site. Check in to unlock the quiz!"
    override fun checkInTooFar(distance: Int, radius: Int) =
        "You are ~$distance m away. Get within $radius m to check in."
    override val checkIn = "Check in"
    override fun checkInSuccess(points: Int) = "Checked in! +$points points"
    override fun checkInStillFar(distance: Int) = "Still ~$distance m away, not within range yet."
    override val locationNotRead = "Location not available yet."
    override fun answerCorrect(points: Int) = "Correct! +$points points"
    override fun answerWrong(correctAnswer: String) = "Not quite. The answer is: $correctAnswer"
    override fun missionDone(correct: Boolean?) = "Mission complete ✓" + when (correct) {
        true -> " · quiz answered correctly"
        false -> " · quiz answered incorrectly"
        null -> ""
    }
    override val quizDummy = "Sample question (dummy)."
    override val answer = "Answer"

    override val channelTrackingName = "Location detection"
    override val channelTrackingDescription = "Ongoing notification while HistoryGo detects nearby places"
    override val channelPoiName = "Nearby places"
    override val channelPoiDescription = "Alerts when you are near a historic building"
    override val trackingTitle = "HistoryGo is active"
    override val trackingText = "Detecting historic buildings around you"
    override val stop = "Stop"
    override fun nearPoiTitle(name: String) = "You are near $name"
    override val narrationArtist = "HistoryGo · History narration"
}
