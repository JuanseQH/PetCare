package com.upb.petcare.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.MedicalServices
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Vaccines
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.Pet
import com.upb.petcare.data.Vaccine
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.PetViewModel
import com.upb.petcare.ui.components.BackTopBar
import com.upb.petcare.ui.components.DateField
import com.upb.petcare.ui.components.PetAvatar
import com.upb.petcare.ui.components.PetTextField
import com.upb.petcare.ui.components.PillTabs
import com.upb.petcare.ui.components.PrimaryButton
import com.upb.petcare.ui.components.SoftCard
import com.upb.petcare.ui.components.StatusBarForLightScreen
import com.upb.petcare.ui.theme.PetPalette
import com.upb.petcare.ui.theme.petColor
import com.upb.petcare.util.SPECIES
import com.upb.petcare.util.VACCINE_SUGGESTIONS
import com.upb.petcare.util.speciesEmoji
import com.upb.petcare.util.today
import kotlinx.coroutines.launch
import java.time.LocalDate

/** Tarjeta de sección de formulario con título. */
@Composable
private fun FormSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    SoftCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            content()
        }
    }
}

// ================================================================= Formulario de mascota

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PetFormScreen(
    petId: Long,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit,
    vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    StatusBarForLightScreen()
    val isEdit = petId != 0L
    val pets by vm.pets.collectAsStateWithLifecycle()
    var loaded by rememberSaveable { mutableStateOf(!isEdit) }
    var name by rememberSaveable { mutableStateOf("") }
    var species by rememberSaveable { mutableStateOf("Perro") }
    var breed by rememberSaveable { mutableStateOf("") }
    var sex by rememberSaveable { mutableStateOf("") }
    var birthDate by rememberSaveable { mutableStateOf<Long?>(null) }
    var weight by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    var photo by rememberSaveable { mutableStateOf<String?>(null) }
    var colorIndex by rememberSaveable { mutableIntStateOf(-1) }
    var submitted by rememberSaveable { mutableStateOf(false) }
    var saving by rememberSaveable { mutableStateOf(false) }
    var importing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(petId) {
        if (isEdit && !loaded) {
            vm.getPet(petId)?.let { p ->
                name = p.name; species = p.species; breed = p.breed; sex = p.sex
                birthDate = p.birthDate; notes = p.notes; photo = p.photoPath; colorIndex = p.colorIndex
                weight = p.weightKg?.toString()?.removeSuffix(".0") ?: ""
            }
            loaded = true
        }
    }
    // Mascota nueva: color sugerido distinto al de las mascotas existentes
    LaunchedEffect(pets) {
        if (!isEdit && colorIndex == -1 && pets != null) {
            val used = pets!!.map { it.colorIndex }.toSet()
            colorIndex = PetPalette.indices.firstOrNull { it !in used } ?: (pets!!.size % PetPalette.size)
        }
    }

    // Selector de fotos del sistema (no requiere permisos)
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) scope.launch {
            importing = true
            vm.importPhoto(uri)?.let { photo = it }
            importing = false
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
                id = petId, ownerId = 0, name = name.trim(), species = species, breed = breed.trim(), sex = sex,
                birthDate = birthDate, weightKg = weightValue, notes = notes.trim(), photoPath = photo,
                colorIndex = colorIndex.coerceAtLeast(0),
            )
        ) { onSaved(it) }
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BackTopBar(if (isEdit) "Editar mascota" else "Nueva mascota", onBack)
        if (!loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ---------- Foto y color
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(Modifier.clip(CircleShape).clickable {
                        picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        PetAvatar(photo, species, colorIndex.coerceAtLeast(0), size = 132.dp, ring = 4.dp)
                        if (importing) CircularProgressIndicator(Modifier.align(Alignment.Center))
                    }
                    Box(
                        Modifier.size(42.dp).background(MaterialTheme.colorScheme.secondary, CircleShape)
                            .border(3.dp, MaterialTheme.colorScheme.background, CircleShape)
                            .clickable { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Rounded.PhotoCamera, "Elegir foto", tint = Color.White, modifier = Modifier.size(20.dp)) }
                }
                if (photo != null) TextButton(onClick = { photo = null }) { Text("Quitar foto") }
                else Text("Toca para agregar una foto", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 6.dp))
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PetPalette.forEachIndexed { i, c ->
                        val selected = i == colorIndex
                        val s by animateFloatAsState(if (selected) 1.15f else 1f, label = "color")
                        Box(
                            Modifier.size(30.dp).scale(s).clip(CircleShape).background(c).clickable { colorIndex = i }
                                .then(if (selected) Modifier.border(3.dp, Color.White, CircleShape) else Modifier),
                            contentAlignment = Alignment.Center,
                        ) { if (selected) Icon(Icons.Rounded.Check, "Color elegido", tint = Color.White, modifier = Modifier.size(16.dp)) }
                    }
                }
            }

            // ---------- Datos básicos
            FormSection("Datos básicos") {
                PetTextField(
                    value = name, onValueChange = { name = it }, label = "Nombre *",
                    isError = nameError, supportingText = if (nameError) "El nombre es obligatorio" else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
                Text("Especie", style = MaterialTheme.typography.labelLarge)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SPECIES.forEach { s ->
                        val on = species == s
                        val accent = petColor(colorIndex.coerceAtLeast(0))
                        Column(
                            Modifier.weight(1f).clip(RoundedCornerShape(18.dp))
                                .background(if (on) accent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceContainer)
                                .then(if (on) Modifier.border(2.dp, accent, RoundedCornerShape(18.dp)) else Modifier)
                                .clickable { species = s }.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(speciesEmoji(s), fontSize = 24.sp)
                            Text(s, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
                PetTextField(value = breed, onValueChange = { breed = it }, label = "Raza",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words))
                Text("Sexo", style = MaterialTheme.typography.labelLarge)
                PillTabs(listOf("♂  Macho", "♀  Hembra"), when (sex) { "Macho" -> 0; "Hembra" -> 1; else -> -1 },
                    { sex = if (it == 0) "Macho" else "Hembra" })
            }

            // ---------- Salud
            FormSection("Salud y cuidados") {
                DateField("Fecha de nacimiento", birthDate, { birthDate = it }, clearable = true,
                    isError = birthError, supportingText = if (birthError) "La fecha no puede ser futura" else null)
                PetTextField(
                    value = weight, onValueChange = { weight = it }, label = "Peso actual (kg)",
                    isError = weightError, supportingText = if (weightError) "Ingresa un peso válido" else "Se guarda en el historial de peso",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                )
                PetTextField(value = notes, onValueChange = { notes = it }, label = "Notas (alergias, cuidados...)",
                    singleLine = false, minLines = 3)
            }
        }
        PrimaryButton(
            if (isEdit) "Guardar cambios" else "Guardar mascota", { save() },
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            icon = Icons.Rounded.Check, loading = saving,
        )
    }
}

// ================================================================= Formulario de vacuna

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VaccineFormScreen(
    petId: Long,
    vaccineId: Long,
    onBack: () -> Unit,
    vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    StatusBarForLightScreen()
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
    val appliedError = (submitted && applied == null) || (applied != null && applied!! > today())
    val nextError = next != null && applied != null && next!! <= applied!!

    fun save() {
        submitted = true
        if (name.isBlank() || applied == null || applied!! > today() || nextError) return
        saving = true
        vm.saveVaccine(
            Vaccine(id = vaccineId, petId = petId, name = name.trim(), appliedDate = applied!!,
                nextDoseDate = next, veterinarian = vet.trim(), notes = notes.trim()),
            onBack,
        )
    }

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        BackTopBar(if (isEdit) "Editar vacuna" else "Registrar vacuna", onBack)
        if (!loaded) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Column
        }
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).imePadding().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            pet?.let { p ->
                SoftCard(Modifier.fillMaxWidth(), color = petColor(p.colorIndex).copy(alpha = 0.14f), elevation = 0.dp) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        PetAvatar(p.photoPath, p.species, p.colorIndex, size = 52.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Carné de", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(p.name, style = MaterialTheme.typography.titleLarge)
                        }
                    }
                }
            }
            FormSection("Vacuna o tratamiento") {
                PetTextField(
                    value = name, onValueChange = { name = it }, label = "Nombre *", leadingIcon = Icons.Rounded.Vaccines,
                    isError = nameError, supportingText = if (nameError) "Escribe el nombre o elige una sugerencia" else null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                )
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VACCINE_SUGGESTIONS.forEach { s ->
                        val on = name.equals(s, true)
                        Text(
                            s, style = MaterialTheme.typography.labelLarge,
                            color = if (on) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.clip(RoundedCornerShape(50))
                                .background(if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { name = s }.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            FormSection("Fechas") {
                DateField("Fecha de aplicación *", applied, { applied = it }, isError = appliedError,
                    supportingText = if (appliedError) "Elige una fecha que no sea futura" else null)
                DateField("Próxima dosis / refuerzo", next, { next = it }, clearable = true, isError = nextError,
                    supportingText = if (nextError) "Debe ser posterior a la aplicación" else "Opcional: activa las alertas y recordatorios")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("+1 mes" to 1L, "+6 meses" to 6L, "+1 año" to 12L).forEach { (label, months) ->
                        Text(
                            label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.secondaryContainer)
                                .clickable { applied?.let { next = LocalDate.ofEpochDay(it).plusMonths(months).toEpochDay() } }
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                }
            }
            FormSection("Detalles") {
                PetTextField(value = vet, onValueChange = { vet = it }, label = "Veterinario / clínica",
                    leadingIcon = Icons.Rounded.MedicalServices,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words))
                PetTextField(value = notes, onValueChange = { notes = it }, label = "Observaciones", singleLine = false, minLines = 2)
            }
        }
        PrimaryButton(
            if (isEdit) "Guardar cambios" else "Guardar vacuna", { save() },
            Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
            icon = Icons.Rounded.Check, loading = saving,
        )
    }
}
