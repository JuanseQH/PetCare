package com.upb.petcare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.Pet
import com.upb.petcare.data.Vaccine
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.PetViewModel
import com.upb.petcare.ui.components.DateField
import com.upb.petcare.ui.components.PetTopBar
import com.upb.petcare.util.SPECIES
import com.upb.petcare.util.VACCINE_SUGGESTIONS
import com.upb.petcare.util.speciesEmoji
import com.upb.petcare.util.today

// ---------------------------------------------------------------- Formulario mascota

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun PetFormScreen(
    petId: Long,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val isEdit = petId != 0L
    var loaded by rememberSaveable { mutableStateOf(!isEdit) }
    var name by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf("Perro") }
    var breed by rememberSaveable { mutableStateOf("") }
    var sex by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var weight by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var saving by rememberSaveable { mutableStateOf(false) }

    // En modo edición, cargar los datos actuales una sola vez
    LaunchedEffect(petId) {
        if (isEdit && !loaded) {
            vm.getPet(petId)?.let { p ->
                name = p.name; species = p.species; breed = p.breed; sex = p.sex
                birthDate = p.birthDate; notes = p.notes
                weight = p.weightKg?.toString()?.removeSuffix(".0") ?: ""
            }
            loaded = true
        }
    }

    val weightValue = weight.replace(',', '.').toDoubleOrNull()
    val nameError = submitted && name.isBlank()
    val weightError = weight.isNotBlank() && (weightValue == null || weightValue <= 0 || weightValue > 200)
    val birthError = birthDate != null && birthDate!! > today()

    fun save() {
        submitted = true
        if (name.isBlank() || weightError || birthError) return
        saving = true
        vm.savePet(
            Pet(
                id = petId, ownerId = 0, name = name.trim(), species = species, breed = breed.trim(),
                sex = sex, birthDate = birthDate, weightKg = weightValue, notes = notes.trim(),
            )
        ) { onSaved(it) }
    }

    Scaffold(topBar = { PetTopBar(if (isEdit) "Editar mascota" else "Nueva mascota", onBack = onBack) }) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Nombre *") },
                leadingIcon = { Icon(Icons.Default.Pets, null) },
                isError = nameError,
                supportingText = if (nameError) { { Text("El nombre es obligatorio") } } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Especie", style = MaterialTheme.typography.labelLarge)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SPECIES.forEach { s ->
                    FilterChip(
                        selected = species == s,
                        onClick = { species = s },
                        label = { Text("${speciesEmoji(s)}  $s") },
                    )
                }
            }

            OutlinedTextField(
                value = breed, onValueChange = { breed = it },
                label = { Text("Raza") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Sexo", style = MaterialTheme.typography.labelLarge)
            val sexes = listOf("Macho", "Hembra")
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                sexes.forEachIndexed { i, s ->
                    SegmentedButton(
                        selected = sex == s,
                        onClick = { sex = if (sex == s) "" else s },
                        shape = SegmentedButtonDefaults.itemShape(i, sexes.size),
                    ) { Text(s) }
                }
            }

            DateField(
                label = "Fecha de nacimiento",
                value = birthDate,
                onValueChange = { birthDate = it },
                clearable = true,
                isError = birthError,
                supportingText = if (birthError) "La fecha no puede ser futura" else null,
            )

            OutlinedTextField(
                value = weight, onValueChange = { weight = it },
                label = { Text("Peso (kg)") },
                isError = weightError,
                supportingText = if (weightError) { { Text("Ingresa un peso válido") } } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Notas (alergias, cuidados...)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(onClick = { save() }, enabled = !saving, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(if (isEdit) "Guardar cambios" else "Guardar mascota")
            }
        }
    }
}

// ---------------------------------------------------------------- Formulario vacuna

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaccineFormScreen(
    petId: Long,
    vaccineId: Long,
    onBack: () -> Unit,
    vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val isEdit = vaccineId != 0L
    val pet by remember(petId) { vm.pet(petId) }.collectAsStateWithLifecycle(null)
    var loaded by rememberSaveable { mutableStateOf(!isEdit) }
    var name by rememberSaveable { mutableStateOf("") }
    var applied by rememberSaveable { mutableStateOf<Long?>(today()) }
    var next by rememberSaveable { mutableStateOf<Long?>(null) }
    var vet by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var saving by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(vaccineId) {
        if (isEdit && !loaded) {
            vm.getVaccine(vaccineId)?.let { v ->
                name = v.name; applied = v.appliedDate; next = v.nextDoseDate; vet = v.veterinarian; notes = v.notes
            }
            loaded = true
        }
    }

    val nameError = submitted && name.isBlank()
    val appliedError = submitted && applied == null || (applied != null && applied!! > today())
    val nextError = next != null && applied != null && next!! <= applied!!

    fun save() {
        submitted = true
        if (name.isBlank() || applied == null || applied!! > today() || nextError) return
        saving = true
        vm.saveVaccine(
            Vaccine(
                id = vaccineId, petId = petId, name = name.trim(), appliedDate = applied!!,
                nextDoseDate = next, veterinarian = vet.trim(), notes = notes.trim(),
            ),
            onBack,
        )
    }

    Scaffold(
        topBar = {
            PetTopBar(
                (if (isEdit) "Editar vacuna" else "Registrar vacuna") + (pet?.let { " · ${it.name}" } ?: ""),
                onBack = onBack,
            )
        }
    ) { padding ->
        if (!loaded) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Vacuna o tratamiento *") },
                leadingIcon = { Icon(Icons.Default.Vaccines, null) },
                isError = nameError,
                supportingText = if (nameError) { { Text("Escribe el nombre o elige una sugerencia") } } else null,
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                VACCINE_SUGGESTIONS.forEach { s -> SuggestionChip(onClick = { name = s }, label = { Text(s) }) }
            }
            DateField(
                label = "Fecha de aplicación *",
                value = applied,
                onValueChange = { applied = it },
                isError = appliedError,
                supportingText = if (appliedError) "Elige una fecha que no sea futura" else null,
            )
            DateField(
                label = "Próxima dosis / refuerzo",
                value = next,
                onValueChange = { next = it },
                clearable = true,
                isError = nextError,
                supportingText = if (nextError) "Debe ser posterior a la aplicación" else "Opcional: genera alertas en el menú",
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("+1 mes" to 1L, "+6 meses" to 6L, "+1 año" to 12L).forEach { (label, months) ->
                    SuggestionChip(
                        onClick = {
                            applied?.let { next = java.time.LocalDate.ofEpochDay(it).plusMonths(months).toEpochDay() }
                        },
                        label = { Text(label) },
                    )
                }
            }
            OutlinedTextField(
                value = vet, onValueChange = { vet = it },
                label = { Text("Veterinario / clínica") },
                leadingIcon = { Icon(Icons.Default.MedicalServices, null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = notes, onValueChange = { notes = it },
                label = { Text("Observaciones") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(onClick = { save() }, enabled = !saving, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Text(if (isEdit) "Guardar cambios" else "Guardar vacuna")
            }
        }
    }
}
