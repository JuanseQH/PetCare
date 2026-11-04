package com.upb.petcare.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MonitorWeight
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.Pet
import com.upb.petcare.data.Vaccine
import com.upb.petcare.data.WeightEntry
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.PetViewModel
import com.upb.petcare.ui.components.CircleIconButton
import com.upb.petcare.ui.components.DateField
import com.upb.petcare.ui.components.EmptyState
import com.upb.petcare.ui.components.InfoPill
import com.upb.petcare.ui.components.PawPattern
import com.upb.petcare.ui.components.PetAvatar
import com.upb.petcare.ui.components.PetTextField
import com.upb.petcare.ui.components.PillTabs
import com.upb.petcare.ui.components.PrimaryButton
import com.upb.petcare.ui.components.ProgressRing
import com.upb.petcare.ui.components.SoftCard
import com.upb.petcare.ui.components.StatusBarForLightScreen
import com.upb.petcare.ui.components.StatusBarIcons
import com.upb.petcare.ui.components.StatusBarScrim
import com.upb.petcare.ui.theme.LocalAppExtras
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import com.upb.petcare.ui.components.StatusChip
import com.upb.petcare.ui.components.TonalButton
import com.upb.petcare.ui.components.WeightChart
import com.upb.petcare.ui.components.statusColors
import com.upb.petcare.ui.components.weightDelta
import com.upb.petcare.ui.navigation.BottomBarSpace
import com.upb.petcare.ui.theme.petColor
import com.upb.petcare.util.SPECIES
import com.upb.petcare.util.VaccineStatus
import com.upb.petcare.util.ageText
import com.upb.petcare.util.formatDate
import com.upb.petcare.util.formatKg
import com.upb.petcare.util.healthScore
import com.upb.petcare.util.latestIds
import com.upb.petcare.util.latestPerVaccine
import com.upb.petcare.util.relativeDays
import com.upb.petcare.util.shortAge
import com.upb.petcare.util.speciesEmoji
import com.upb.petcare.util.today
import com.upb.petcare.util.vaccineStatus
import kotlinx.coroutines.launch

// ================================================================= Listado (cuadrícula)

@Composable
fun PetListScreen(
    onPet: (Long) -> Unit,
    onAdd: () -> Unit,
    vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    StatusBarForLightScreen()
    val pets by vm.pets.collectAsStateWithLifecycle()
    val vaccines by vm.vaccines.collectAsStateWithLifecycle()
    val alertDays by vm.alertDays.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf<String?>(null) }

    val pendingByPet = remember(vaccines, alertDays) {
        vaccines.latestPerVaccine()
            .filter { vaccineStatus(it.vaccine.nextDoseDate, alertDays) in setOf(VaccineStatus.OVERDUE, VaccineStatus.SOON) }
            .groupingBy { it.vaccine.petId }.eachCount()
    }
    val filtered = remember(pets, query, species) {
        pets.orEmpty().filter { p ->
            (species == null || p.species == species) &&
                (query.isBlank() || p.name.contains(query, true) || p.breed.contains(query, true))
        }
    }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = BottomBarSpace + 72.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            item(span = { GridItemSpan(2) }) {
                Column(Modifier.statusBarsPadding().padding(top = 16.dp)) {
                    Text("Mis mascotas", style = MaterialTheme.typography.headlineLarge)
                    Text(
                        "${pets?.size ?: 0} ${if (pets?.size == 1) "integrante" else "integrantes"} de la familia",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    PetTextField(
                        value = query, onValueChange = { query = it }, label = "Buscar por nombre o raza",
                        leadingIcon = Icons.Rounded.Search,
                        trailing = if (query.isNotEmpty()) { { IconButton(onClick = { query = "" }) { Icon(Icons.Rounded.Close, "Limpiar") } } } else null,
                    )
                    Row(Modifier.horizontalScroll(rememberScrollState()).padding(vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SpeciesChip("Todas", "✨", species == null) { species = null }
                        SPECIES.forEach { s -> SpeciesChip(s, speciesEmoji(s), species == s) { species = if (species == s) null else s } }
                    }
                }
            }
            when {
                pets == null -> item(span = { GridItemSpan(2) }) {
                    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                }
                pets!!.isEmpty() -> item(span = { GridItemSpan(2) }) {
                    EmptyState("🐾", "Aún no tienes mascotas", "Toca \"Nueva mascota\" para registrar a la primera.")
                }
                filtered.isEmpty() -> item(span = { GridItemSpan(2) }) {
                    EmptyState("🔍", "Sin resultados", "No hay mascotas que coincidan con la búsqueda.")
                }
                else -> items(filtered, key = { it.id }) { pet ->
                    PetCard(pet, pendingByPet[pet.id] ?: 0, Modifier.animateItem()) { onPet(pet.id) }
                }
            }
        }
        ExtendedFloatingActionButton(
            onClick = onAdd,
            icon = { Icon(Icons.Rounded.Add, null) },
            text = { Text("Nueva mascota", style = MaterialTheme.typography.labelLarge) },
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 20.dp, bottom = 96.dp),
        )
        StatusBarScrim(modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun SpeciesChip(label: String, emoji: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected, onClick = onClick,
        label = { Text("$emoji  $label", style = MaterialTheme.typography.labelLarge) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant, selectedBorderColor = Color.Transparent),
    )
}

@Composable
private fun PetCard(pet: Pet, pending: Int, modifier: Modifier, onClick: () -> Unit) {
    val color = petColor(pet.colorIndex)
    SoftCard(modifier.fillMaxWidth(), onClick = onClick, radius = 28.dp) {
        Box(
            Modifier.fillMaxWidth().height(118.dp).background(Brush.verticalGradient(listOf(color.copy(alpha = 0.32f), color.copy(alpha = 0.12f)))),
            contentAlignment = Alignment.Center,
        ) {
            PawPattern(Modifier.matchParentSize(), color.copy(alpha = 0.16f))
            PetAvatar(pet.photoPath, pet.species, pet.colorIndex, size = 88.dp, ringColor = Color.White)
            if (pending > 0) {
                Row(
                    Modifier.align(Alignment.TopEnd).padding(10.dp).clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.secondary).padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Rounded.Vaccines, null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("$pending", style = MaterialTheme.typography.labelMedium, color = Color.White)
                }
            }
        }
        Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
            Text(pet.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(pet.breed.ifBlank { pet.species }, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                InfoPill(shortAge(pet.birthDate), MaterialTheme.colorScheme.surfaceContainer, MaterialTheme.colorScheme.onSurface)
                if (pet.sex.isNotBlank()) InfoPill(if (pet.sex == "Macho") "♂ Macho" else "♀ Hembra", color.copy(alpha = 0.18f),
                    androidx.compose.ui.graphics.lerp(color, Color.Black, 0.35f))
            }
        }
    }
}

// ---------------------------------------------------------------- Detalle

@Composable
fun PetDetailScreen(
    petId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddVaccine: () -> Unit,
    onEditVaccine: (Long) -> Unit,
    vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val pet by remember(petId) { vm.pet(petId) }.collectAsStateWithLifecycle(null)
    val vaccines by remember(petId) { vm.vaccinesForPet(petId) }.collectAsStateWithLifecycle(emptyList())
    val alertDays by vm.alertDays.collectAsStateWithLifecycle()
    var confirmDeletePet by rememberSaveable { mutableStateOf(false) }
    var vaccineToDelete by remember { mutableStateOf<Vaccine?>(null) }

    Scaffold(
        topBar = {
            PetTopBar(pet?.name ?: "Detalle", onBack = onBack) {
                IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Editar mascota") }
                IconButton(onClick = { confirmDeletePet = true }) { Icon(Icons.Default.Delete, contentDescription = "Eliminar mascota") }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddVaccine, icon = { Icon(Icons.Default.Vaccines, null) },
                text = { Text("Registrar vacuna") })
        },
    ) { padding ->
        val p = pet
        if (p == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item { PetHeader(p) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SectionTitle("Carné de vacunación")
                    Spacer(Modifier.weight(1f))
                    Text("${vaccines.size} ${if (vaccines.size == 1) "registro" else "registros"}", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (vaccines.isEmpty()) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                        Text(
                            "Todavía no hay vacunas registradas para ${p.name}. Toca \"Registrar vacuna\" para agregar la primera.",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else {
                // Solo la aplicación más reciente de cada vacuna genera alerta
                val latestIds = vaccines.groupBy { it.name.trim().lowercase() }
                    .map { (_, l) -> l.maxBy { it.appliedDate }.id }.toSet()
                items(vaccines, key = { it.id }) { v ->
                    VaccineRow(
                        vaccine = v,
                        status = if (v.id in latestIds) vaccineStatus(v.nextDoseDate, alertDays) else VaccineStatus.DONE,
                        superseded = v.id !in latestIds,
                        onClick = { onEditVaccine(v.id) },
                        onDelete = { vaccineToDelete = v },
                    )
                }
            }
        }
    }

    if (confirmDeletePet && pet != null) {
        AlertDialog(
            onDismissRequest = { confirmDeletePet = false },
            title = { Text("Eliminar a ${pet!!.name}") },
            text = { Text("Se borrará la mascota y todo su historial de vacunas. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { confirmDeletePet = false; vm.deletePet(pet!!, onBack) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDeletePet = false }) { Text("Cancelar") } },
        )
    }
    vaccineToDelete?.let { v ->
        AlertDialog(
            onDismissRequest = { vaccineToDelete = null },
            title = { Text("Eliminar registro") },
            text = { Text("¿Eliminar la vacuna \"${v.name}\" del ${formatDate(v.appliedDate)}?") },
            confirmButton = {
                TextButton(onClick = { vm.deleteVaccine(v); vaccineToDelete = null }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { vaccineToDelete = null }) { Text("Cancelar") } },
        )
    }
}

@Composable
private fun PetHeader(pet: Pet) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PetAvatar(pet.species, 72.dp)
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(pet.name, style = MaterialTheme.typography.headlineSmall)
                    Text(listOf(pet.species, pet.breed).filter { it.isNotBlank() }.joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium)
                }
            }
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f))
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth()) {
                InfoItem("Edad", ageText(pet.birthDate), Modifier.weight(1f))
                InfoItem("Sexo", pet.sex.ifBlank { "—" }, Modifier.weight(1f))
                InfoItem("Peso", pet.weightKg?.let { "${"%.1f".format(it).removeSuffix(",0").removeSuffix(".0")} kg" } ?: "—", Modifier.weight(1f))
            }
            if (pet.birthDate != null) {
                Spacer(Modifier.height(8.dp))
                InfoItem("Nacimiento", formatDate(pet.birthDate))
            }
            if (pet.notes.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                InfoItem("Notas", pet.notes)
            }
        }
    }
}

@Composable
private fun InfoItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun VaccineRow(
    vaccine: Vaccine,
    status: VaccineStatus,
    superseded: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(vaccine.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(8.dp))
                    if (!superseded) StatusChip(status)
                }
                Text("Aplicada: ${formatDate(vaccine.appliedDate)}", style = MaterialTheme.typography.bodyMedium)
                vaccine.nextDoseDate?.let {
                    Text(
                        if (superseded) "Refuerzo: ${formatDate(it)} (ya renovada)"
                        else "Refuerzo: ${formatDate(it)} · ${relativeDays(it)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (vaccine.veterinarian.isNotBlank()) {
                    Text("Veterinario: ${vaccine.veterinarian}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Eliminar vacuna") }
        }
    }
}
