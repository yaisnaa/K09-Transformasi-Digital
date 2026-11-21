package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import id.ac.itb.k09.historygo.account.AccountStore
import id.ac.itb.k09.historygo.data.AppLanguage
import id.ac.itb.k09.historygo.data.ProfileStore
import id.ac.itb.k09.historygo.data.SettingsStore
import id.ac.itb.k09.historygo.i18n.LanguageStore
import id.ac.itb.k09.historygo.i18n.LocalLanguage
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.location.TriggerService
import id.ac.itb.k09.historygo.location.hasLocationPermission
import id.ac.itb.k09.historygo.location.isGpsEnabled
import id.ac.itb.k09.historygo.ui.components.HgTopBar
import id.ac.itb.k09.historygo.ui.components.StatusChip

/**
 * Layar Pengaturan (PB10): rencana kunjungan, bahasa (A01), autoplay narasi (F04),
 * deteksi bangunan, dan penjelasan label konten (N01).
 */
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onEditProfile: () -> Unit,
    onOpenAuth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val s = LocalStrings.current
    val language = LocalLanguage.current
    BackHandler(onBack = onBack)

    LaunchedEffect(Unit) { SettingsStore.load(context) }
    val autoplay by SettingsStore.autoplay.collectAsStateWithLifecycle()
    val detection by SettingsStore.detection.collectAsStateWithLifecycle()
    val profile by ProfileStore.profile.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { AccountStore.load(context) }
    val account by AccountStore.current.collectAsStateWithLifecycle()
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        HgTopBar(title = s.settingsTitle, onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(28.dp),
        ) {
            // ---------- Akun (CL-03) ----------
            Section(title = s.settingsAccount) {
                SettingsCard {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(account?.name ?: s.guestMode, style = MaterialTheme.typography.titleMedium)
                            Text(
                                account?.email ?: s.guestModeHint,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (account == null) {
                            TextButton(onClick = onOpenAuth) { Text(s.signInOrUp, color = MaterialTheme.colorScheme.tertiary) }
                        } else {
                            TextButton(onClick = { confirmSignOut = true }) { Text(s.signOut, color = MaterialTheme.colorScheme.error) }
                        }
                    }
                }
            }

            // ---------- Rencana kunjungan (F07) ----------
            Section(title = s.settingsVisitPlan) {
                SettingsCard {
                    Row(
                        modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 12.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            val summary = profile?.let { p ->
                                s.settingsVisitPlanSummary(p.durationMinutes, p.interests.joinToString(", ") { s.filterLabel(it) })
                            } ?: s.settingsVisitPlanEmpty
                            Text(summary, style = MaterialTheme.typography.titleMedium)
                            Text(
                                s.settingsVisitPlanUsed,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        TextButton(onClick = onEditProfile) {
                            Text(s.change, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }

            // ---------- Bahasa (A01) ----------
            Section(title = s.settingsLanguage) {
                val options = listOf(AppLanguage.ID to s.languageIndonesian, AppLanguage.EN to s.languageEnglish)
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    options.forEachIndexed { index, (lang, label) ->
                        SegmentedButton(
                            selected = language == lang,
                            onClick = {
                                if (language != lang) {
                                    LanguageStore.set(context, lang)
                                    // Notifikasi permanen ikut diperbarui ke bahasa baru.
                                    if (hasLocationPermission(context) && isGpsEnabled(context)) TriggerService.start(context)
                                }
                            },
                            shape = SegmentedButtonDefaults.itemShape(index, options.size),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = MaterialTheme.colorScheme.primary,
                                activeContentColor = MaterialTheme.colorScheme.onPrimary,
                                inactiveContainerColor = MaterialTheme.colorScheme.surface,
                                inactiveContentColor = MaterialTheme.colorScheme.primary,
                            ),
                            icon = {},
                        ) { Text(label, style = MaterialTheme.typography.titleSmall) }
                    }
                }
            }

            // ---------- Cerita dan deteksi (F04, F02) ----------
            Section(title = s.settingsStoriesDetection) {
                SettingsCard {
                    ToggleRow(
                        label = s.autoplayNarration,
                        help = s.autoplayHelp,
                        checked = autoplay,
                        onChange = { SettingsStore.setAutoplay(context, it) },
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    ToggleRow(
                        label = s.detectionLabel,
                        help = s.detectionHelp,
                        checked = detection,
                        onChange = { enabled ->
                            SettingsStore.setDetection(context, enabled)
                            if (enabled) {
                                if (hasLocationPermission(context) && isGpsEnabled(context)) TriggerService.start(context)
                            } else {
                                TriggerService.stop(context)
                            }
                        },
                    )
                }
            }

            // ---------- Tentang konten (N01) ----------
            Section(title = s.aboutContent) {
                SettingsCard {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatusChip(s.notValidatedChip)
                        Text(s.aboutNotValidated, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        StatusChip(s.dummyChip)
                        Text(s.aboutDummy, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(s.aboutSources, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // Keluar akun minta konfirmasi; progres tetap tersimpan di akun itu.
    if (confirmSignOut) {
        AlertDialog(
            onDismissRequest = { confirmSignOut = false },
            title = { Text(s.signOutConfirmTitle, style = MaterialTheme.typography.headlineSmall) },
            text = { Text(s.signOutConfirmBody, style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(onClick = { AccountStore.signOut(context); confirmSignOut = false }) {
                    Text(s.signOut, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmSignOut = false }) { Text(s.cancel) } },
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) { Column { content() } }
}

@Composable
private fun ToggleRow(label: String, help: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium)
            Text(help, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(14.dp))
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = MaterialTheme.colorScheme.primary,
                checkedThumbColor = MaterialTheme.colorScheme.inversePrimary,
                uncheckedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline,
            ),
        )
    }
}
