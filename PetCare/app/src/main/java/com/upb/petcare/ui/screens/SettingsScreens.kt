package com.upb.petcare.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.ThemeMode
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.SettingsViewModel
import com.upb.petcare.ui.components.PetTopBar
import kotlinx.coroutines.launch

// ---------------------------------------------------------------- Configuración

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLoggedOut: () -> Unit,
    vm: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val user by vm.user.collectAsStateWithLifecycle()
    val theme by vm.themeMode.collectAsStateWithLifecycle()
    val alertDays by vm.alertDays.collectAsStateWithLifecycle()
    var editName by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { PetTopBar("Configuración", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            SettingsGroup("Perfil") {
                SettingItem(Icons.Default.Person, "Nombre", user?.name ?: "", onClick = { editName = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar nombre")
                }
                SettingItem(Icons.Default.Email, "Correo", user?.email ?: "")
            }

            SettingsGroup("Apariencia") {
                SettingItem(Icons.Default.Palette, "Tema", "Claro, oscuro o según el sistema")
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    ThemeMode.entries.forEachIndexed { i, mode ->
                        SegmentedButton(
                            selected = theme == mode,
                            onClick = { vm.setTheme(mode) },
                            shape = SegmentedButtonDefaults.itemShape(i, ThemeMode.entries.size),
                        ) { Text(mode.label) }
                    }
                }
            }

            SettingsGroup("Alertas de vacunas") {
                SettingItem(Icons.Default.NotificationsActive, "Anticipación",
                    "Marcar como \"Próxima\" una vacuna $alertDays días antes")
                val options = listOf(7, 15, 30, 60)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    options.forEachIndexed { i, d ->
                        SegmentedButton(
                            selected = alertDays == d,
                            onClick = { vm.setAlertDays(d) },
                            shape = SegmentedButtonDefaults.itemShape(i, options.size),
                        ) { Text("$d d") }
                    }
                }
            }

            SettingsGroup("Datos") {
                SettingItem(Icons.Default.AutoAwesome, "Cargar datos de ejemplo", "Agrega 3 mascotas con vacunas de prueba",
                    onClick = { vm.seedDemo { scope.launch { snackbar.showSnackbar("Datos de ejemplo agregados") } } })
                SettingItem(Icons.Default.DeleteForever, "Borrar mis mascotas", "Elimina todas las mascotas y vacunas",
                    onClick = { confirmDelete = true }, danger = true)
            }

            SettingsGroup("Cuenta") {
                SettingItem(Icons.AutoMirrored.Filled.Logout, "Cerrar sesión", "Volver a la pantalla de inicio",
                    onClick = { vm.logout(onLoggedOut) })
            }

            Text("PetCare v1.0.0", style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth())
        }
    }

    if (editName) {
        var value by rememberSaveable { mutableStateOf(user?.name ?: "") }
        AlertDialog(
            onDismissRequest = { editName = false },
            title = { Text("Editar nombre") },
            text = { OutlinedTextField(value = value, onValueChange = { value = it }, singleLine = true, label = { Text("Nombre") }) },
            confirmButton = {
                TextButton(enabled = value.trim().length >= 3, onClick = { vm.updateName(value); editName = false }) { Text("Guardar") }
            },
            dismissButton = { TextButton(onClick = { editName = false }) { Text("Cancelar") } },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Borrar todos los datos") },
            text = { Text("Se eliminarán todas tus mascotas y su historial de vacunas. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    vm.deleteAll { scope.launch { snackbar.showSnackbar("Datos eliminados") } }
                }) { Text("Borrar", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun SettingsGroup(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp))
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
            Column(Modifier.padding(vertical = 4.dp)) { content() }
        }
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null,
    danger: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    ListItem(
        headlineContent = { Text(title, color = color, fontWeight = FontWeight.Medium) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, null, tint = if (danger) color else MaterialTheme.colorScheme.primary) },
        trailingContent = trailing,
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        modifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
    )
}
