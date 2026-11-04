package com.upb.petcare.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DeleteSweep
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.ThemeMode
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.SettingsViewModel
import com.upb.petcare.ui.components.BackTopBar
import com.upb.petcare.ui.components.BrandHeader
import com.upb.petcare.ui.components.CircleIconButton
import com.upb.petcare.ui.components.IconBadge
import com.upb.petcare.ui.components.PetTextField
import com.upb.petcare.ui.components.PillTabs
import com.upb.petcare.ui.components.SoftCard
import com.upb.petcare.ui.components.StatusBarForLightScreen
import com.upb.petcare.ui.components.StatusBarIcons
import com.upb.petcare.ui.navigation.BottomBarSpace
import com.upb.petcare.ui.theme.PetPalette
import kotlinx.coroutines.launch

// ================================================================= Ajustes

@Composable
fun SettingsScreen(
    onCredits: () -> Unit,
    onLoggedOut: () -> Unit,
    vm: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    StatusBarForLightScreen()
    val context = LocalContext.current
    val user by vm.user.collectAsStateWithLifecycle()
    val theme by vm.themeMode.collectAsStateWithLifecycle()
    val alertDays by vm.alertDays.collectAsStateWithLifecycle()
    val reminders by vm.reminders.collectAsStateWithLifecycle()
    var editName by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Android 13+ pide permiso para mostrar notificaciones
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.setReminders(true)
        else scope.launch { snackbar.showSnackbar("Sin permiso no se pueden mostrar recordatorios") }
    }
    fun toggleReminders(on: Boolean) {
        if (!on) { vm.setReminders(false); return }
        val needsPermission = Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        if (needsPermission) permission.launch(Manifest.permission.POST_NOTIFICATIONS) else vm.setReminders(true)
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = BottomBarSpace),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("Ajustes", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.statusBarsPadding().padding(top = 16.dp))

            // ---------- Perfil
            SoftCard(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.primary, radius = 28.dp) {
                Box {
                    com.upb.petcare.ui.components.PawPattern(Modifier.matchParentSize())
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(58.dp).background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape), contentAlignment = Alignment.Center) {
                            Text(
                                user?.name.orEmpty().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() },
                                style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(user?.name ?: "", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimary)
                            Text(user?.email ?: "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                        }
                        CircleIconButton(Icons.Rounded.Edit, "Editar nombre", { editName = true },
                            Color.White.copy(alpha = 0.2f), MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }

            // ---------- Apariencia
            Group("Apariencia") {
                ItemRow(Icons.Rounded.DarkMode, "Tema", "Claro, oscuro o el del sistema", PetPalette[3])
                PillTabs(ThemeMode.entries.map { it.label }, theme.ordinal, { vm.setTheme(ThemeMode.entries[it]) },
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp))
            }

            // ---------- Recordatorios
            Group("Recordatorios") {
                ItemRow(Icons.Rounded.NotificationsActive, "Notificaciones diarias",
                    "Te avisamos de vacunas vencidas o próximas", MaterialTheme.colorScheme.secondary) {
                    Switch(checked = reminders, onCheckedChange = { toggleReminders(it) })
                }
                if (reminders) {
                    ItemRow(Icons.Rounded.Send, "Probar notificación", "Envía ahora un recordatorio de prueba",
                        MaterialTheme.colorScheme.secondary, onClick = {
                            vm.testReminder()
                            scope.launch { snackbar.showSnackbar("Recordatorio enviado 🔔") }
                        })
                }
                ItemRow(Icons.Rounded.Timer, "Anticipación", "Marcar \"Próxima\" $alertDays días antes", MaterialTheme.colorScheme.tertiary)
                val options = listOf(7, 15, 30, 60)
                PillTabs(options.map { "$it días" }, options.indexOf(alertDays).coerceAtLeast(0), { vm.setAlertDays(options[it]) },
                    Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp))
            }

            // ---------- Datos
            Group("Datos") {
                ItemRow(Icons.Rounded.AutoAwesome, "Cargar datos de ejemplo", "Agrega 3 mascotas con vacunas y pesos",
                    MaterialTheme.colorScheme.primary, onClick = {
                        vm.seedDemo { scope.launch { snackbar.showSnackbar("Datos de ejemplo agregados 🐾") } }
                    })
                ItemRow(Icons.Rounded.DeleteSweep, "Borrar mis mascotas", "Elimina mascotas, vacunas, pesos y fotos",
                    MaterialTheme.colorScheme.error, onClick = { confirmDelete = true })
            }

            // ---------- Acerca de
            Group("Acerca de") {
                ItemRow(Icons.Rounded.School, "Créditos", "Información del estudiante y del proyecto",
                    PetPalette[4], onClick = onCredits)
                ItemRow(Icons.Rounded.Info, "Versión", "PetCare 2.0.0", PetPalette[6])
            }

            OutlinedButton(
                onClick = { confirmLogout = true }, shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Icon(Icons.AutoMirrored.Rounded.Logout, null, tint = MaterialTheme.colorScheme.error)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
            }
        }
        SnackbarHost(snackbar, Modifier.align(Alignment.BottomCenter).padding(bottom = BottomBarSpace))
        com.upb.petcare.ui.components.StatusBarScrim(modifier = Modifier.align(Alignment.TopCenter))
    }

    if (editName) {
        var value by rememberSaveable { mutableStateOf(user?.name ?: "") }
        AlertDialog(
            onDismissRequest = { editName = false },
            title = { Text("Editar nombre") },
            text = { PetTextField(value, { value = it }, "Nombre") },
            confirmButton = {
                TextButton(enabled = value.trim().length >= 3, onClick = { vm.updateName(value); editName = false }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editName = false }) { Text("Cancelar") } },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Text("🗑️", fontSize = 30.sp) },
            title = { Text("¿Borrar todos los datos?") },
            text = { Text("Se eliminarán todas tus mascotas con su historial. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteAll { scope.launch { snackbar.showSnackbar("Datos eliminados") } }
                }) { Text("Borrar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            icon = { Text("👋", fontSize = 30.sp) },
            title = { Text("¿Cerrar sesión?") },
            text = { Text("Tus datos quedan guardados en el teléfono para la próxima vez.") },
            confirmButton = { TextButton(onClick = { confirmLogout = false; vm.logout(onLoggedOut) }) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun Group(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(title.uppercase(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp))
        SoftCard(Modifier.fillMaxWidth()) { Column(Modifier.padding(vertical = 4.dp), content = content) }
    }
}

@Composable
private fun ItemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accent: Color,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconBadge(icon, accent.copy(alpha = 0.15f), accent, size = 42.dp)
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall,
                color = if (accent == MaterialTheme.colorScheme.error) accent else MaterialTheme.colorScheme.onSurface)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        when {
            trailing != null -> trailing()
            onClick != null -> Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ================================================================= Créditos

/**
 * >>> DATOS DEL ESTUDIANTE <<<
 * Esta información aparece en la pantalla de Créditos.
 */
object StudentInfo {
    const val NAME = "Juan Sebastián Quintero Hernández"
    const val STUDENT_ID = "ID: 000492758"
    const val EMAIL = "setiquintero@gmail.com"
    const val PROGRAM = "Ingeniería de Sistemas e Informática"
    const val COURSE = "Aplicaciones Móviles"
    const val UNIVERSITY = "Universidad Pontificia Bolivariana"
    const val DELIVERY = "Entrega 3 · App nativa (Kotlin)"
    const val YEAR = "2026"
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreditsScreen(onBack: () -> Unit) {
    StatusBarIcons(darkIcons = false)
    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).verticalScroll(rememberScrollState())) {
        Box {
            BrandHeader(Modifier.fillMaxWidth().height(290.dp), bottomRadius = 44.dp) {
                Column(Modifier.fillMaxSize().statusBarsPadding().padding(bottom = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Box(Modifier.size(84.dp).background(Color.White, RoundedCornerShape(28.dp)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Pets, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(46.dp))
                    }
                    Spacer(Modifier.height(12.dp))
                    Text("PetCare", style = MaterialTheme.typography.headlineLarge, color = Color.White)
                    Text("Versión 2.0.0", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
                }
            }
            Box(Modifier.statusBarsPadding().padding(12.dp)) {
                CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack,
                    Color.White.copy(alpha = 0.2f), Color.White)
            }
        }
        Column(Modifier.padding(20.dp).navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Aplicación nativa para llevar el carné de vacunación de tus mascotas, con alertas, recordatorios, " +
                    "control de peso y exportación a PDF.",
                style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth(),
            )
            SoftCard(Modifier.fillMaxWidth(), radius = 28.dp) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconBadge(Icons.Rounded.School, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Desarrollado por", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(StudentInfo.NAME, style = MaterialTheme.typography.headlineSmall)
                    Text(StudentInfo.STUDENT_ID, style = MaterialTheme.typography.bodyLarge)
                    Text(StudentInfo.EMAIL, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(StudentInfo.PROGRAM, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Text("${StudentInfo.COURSE} · ${StudentInfo.UNIVERSITY}", style = MaterialTheme.typography.titleSmall)
                    Text("${StudentInfo.DELIVERY} · ${StudentInfo.YEAR}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            SoftCard(Modifier.fillMaxWidth(), radius = 28.dp) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tecnologías", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Kotlin", "Jetpack Compose", "Material 3", "Room", "DataStore", "Navigation",
                            "WorkManager", "Coil", "PdfDocument", "SplashScreen").forEachIndexed { i, t ->
                            val c = PetPalette[i % PetPalette.size]
                            Text(t, style = MaterialTheme.typography.labelLarge, color = c,
                                modifier = Modifier.clip(RoundedCornerShape(50)).background(c.copy(alpha = 0.14f))
                                    .padding(horizontal = 12.dp, vertical = 7.dp))
                        }
                    }
                }
            }
            Text("Tipografías Fredoka y Nunito (SIL Open Font License) · Íconos Material Symbols",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        }
    }
}

