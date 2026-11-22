package id.ac.itb.k09.historygo.ui

import android.Manifest
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.data.SettingsStore
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.ui.components.DecoDivider
import id.ac.itb.k09.historygo.ui.components.rememberAssetBitmap
import id.ac.itb.k09.historygo.ui.theme.HistoryGoTheme

/** Foto pembuka: Gedung Merdeka (P01), sumbernya tercantum di poi.json. */
private const val ONBOARDING_PHOTO = "foto/p01.jpg"

/**
 * Onboarding (PB10, alur PRD no. 1): satu layar pengenalan lalu penjelasan izin
 * SEBELUM dialog izin sistem muncul. Tampil sekali saja (SettingsStore.onboardingDone).
 * "Jelajah tanpa lokasi" langsung ke peta tanpa meminta izin.
 */
@Composable
fun OnboardingScreen(onFinish: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var step by rememberSaveable { mutableIntStateOf(1) }

    // Izin lokasi presisi + notifikasi (Android 13+) diminta bersamaan, setelah dijelaskan.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        SettingsStore.setNotificationAsked(context)
        SettingsStore.setOnboardingDone(context)
        onFinish()
    }
    val finishWithoutLocation = {
        SettingsStore.setOnboardingDone(context)
        onFinish()
    }

    BackHandler(enabled = step == 2) { step = 1 }

    when (step) {
        1 -> IntroStep(modifier, onNext = { step = 2 }, onSkip = finishWithoutLocation)
        else -> PermissionStep(
            modifier,
            onAllow = {
                val permissions = buildList {
                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                    add(Manifest.permission.ACCESS_COARSE_LOCATION)
                    if (Build.VERSION.SDK_INT >= 33) add(Manifest.permission.POST_NOTIFICATIONS)
                }
                permissionLauncher.launch(permissions.toTypedArray())
            },
            onWithoutLocation = finishWithoutLocation,
        )
    }
}

@Composable
private fun IntroStep(modifier: Modifier, onNext: () -> Unit, onSkip: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val gold = HistoryGoTheme.colors.ornament
    Column(modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 24.dp), verticalArrangement = Arrangement.spacedBy(28.dp)) {
            // Foto dalam bingkai lengkung Deco (DESIGN.md, Shapes).
            val arch = RoundedCornerShape(topStart = 180.dp, topEnd = 180.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
            val image = rememberAssetBitmap(ONBOARDING_PHOTO)?.getOrNull()
            Box(
                Modifier.fillMaxWidth().height(400.dp).clip(arch).border(2.dp, gold, arch).background(colors.primaryContainer),
            ) {
                if (image != null) {
                    Image(image, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(s.onboardingTitle, style = MaterialTheme.typography.displaySmall, color = colors.onSurface)
                Text(s.onboardingBody, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            }
        }
        StepDots(current = 1, total = 2)
        Spacer(Modifier.height(18.dp))
        Row(Modifier.fillMaxWidth().padding(bottom = 24.dp), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onSkip, modifier = Modifier.height(52.dp)) {
                Text(s.skip, style = MaterialTheme.typography.labelLarge, color = colors.tertiary)
            }
            Spacer(Modifier.width(12.dp))
            Button(onClick = onNext, modifier = Modifier.weight(1f).height(56.dp), shape = MaterialTheme.shapes.medium) {
                Text(s.next, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun PermissionStep(modifier: Modifier, onAllow: () -> Unit, onWithoutLocation: () -> Unit) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    Column(modifier.fillMaxSize().padding(horizontal = 24.dp)) {
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(top = 72.dp), verticalArrangement = Arrangement.spacedBy(32.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(s.permTitle, style = MaterialTheme.typography.displaySmall, color = colors.onSurface)
                DecoDivider()
            }
            PermissionRow(R.drawable.ic_pin, colors.secondaryContainer, colors.onSecondaryContainer, s.permLocationTitle, s.permLocationBody)
            PermissionRow(R.drawable.ic_bell, colors.primaryContainer, colors.tertiary, s.permNotifTitle, s.permNotifBody)
        }
        StepDots(current = 2, total = 2)
        Spacer(Modifier.height(18.dp))
        Button(onClick = onAllow, modifier = Modifier.fillMaxWidth().height(56.dp), shape = MaterialTheme.shapes.medium) {
            Text(s.allowLocation, style = MaterialTheme.typography.labelLarge)
        }
        TextButton(onClick = onWithoutLocation, modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 4.dp)) {
            Text(s.exploreWithoutLocation, style = MaterialTheme.typography.labelLarge, color = colors.tertiary)
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun PermissionRow(icon: Int, bg: androidx.compose.ui.graphics.Color, fg: androidx.compose.ui.graphics.Color, title: String, body: String) {
    val colors = MaterialTheme.colorScheme
    Row(verticalAlignment = Alignment.Top) {
        Box(Modifier.size(48.dp).background(bg, CircleShape), contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), null, tint = fg, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(16.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = colors.onSurface)
            Text(body, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
        }
    }
}

/** Penanda langkah: belah ketupat penuh untuk langkah aktif, garis untuk lainnya. */
@Composable
private fun StepDots(current: Int, total: Int) {
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    Row(
        Modifier.semantics { contentDescription = s.stepOf(current, total) },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        (1..total).forEach { i ->
            if (i == current) Box(Modifier.size(10.dp).rotate(45f).background(colors.primary))
            else Box(Modifier.size(8.dp).rotate(45f).border(1.5.dp, colors.outline))
        }
    }
}
