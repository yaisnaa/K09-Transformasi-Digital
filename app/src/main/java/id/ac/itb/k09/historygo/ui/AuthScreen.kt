package id.ac.itb.k09.historygo.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import id.ac.itb.k09.historygo.R
import id.ac.itb.k09.historygo.account.AccountError
import id.ac.itb.k09.historygo.account.AccountStore
import id.ac.itb.k09.historygo.i18n.LocalStrings
import id.ac.itb.k09.historygo.ui.components.DecoDivider
import id.ac.itb.k09.historygo.ui.components.HgTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Masuk / Daftar akun lokal (CL-03). Tampil sekali setelah onboarding, atau dari Pengaturan.
 * @param onBack null = tampil sebagai langkah awal (ada "Lanjut tanpa akun"); selain itu dari Pengaturan.
 */
@Composable
fun AuthScreen(onDone: () -> Unit, onBack: (() -> Unit)? = null, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val s = LocalStrings.current
    val colors = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()

    var signUpMode by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    // Password sengaja TIDAK disimpan saat layar diputar (bukan rememberSaveable).
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<AccountError?>(null) }
    var busy by remember { mutableStateOf(false) }

    BackHandler(enabled = onBack != null) { onBack?.invoke() }

    val submit: () -> Unit = {
        if (!busy) {
            busy = true
            scope.launch {
                // PBKDF2 berat: dihitung di luar thread layar.
                val result = withContext(Dispatchers.Default) {
                    if (signUpMode) AccountStore.signUp(context, name, email, password, confirm)
                    else AccountStore.signIn(context, email, password)
                }
                busy = false
                error = result
                if (result == null) onDone()
            }
        }
    }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = colors.surfaceContainer,
        unfocusedContainerColor = colors.surfaceContainer,
        unfocusedBorderColor = colors.outlineVariant,
        focusedBorderColor = colors.primary,
        cursorColor = colors.primary,
        focusedLabelColor = colors.primary,
    )

    Column(modifier.fillMaxSize().imePadding()) {
        if (onBack != null) HgTopBar(title = s.settingsAccount, onBack = onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(if (signUpMode) s.signUpTitle else s.signInTitle, style = MaterialTheme.typography.displaySmall, color = colors.onSurface)
            DecoDivider()
            Text(s.authIntro, style = MaterialTheme.typography.bodyLarge, color = colors.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))

            if (signUpMode) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it; error = null }, label = { Text(s.fieldName) },
                    singleLine = true, shape = MaterialTheme.shapes.medium, colors = fieldColors,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            OutlinedTextField(
                value = email, onValueChange = { email = it; error = null }, label = { Text(s.fieldEmail) },
                singleLine = true, shape = MaterialTheme.shapes.medium, colors = fieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password, onValueChange = { password = it; error = null }, label = { Text(s.fieldPassword) },
                singleLine = true, shape = MaterialTheme.shapes.medium, colors = fieldColors,
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = if (signUpMode) ImeAction.Next else ImeAction.Done),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            painterResource(R.drawable.ic_eye), contentDescription = if (showPassword) s.hidePassword else s.showPassword,
                            tint = if (showPassword) colors.primary else colors.outline,
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
            if (signUpMode) {
                OutlinedTextField(
                    value = confirm, onValueChange = { confirm = it; error = null }, label = { Text(s.fieldConfirmPassword) },
                    singleLine = true, shape = MaterialTheme.shapes.medium, colors = fieldColors,
                    visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            error?.let {
                Text(
                    s.accountError(it),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onErrorContainer,
                    modifier = Modifier.fillMaxWidth().background(colors.errorContainer, MaterialTheme.shapes.small).padding(12.dp),
                )
            }

            Button(onClick = submit, enabled = !busy, modifier = Modifier.fillMaxWidth().height(56.dp), shape = MaterialTheme.shapes.medium) {
                if (busy) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colors.onPrimary)
                else Text(if (signUpMode) s.signUp else s.signIn, style = MaterialTheme.typography.labelLarge)
            }
            TextButton(onClick = { signUpMode = !signUpMode; error = null; confirm = "" }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Text(if (signUpMode) s.haveAccount else s.noAccountYet, style = MaterialTheme.typography.labelLarge, color = colors.tertiary)
            }
        }
        if (onBack == null) {
            TextButton(
                onClick = { AccountStore.continueAsGuest(context); onDone() },
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(bottom = 8.dp),
            ) { Text(s.continueAsGuest, style = MaterialTheme.typography.labelLarge, color = colors.onSurfaceVariant) }
        }
    }
}
