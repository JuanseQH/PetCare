package com.upb.petcare.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.VaccineWithPet
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.AuthViewModel
import com.upb.petcare.ui.PetViewModel
import com.upb.petcare.ui.components.PetAvatar
import com.upb.petcare.ui.components.StatusChip
import com.upb.petcare.util.VaccineStatus
import com.upb.petcare.util.formatDate
import com.upb.petcare.util.latestPerVaccine
import com.upb.petcare.util.relativeDays
import com.upb.petcare.util.vaccineStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onPets: () -> Unit,
    onAddPet: () -> Unit,
    onPet: (Long) -> Unit,
    onSettings: () -> Unit,
    onCredits: () -> Unit,
    onLoggedOut: () -> Unit,
    authVm: AuthViewModel = viewModel(factory = AppViewModelProvider.Factory),
    petVm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val user by authVm.currentUser.collectAsStateWithLifecycle()
    val pets by petVm.pets.collectAsStateWithLifecycle()
    val vaccines by petVm.vaccines.collectAsStateWithLifecycle()
    val alertDays by petVm.alertDays.collectAsStateWithLifecycle()
    var confirmLogout by rememberSaveable { mutableStateOf(false) }

    // Alertas: última aplicación de cada vacuna que está vencida o próxima
    val alerts = remember(vaccines, alertDays) {
        vaccines.latestPerVaccine()
            .filter { vaccineStatus(it.vaccine.nextDoseDate, alertDays) in setOf(VaccineStatus.OVERDUE, VaccineStatus.SOON) }
            .sortedBy { it.vaccine.nextDoseDate }
    }
    val overdue = alerts.count { vaccineStatus(it.vaccine.nextDoseDate, alertDays) == VaccineStatus.OVERDUE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Hola, ${user?.name?.substringBefore(' ') ?: ""} 👋", fontWeight = FontWeight.Bold)
                        Text("¿Cómo están tus mascotas hoy?", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { confirmLogout = true }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Resumen
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Mascotas", "${pets?.size ?: 0}", Icons.Default.Pets,
                        MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, Modifier.weight(1f))
                    StatCard("Vacunas", "${vaccines.size}", Icons.Default.Vaccines,
                        MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer, Modifier.weight(1f))
                    StatCard("Alertas", "${alerts.size}", Icons.Default.Warning,
                        if (overdue > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer,
                        if (overdue > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        Modifier.weight(1f))
                }
            }

            // Menú
            item { SectionTitle("Menú") }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MenuTile("Mis mascotas", "Listado y detalle", Icons.Default.Pets, onPets, Modifier.weight(1f))
                        MenuTile("Nueva mascota", "Formulario", Icons.Default.AddCircle, onAddPet, Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        MenuTile("Configuración", "Tema y alertas", Icons.Default.Settings, onSettings, Modifier.weight(1f))
                        MenuTile("Créditos", "Sobre la app", Icons.Default.Info, onCredits, Modifier.weight(1f))
                    }
                }
            }

            // Próximas vacunas
            item { SectionTitle("Próximas vacunas") }
            if (alerts.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                if (pets.isNullOrEmpty()) "Agrega tu primera mascota para empezar."
                                else "¡Todo al día! No hay vacunas pendientes en los próximos $alertDays días.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            } else {
                items(alerts, key = { it.vaccine.id }) { item ->
                    AlertRow(item, alertDays) { onPet(item.vaccine.petId) }
                }
            }
        }
    }

    if (confirmLogout) {
        AlertDialog(
            onDismissRequest = { confirmLogout = false },
            title = { Text("Cerrar sesión") },
            text = { Text("¿Seguro que quieres salir de tu cuenta?") },
            confirmButton = {
                TextButton(onClick = { confirmLogout = false; authVm.logout(onLoggedOut) }) { Text("Salir") }
            },
            dismissButton = { TextButton(onClick = { confirmLogout = false }) { Text("Cancelar") } },
        )
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun StatCard(label: String, value: String, icon: ImageVector, bg: Color, fg: Color, modifier: Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = bg, contentColor = fg)) {
        Column(Modifier.padding(14.dp)) {
            Icon(icon, null, Modifier.size(22.dp))
            Spacer(Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun MenuTile(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            Spacer(Modifier.height(10.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AlertRow(item: VaccineWithPet, alertDays: Int, onClick: () -> Unit) {
    val next = item.vaccine.nextDoseDate ?: return
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PetAvatar(item.petSpecies, 44.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("${item.vaccine.name} · ${item.petName}", style = MaterialTheme.typography.titleSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${formatDate(next)} (${relativeDays(next)})", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(vaccineStatus(next, alertDays))
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
