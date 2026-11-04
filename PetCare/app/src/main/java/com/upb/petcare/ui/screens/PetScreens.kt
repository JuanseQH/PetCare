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
