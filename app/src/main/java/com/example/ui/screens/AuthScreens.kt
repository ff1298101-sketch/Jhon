package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.repository.JamesChatRepository
import com.example.data.repository.UsernameCheckResult
import com.example.ui.components.AvatarWithStatus
import com.example.ui.components.JamesOfficialLogo
import com.example.ui.components.JamesVBadge
import com.example.ui.theme.JamesBluePrimary
import com.example.ui.theme.JamesOnlineGreen
import com.example.util.JamesI18n
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onNavigateToSignup: () -> Unit,
    onNavigateToChooseUsername: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var selectedCountry by remember { mutableStateOf(com.example.util.JamesCountryUtil.findByCode("HT")) }
    var showCountryDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            JamesOfficialLogo(
                size = 88.dp,
                isVertical = true,
                brandTextSize = 26.sp,
                subtitle = JamesI18n.t("login_title", currentLang),
                animated = true,
                withHalo = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Sélecteur de pays et indicatif téléphonique international
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { showCountryDialog = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("login_country_selector"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedCountry.flag, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${selectedCountry.name} (${selectedCountry.dialCode})",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "Changer",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showCountryDialog) {
                com.example.ui.components.JamesCountryPickerDialog(
                    selectedCountry = selectedCountry,
                    onCountrySelected = { selectedCountry = it },
                    onDismiss = { showCountryDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text("Email, @nom d'utilisateur ou ID James") },
                placeholder = { Text("ex: user@mail.com, @jean, JMS-1042") },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_email_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text(JamesI18n.t("password_label", currentLang)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Afficher mot de passe"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it },
                        modifier = Modifier.testTag("login_remember_checkbox")
                    )
                    Text(
                        text = JamesI18n.t("remember_me", currentLang),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(onClick = { /* Reset password flow */ }) {
                    Text(
                        text = JamesI18n.t("forgot_password", currentLang),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = repository.login(email, password, rememberMe)
                        isLoading = false
                        result.fold(
                            onSuccess = { user ->
                                if (user.username.isEmpty()) {
                                    onNavigateToChooseUsername()
                                } else {
                                    onLoginSuccess()
                                }
                            },
                            onFailure = { err ->
                                errorMessage = err.message ?: "Erreur de connexion"
                            }
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("login_submit_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = JamesI18n.t("login_btn", currentLang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showGoogleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("google_login_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🌐 ${JamesI18n.t("google_login", currentLang)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showGoogleDialog) {
                GoogleAccountSelectorDialog(
                    onDismiss = { showGoogleDialog = false },
                    onAccountSelected = { gEmail, gName, gPhoto ->
                        showGoogleDialog = false
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val res = repository.loginWithGoogle(
                                email = gEmail,
                                displayName = gName,
                                photoUrl = gPhoto
                            )
                            isLoading = false
                            res.fold(
                                onSuccess = { u ->
                                    if (u.username.isEmpty()) onNavigateToChooseUsername() else onLoginSuccess()
                                },
                                onFailure = { err -> errorMessage = err.message ?: "Erreur de connexion Google" }
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            TextButton(
                onClick = onNavigateToSignup,
                modifier = Modifier.testTag("go_to_signup_btn")
            ) {
                Text(
                    text = JamesI18n.t("no_account", currentLang),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun SignupScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onNavigateToLogin: () -> Unit,
    onSignupSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    var selectedCountry by remember { mutableStateOf(com.example.util.JamesCountryUtil.findByCode("HT")) }
    var showCountryDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            JamesOfficialLogo(
                size = 76.dp,
                isVertical = true,
                brandTextSize = 24.sp,
                subtitle = "Rejoignez la communauté de messagerie moderne",
                animated = true,
                withHalo = true
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Sélecteur de pays et indicatif téléphonique international
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .clickable { showCountryDialog = true }
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("signup_country_selector"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = selectedCountry.flag, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${selectedCountry.name} (${selectedCountry.dialCode})",
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = "Changer",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showCountryDialog) {
                com.example.ui.components.JamesCountryPickerDialog(
                    selectedCountry = selectedCountry,
                    onCountrySelected = { selectedCountry = it },
                    onDismiss = { showCountryDialog = false }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    errorMessage = null
                },
                label = { Text(JamesI18n.t("email_label", currentLang)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_email_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    errorMessage = null
                },
                label = { Text(JamesI18n.t("password_label", currentLang)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_password_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = {
                    confirmPassword = it
                    errorMessage = null
                },
                label = { Text("Confirmer le mot de passe") },
                leadingIcon = { Icon(imageVector = Icons.Default.Lock, contentDescription = null) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("signup_confirm_password_input"),
                shape = RoundedCornerShape(12.dp)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (password != confirmPassword) {
                        errorMessage = "Les mots de passe ne correspondent pas"
                        return@Button
                    }
                    scope.launch {
                        isLoading = true
                        errorMessage = null
                        val result = repository.signup(email, password, country = selectedCountry.name)
                        isLoading = false
                        result.fold(
                            onSuccess = { onSignupSuccess() },
                            onFailure = { err -> errorMessage = err.message ?: "Erreur d'inscription" }
                        )
                    }
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("signup_submit_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = JamesI18n.t("signup_btn", currentLang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { showGoogleDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("signup_google_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "🌐 ${JamesI18n.t("google_login", currentLang)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (showGoogleDialog) {
                GoogleAccountSelectorDialog(
                    onDismiss = { showGoogleDialog = false },
                    onAccountSelected = { gEmail, gName, gPhoto ->
                        showGoogleDialog = false
                        scope.launch {
                            isLoading = true
                            errorMessage = null
                            val res = repository.loginWithGoogle(
                                email = gEmail,
                                displayName = gName,
                                photoUrl = gPhoto
                            )
                            isLoading = false
                            res.fold(
                                onSuccess = { onSignupSuccess() },
                                onFailure = { err -> errorMessage = err.message ?: "Erreur d'inscription Google" }
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = onNavigateToLogin,
                modifier = Modifier.testTag("go_to_login_btn")
            ) {
                Text(
                    text = JamesI18n.t("has_account", currentLang),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun ChooseUsernameScreen(
    repository: JamesChatRepository,
    currentLang: AppLanguage,
    onUsernameSet: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var checkResult by remember { mutableStateOf<UsernameCheckResult?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(username) {
        val trimmed = username.trim().lowercase()
        if (trimmed.isEmpty()) {
            checkResult = null
            isChecking = false
            return@LaunchedEffect
        }
        isChecking = true
        delay(350)
        checkResult = repository.checkUsernameAvailability(trimmed)
        isChecking = false
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AlternateEmail,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = JamesI18n.t("choose_username", currentLang),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = JamesI18n.t("username_desc", currentLang),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it.lowercase().replace(" ", "_")
                    errorMessage = null
                },
                label = { Text(JamesI18n.t("username_label", currentLang)) },
                prefix = { Text("@", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("choose_username_input"),
                shape = RoundedCornerShape(12.dp),
                trailingIcon = {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else if (checkResult == UsernameCheckResult.AVAILABLE) {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Disponible", tint = JamesOnlineGreen)
                    } else if (checkResult != null) {
                        Icon(imageVector = Icons.Default.Error, contentDescription = "Indisponible", tint = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            val statusText = when {
                isChecking -> JamesI18n.t("username_checking", currentLang)
                checkResult == UsernameCheckResult.AVAILABLE -> "✅ ${JamesI18n.t("username_available", currentLang)}"
                checkResult == UsernameCheckResult.TAKEN -> "❌ ${JamesI18n.t("username_taken", currentLang)}"
                checkResult == UsernameCheckResult.RESERVED -> "⚠️ ${JamesI18n.t("username_reserved", currentLang)}"
                checkResult == UsernameCheckResult.TOO_SHORT -> "⚠️ Minimum 3 caractères"
                checkResult == UsernameCheckResult.TOO_LONG -> "⚠️ Maximum 20 caractères"
                checkResult == UsernameCheckResult.INVALID_FORMAT -> "⚠️ Lettres, chiffres et _ uniquement"
                else -> JamesI18n.t("username_rules", currentLang)
            }
            Text(
                text = statusText,
                fontSize = 12.sp,
                color = if (checkResult == UsernameCheckResult.AVAILABLE) JamesOnlineGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.Start)
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    scope.launch {
                        isSubmitting = true
                        val res = repository.setUsername(username)
                        isSubmitting = false
                        res.fold(
                            onSuccess = { onUsernameSet() },
                            onFailure = { err -> errorMessage = err.message }
                        )
                    }
                },
                enabled = checkResult == UsernameCheckResult.AVAILABLE && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_username_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text(
                        text = JamesI18n.t("save_username", currentLang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleAccountSelectorDialog(
    onDismiss: () -> Unit,
    onAccountSelected: (email: String, displayName: String, photoUrl: String) -> Unit
) {
    var showCustomInput by remember { mutableStateOf(false) }
    var customEmail by remember { mutableStateOf("") }
    var customName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🌐", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Continuer avec Google", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!showCustomInput) {
                    Text(
                        text = "Choisissez un compte pour continuer sur James Chat :",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAccountSelected(
                                    "hangington53@gmail.com",
                                    "Hangington",
                                    "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
                                )
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AvatarWithStatus(name = "Hangington", size = 40.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "Hangington", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    JamesVBadge(size = 12.dp)
                                }
                                Text(text = "hangington53@gmail.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onAccountSelected(
                                    "james.user@gmail.com",
                                    "James User",
                                    "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?w=150"
                                )
                            },
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            AvatarWithStatus(name = "James User", size = 40.dp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = "James User", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text(text = "james.user@gmail.com", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = { showCustomInput = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "➕ Utiliser un autre compte Google", fontSize = 13.sp)
                    }
                } else {
                    Text(
                        text = "Connectez-vous avec votre adresse Google :",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = customEmail,
                        onValueChange = { customEmail = it },
                        label = { Text("Adresse email Google") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("Nom complet") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            if (showCustomInput) {
                Button(
                    onClick = {
                        val trimmed = customEmail.trim()
                        if (trimmed.isNotEmpty()) {
                            val name = customName.trim().ifEmpty { trimmed.substringBefore("@") }
                            onAccountSelected(trimmed, name, "")
                        }
                    },
                    enabled = customEmail.contains("@")
                ) {
                    Text("Continuer")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
