package com.upb.petcare.ui.screens

import android.util.Patterns
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.AuthResult
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.AuthViewModel
import com.upb.petcare.ui.components.BrandHeader
import com.upb.petcare.ui.components.PetTextField
import com.upb.petcare.ui.components.PrimaryButton
import com.upb.petcare.ui.components.SoftCard
import com.upb.petcare.ui.components.StatusBarForLightScreen
import com.upb.petcare.ui.components.StatusBarIcons
import kotlinx.coroutines.launch

private fun isValidEmail(email: String) = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

// ================================================================= Onboarding

private data class OnboardingPage(val emojis: List<String>, val title: String, val text: String, val bg: @Composable () -> Color)

@Composable
fun OnboardingScreen(onFinish: () -> Unit, vm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    StatusBarForLightScreen()
    val pages = listOf(
        OnboardingPage(listOf("🐶", "🐱", "🐰"), "Todas tus mascotas en un lugar",
            "Registra a cada integrante peludo de la familia con su foto, raza, peso y cuidados.") { MaterialTheme.colorScheme.primaryContainer },
        OnboardingPage(listOf("💉", "📋", "🩺"), "Su carné de vacunas, siempre contigo",
            "Lleva el historial de vacunas y desparasitaciones, y compártelo en PDF con tu veterinario.") { MaterialTheme.colorScheme.secondaryContainer },
        OnboardingPage(listOf("🔔", "📅", "⏰"), "Nunca olvides un refuerzo",
            "PetCare te avisa con anticipación cuando una vacuna está por vencer.") { MaterialTheme.colorScheme.tertiaryContainer },
    )
    val pager = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val finish = { vm.completeOnboarding(onFinish) }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).statusBarsPadding().navigationBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Pets, null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text("PetCare", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
            if (pager.currentPage < pages.lastIndex) TextButton(onClick = { finish() }) { Text("Saltar") }
        }
        HorizontalPager(state = pager, modifier = Modifier.weight(1f)) { i ->
            val p = pages[i]
            Column(
                Modifier.fillMaxSize().padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Ilustración: burbuja de color con emojis superpuestos
                Box(Modifier.size(280.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(250.dp).clip(RoundedCornerShape(topStart = 120.dp, topEnd = 90.dp, bottomEnd = 130.dp, bottomStart = 80.dp)).background(p.bg()))
                    Text(p.emojis[0], fontSize = 110.sp)
                    Text(p.emojis[1], fontSize = 54.sp, modifier = Modifier.align(Alignment.TopEnd).offset((-12).dp, 24.dp))
                    Text(p.emojis[2], fontSize = 46.sp, modifier = Modifier.align(Alignment.BottomStart).offset(16.dp, (-20).dp))
                }
                Spacer(Modifier.height(36.dp))
                Text(p.title, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                Spacer(Modifier.height(12.dp))
                Text(p.text, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        // Indicador de página
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.Center) {
            repeat(pages.size) { i ->
                val on = pager.currentPage == i
                val w by animateDpAsState(if (on) 28.dp else 8.dp, label = "dot")
                val c by animateColorAsState(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant, label = "dotc")
                Box(Modifier.padding(horizontal = 4.dp).height(8.dp).width(w).clip(CircleShape).background(c))
            }
        }
        PrimaryButton(
            text = if (pager.currentPage == pages.lastIndex) "Comenzar" else "Siguiente",
            icon = Icons.AutoMirrored.Rounded.ArrowForward,
            onClick = {
                if (pager.currentPage == pages.lastIndex) finish()
                else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

// ================================================================= Encabezado común de autenticación

@Composable
private fun AuthHeader(title: String, subtitle: String, compact: Boolean = false) {
    BrandHeader(Modifier.fillMaxWidth().height(if (compact) 230.dp else 300.dp), bottomRadius = 48.dp) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier.size(if (compact) 64.dp else 84.dp).background(Color.White, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Rounded.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (compact) 34.dp else 46.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(title, style = MaterialTheme.typography.headlineLarge, color = Color.White)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.85f))
        }
    }
}

// ================================================================= Login

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit,
    vm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    StatusBarIcons(darkIcons = false)
    val lastEmail by vm.lastEmail.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(lastEmail) { if (email.isEmpty()) email = lastEmail }

    val emailError = submitted && !isValidEmail(email)
    val passwordError = submitted && password.isEmpty()

    fun submit() {
        submitted = true
        if (!isValidEmail(email) || password.isEmpty()) return
        loading = true
        vm.login(email, password) { result ->
            loading = false
            when (result) {
                is AuthResult.Success -> onLoggedIn()
                is AuthResult.Error -> scope.launch { snackbar.showSnackbar(result.message) }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0)) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding()) {
            AuthHeader("PetCare", "El carné de vacunas de tus mascotas")
            SoftCard(Modifier.padding(horizontal = 20.dp).offset(y = (-48).dp), radius = 32.dp, elevation = 8.dp) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("¡Hola de nuevo! 👋", style = MaterialTheme.typography.headlineSmall)
                    Text("Inicia sesión para ver a tus mascotas.", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    PetTextField(
                        value = email, onValueChange = { email = it }, label = "Correo electrónico",
                        leadingIcon = Icons.Rounded.Email, isError = emailError,
                        supportingText = if (emailError) "Ingresa un correo válido" else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    )
                    PetTextField(
                        value = password, onValueChange = { password = it }, label = "Contraseña",
                        leadingIcon = Icons.Rounded.Lock,
                        trailing = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    if (showPassword) "Ocultar contraseña" else "Mostrar contraseña")
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError, supportingText = if (passwordError) "Ingresa tu contraseña" else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                    )
                    PrimaryButton("Ingresar", { submit() }, Modifier.fillMaxWidth(), loading = loading,
                        icon = Icons.AutoMirrored.Rounded.ArrowForward)
                }
            }
            Row(Modifier.fillMaxWidth().offset(y = (-32).dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text("¿No tienes cuenta?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onRegister) { Text("Crear una", fontWeight = FontWeight.ExtraBold) }
            }
        }
    }
}

// ================================================================= Registro

@Composable
fun RegisterScreen(
    onRegistered: () -> Unit,
    onBack: () -> Unit,
    vm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    StatusBarIcons(darkIcons = false)
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val nameError = submitted && name.trim().length < 3
    val emailError = submitted && !isValidEmail(email)
    val passwordError = submitted && password.length < 6
    val confirmError = submitted && confirm != password

    // Indicador de seguridad de la contraseña
    val strength = listOf(password.length >= 6, password.length >= 10, password.any { it.isDigit() } && password.any { it.isLetter() }).count { it }

    fun submit() {
        submitted = true
        if (name.trim().length < 3 || !isValidEmail(email) || password.length < 6 || confirm != password) return
        loading = true
        vm.register(name, email, password) { result ->
            loading = false
            when (result) {
                is AuthResult.Success -> onRegistered()
                is AuthResult.Error -> scope.launch { snackbar.showSnackbar(result.message) }
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbar) }, contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0)) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).imePadding()) {
            Box {
                AuthHeader("Crear cuenta", "Únete a la familia PetCare", compact = true)
                IconButton(onClick = onBack, modifier = Modifier.statusBarsPadding().padding(8.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", tint = Color.White, modifier = Modifier.size(26.dp))
                }
            }
            SoftCard(Modifier.padding(horizontal = 20.dp).offset(y = (-48).dp), radius = 32.dp, elevation = 8.dp) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    PetTextField(
                        value = name, onValueChange = { name = it }, label = "Nombre completo", leadingIcon = Icons.Rounded.Person,
                        isError = nameError, supportingText = if (nameError) "Mínimo 3 caracteres" else null,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
                    )
                    PetTextField(
                        value = email, onValueChange = { email = it }, label = "Correo electrónico", leadingIcon = Icons.Rounded.Email,
                        isError = emailError, supportingText = if (emailError) "Ingresa un correo válido" else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    )
                    PetTextField(
                        value = password, onValueChange = { password = it }, label = "Contraseña", leadingIcon = Icons.Rounded.Lock,
                        trailing = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, "Mostrar u ocultar")
                            }
                        },
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = passwordError, supportingText = if (passwordError) "Debe tener al menos 6 caracteres" else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    )
                    if (password.isNotEmpty()) {
                        val labels = listOf("Débil", "Aceptable", "Buena", "Fuerte")
                        val colors = listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.tertiary,
                            MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            repeat(3) { i ->
                                Box(Modifier.weight(1f).padding(end = 6.dp).height(6.dp).clip(CircleShape)
                                    .background(if (i < strength) colors[strength] else MaterialTheme.colorScheme.surfaceContainerHigh))
                            }
                            Text(labels[strength], style = MaterialTheme.typography.labelMedium, color = colors[strength],
                                modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                    PetTextField(
                        value = confirm, onValueChange = { confirm = it }, label = "Confirmar contraseña", leadingIcon = Icons.Rounded.Lock,
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        isError = confirmError, supportingText = if (confirmError) "Las contraseñas no coinciden" else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submit() }),
                    )
                    Spacer(Modifier.height(4.dp))
                    PrimaryButton("Crear cuenta", { submit() }, Modifier.fillMaxWidth(), loading = loading)
                }
            }
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally).offset(y = (-32).dp)) {
                Text("Ya tengo cuenta, iniciar sesión")
            }
        }
    }
}

