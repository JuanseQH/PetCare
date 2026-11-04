package com.upb.petcare.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.Pet
import com.upb.petcare.data.Vaccine
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.PetViewModel
import com.upb.petcare.ui.components.EmptyState
import com.upb.petcare.ui.components.PetAvatar
import com.upb.petcare.ui.components.PetTopBar
import com.upb.petcare.ui.components.StatusChip
import com.upb.petcare.util.SPECIES
import com.upb.petcare.util.VaccineStatus
import com.upb.petcare.util.ageText
import com.upb.petcare.util.formatDate
import com.upb.petcare.util.latestPerVaccine
import com.upb.petcare.util.relativeDays
import com.upb.petcare.util.vaccineStatus

// ---------------------------------------------------------------- Listado

@Composable
fun PetListScreen(
    onBack: () -> Unit,
    onPet: (Long) -> Unit,
    onAdd: () -> Unit,
    vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val pets by vm.pets.collectAsStateWithLifecycle()
    val vaccines by vm.vaccines.collectAsStateWithLifecycle()
    val alertDays by vm.alertDays.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf<String?>(null) }

    // Número de vacunas pendientes (vencidas o próximas) por mascota
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

    Scaffold(
        topBar = { PetTopBar("Mis mascotas", onBack = onBack) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAdd, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Agregar") })
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Buscar por nombre o raza") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Row(
                Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(selected = species == null, onClick = { species = null }, label = { Text("Todas") })
                SPECIES.forEach { s ->
                    FilterChip(selected = species == s, onClick = { species = if (species == s) null else s }, label = { Text(s) })
                }
            }

            when {
                pets == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                pets!!.isEmpty() -> EmptyState(Icons.Default.Pets, "Aún no tienes mascotas",
                    "Toca \"Agregar\" para registrar a tu primera mascota.")
                filtered.isEmpty() -> EmptyState(Icons.Default.SearchOff, "Sin resultados",
                    "No hay mascotas que coincidan con la búsqueda.")
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(filtered, key = { it.id }) { pet ->
                        PetRow(pet, pendingByPet[pet.id] ?: 0) { onPet(pet.id) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PetRow(pet: Pet, pending: Int, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            PetAvatar(pet.species)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(pet.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    listOf(pet.species, pet.breed).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Text(ageText(pet.birthDate), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (pending > 0) {
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, shape = RoundedCornerShape(50)) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Vaccines, null, Modifier.padding(end = 4.dp).height(16.dp))
                        Text("$pending", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
