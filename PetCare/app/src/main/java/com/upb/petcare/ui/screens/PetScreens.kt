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

// ================================================================= Detalle

@Composable
fun PetDetailScreen(
    petId: Long,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddVaccine: () -> Unit,
    onEditVaccine: (Long) -> Unit,
    vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val listState = rememberLazyListState()
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 520 } }
    StatusBarIcons(darkIcons = scrolled && !LocalAppExtras.current.dark)
    val pet by remember(petId) { vm.pet(petId) }.collectAsStateWithLifecycle(null)
    val vaccines by remember(petId) { vm.vaccinesForPet(petId) }.collectAsStateWithLifecycle(emptyList())
    val weights by remember(petId) { vm.weights(petId) }.collectAsStateWithLifecycle(emptyList())
    val alertDays by vm.alertDays.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    var showWeightDialog by rememberSaveable { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val p = pet
    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0),
        floatingActionButton = {
            if (p != null && tab != 2) {
                ExtendedFloatingActionButton(
                    onClick = { if (tab == 0) onAddVaccine() else showWeightDialog = true },
                    icon = { Icon(if (tab == 0) Icons.Rounded.Vaccines else Icons.Rounded.MonitorWeight, null) },
                    text = { Text(if (tab == 0) "Registrar vacuna" else "Registrar peso", style = MaterialTheme.typography.labelLarge) },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.navigationBarsPadding(),
                )
            }
        },
    ) { padding ->
        if (p == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val color = petColor(p.colorIndex)
        val latest = remember(vaccines) { vaccines.latestIds() }
        val score = healthScore(vaccines, alertDays)

        Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            state = listState,
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ---------- Encabezado de color de la mascota
            item {
                Box {
                    Box(
                        Modifier.fillMaxWidth().height(230.dp)
                            .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                            .background(Brush.linearGradient(listOf(color, color.copy(alpha = 0.75f))))
                    ) {
                        PawPattern(Modifier.matchParentSize(), Color.White.copy(alpha = 0.16f))
                        Row(Modifier.statusBarsPadding().padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack, Color.Black.copy(alpha = 0.22f), Color.White)
                            Spacer(Modifier.weight(1f))
                            CircleIconButton(Icons.Rounded.PictureAsPdf, "Compartir carné en PDF", {
                                if (!exporting) {
                                    exporting = true
                                    vm.sharePdf(p) { ok ->
                                        exporting = false
                                        if (!ok) scope.launch { snackbar.showSnackbar("No se pudo generar el PDF") }
                                    }
                                }
                            }, Color.Black.copy(alpha = 0.22f), Color.White)
                            Spacer(Modifier.width(8.dp))
                            CircleIconButton(Icons.Rounded.Edit, "Editar mascota", onEdit, Color.Black.copy(alpha = 0.22f), Color.White)
                            Spacer(Modifier.width(8.dp))
                            CircleIconButton(Icons.Rounded.DeleteOutline, "Eliminar mascota", { confirmDelete = true },
                                Color.Black.copy(alpha = 0.22f), Color.White)
                        }
                    }
                    Column(
                        Modifier.fillMaxWidth().padding(top = 140.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(Modifier.background(MaterialTheme.colorScheme.background, CircleShape).padding(5.dp)) {
                            PetAvatar(p.photoPath, p.species, p.colorIndex, size = 132.dp, ring = 4.dp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(p.name, style = MaterialTheme.typography.headlineLarge)
                        Text(listOf(p.species, p.breed).filter { it.isNotBlank() }.joinToString(" · "),
                            style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // ---------- Datos rápidos
            item {
                Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FactTile("🎂", "Edad", shortAge(p.birthDate), Modifier.weight(1f))
                    FactTile(if (p.sex == "Hembra") "♀" else if (p.sex == "Macho") "♂" else "❔", "Sexo", p.sex.ifBlank { "—" }, Modifier.weight(1f))
                    FactTile("⚖️", "Peso", p.weightKg?.let { formatKg(it) } ?: "—", Modifier.weight(1f))
                }
            }

            // ---------- Salud del carné
            item {
                SoftCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        val ringColor = when {
                            score == null -> MaterialTheme.colorScheme.outline
                            score >= 100 -> statusColors(VaccineStatus.OK).dot
                            score >= 50 -> statusColors(VaccineStatus.SOON).dot
                            else -> statusColors(VaccineStatus.OVERDUE).dot
                        }
                        ProgressRing((score ?: 0) / 100f, 84.dp, 9.dp, ringColor) {
                            Text(if (score == null) "—" else "$score%", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Salud del carné", style = MaterialTheme.typography.titleMedium)
                            val distinct = vaccines.map { it.name.trim().lowercase() }.distinct().size
                            val overdue = vaccines.filter { it.id in latest }.count { vaccineStatus(it.nextDoseDate, alertDays) == VaccineStatus.OVERDUE }
                            Text(
                                when {
                                    score == null -> "Registra la primera vacuna para calcularla."
                                    overdue == 0 && distinct == 1 -> "¡Excelente! Su vacuna está vigente."
                                    overdue == 0 -> "¡Excelente! Las $distinct vacunas están vigentes."
                                    else -> "$overdue de $distinct ${if (distinct == 1) "vacuna está vencida" else "vacunas están vencidas"}."
                                },
                                style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            item { PillTabs(listOf("Vacunas", "Peso", "Info"), tab, { tab = it }, Modifier.padding(horizontal = 20.dp)) }

            // ---------- Contenido de la pestaña
            item {
                AnimatedContent(tab, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "tab") { t ->
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        when (t) {
                            0 -> VaccinesTimeline(vaccines, latest, alertDays, onEditVaccine) { v ->
                                vm.deleteVaccine(v)
                                scope.launch {
                                    val r = snackbar.showSnackbar("Vacuna \"${v.name}\" eliminada", "Deshacer", duration = SnackbarDuration.Long)
                                    if (r == SnackbarResult.ActionPerformed) vm.saveVaccine(v)
                                }
                            }
                            1 -> WeightTab(weights, color, onAdd = { showWeightDialog = true }, onDelete = { vm.deleteWeight(it) })
                            else -> InfoTab(p)
                        }
                    }
                }
            }
        }
        StatusBarScrim(scrolled, Modifier.align(Alignment.TopCenter))
        }
    }

    if (confirmDelete && p != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Text(speciesEmoji(p.species), fontSize = 32.sp) },
            title = { Text("¿Eliminar a ${p.name}?") },
            text = { Text("Se borrarán su foto, su historial de vacunas y de peso. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; vm.deletePet(p, onBack) }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") } },
        )
    }
    if (showWeightDialog && p != null) {
        WeightDialog(onDismiss = { showWeightDialog = false }) { date, kg ->
            vm.addWeight(p.id, date, kg)
            showWeightDialog = false
            tab = 1
        }
    }
}

@Composable
private fun FactTile(emoji: String, label: String, value: String, modifier: Modifier) {
    SoftCard(modifier, radius = 22.dp) {
        Column(Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** Historial de vacunas como línea de tiempo vertical. */
@Composable
private fun VaccinesTimeline(
    vaccines: List<Vaccine>,
    latest: Set<Long>,
    alertDays: Int,
    onEdit: (Long) -> Unit,
    onDelete: (Vaccine) -> Unit,
) {
    if (vaccines.isEmpty()) {
        EmptyState("💉", "Sin vacunas aún", "Toca \"Registrar vacuna\" para agregar la primera al carné.")
        return
    }
    val lineColor = MaterialTheme.colorScheme.outlineVariant
    vaccines.forEachIndexed { i, v ->
        val superseded = v.id !in latest
        val status = if (superseded) VaccineStatus.DONE else vaccineStatus(v.nextDoseDate, alertDays)
        val dot = statusColors(status).dot
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Línea y punto
            Box(Modifier.width(28.dp).fillMaxHeight()) {
                Canvas(Modifier.fillMaxSize()) {
                    val cx = size.width / 2
                    if (i > 0) drawLine(lineColor, Offset(cx, 0f), Offset(cx, 30.dp.toPx()), strokeWidth = 2.dp.toPx())
                    if (i < vaccines.lastIndex) drawLine(lineColor, Offset(cx, 30.dp.toPx()), Offset(cx, size.height), strokeWidth = 2.dp.toPx())
                    drawCircle(dot.copy(alpha = 0.25f), 11.dp.toPx(), Offset(cx, 30.dp.toPx()))
                    drawCircle(dot, 6.dp.toPx(), Offset(cx, 30.dp.toPx()))
                }
            }
            Spacer(Modifier.width(8.dp))
            SoftCard(Modifier.weight(1f).padding(bottom = 12.dp), onClick = { onEdit(v.id) }, radius = 22.dp) {
                Row(Modifier.padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 4.dp), verticalAlignment = Alignment.Top) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(v.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false),
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Spacer(Modifier.width(8.dp))
                            if (!superseded) StatusChip(status)
                        }
                        Text("Aplicada el ${formatDate(v.appliedDate)}", style = MaterialTheme.typography.bodyMedium)
                        v.nextDoseDate?.let {
                            Text(
                                if (superseded) "Refuerzo ${formatDate(it)} · ya renovada" else "Refuerzo ${formatDate(it)} · ${relativeDays(it)}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (v.veterinarian.isNotBlank()) {
                            Text("🩺 ${v.veterinarian}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = { onDelete(v) }) {
                        Icon(Icons.Rounded.DeleteOutline, "Eliminar vacuna", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeightTab(weights: List<WeightEntry>, color: Color, onAdd: () -> Unit, onDelete: (WeightEntry) -> Unit) {
    if (weights.isEmpty()) {
        EmptyState("⚖️", "Sin registros de peso", "Registra el peso para ver su evolución en una gráfica.") {
            TonalButton("Registrar peso", onAdd, icon = Icons.Rounded.Add)
        }
        return
    }
    SoftCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Column(Modifier.weight(1f)) {
                    Text("Peso actual", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(formatKg(weights.last().weightKg), style = MaterialTheme.typography.headlineMedium)
                }
                weightDelta(weights)?.let {
                    InfoPill("$it desde ${formatDate(weights.first().date)}", color.copy(alpha = 0.16f), color)
                }
            }
            Spacer(Modifier.height(14.dp))
            WeightChart(weights, color)
        }
    }
    Spacer(Modifier.height(12.dp))
    weights.asReversed().forEach { w ->
        Row(
            Modifier.fillMaxWidth().padding(vertical = 2.dp).clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow).padding(start = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(formatDate(w.date), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Text(formatKg(w.weightKg), style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = { onDelete(w) }) { Icon(Icons.Rounded.Close, "Eliminar registro", Modifier.size(18.dp)) }
        }
    }
}

@Composable
private fun InfoTab(p: Pet) {
    SoftCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            InfoLine("Nacimiento", if (p.birthDate != null) "${formatDate(p.birthDate)} (${ageText(p.birthDate)})" else "Sin registrar")
            InfoLine("Especie", "${speciesEmoji(p.species)} ${p.species}")
            InfoLine("Raza", p.breed.ifBlank { "Sin registrar" })
            InfoLine("Notas y cuidados", p.notes.ifBlank { "Sin notas. Puedes agregar alergias o cuidados especiales al editar." })
        }
    }
}

@Composable
private fun InfoLine(label: String, value: String) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun WeightDialog(onDismiss: () -> Unit, onSave: (Long, Double) -> Unit) {
    var value by rememberSaveable { mutableStateOf("") }
    var date by rememberSaveable { mutableStateOf<Long?>(today()) }
    val kg = value.replace(',', '.').toDoubleOrNull()
    val valid = kg != null && kg > 0 && kg < 200 && date != null && date!! <= today()
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Text("⚖️", fontSize = 30.sp) },
        title = { Text("Registrar peso") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PetTextField(value, { value = it }, "Peso (kg)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
                DateField("Fecha", date, { date = it })
            }
        },
        confirmButton = { TextButton(enabled = valid, onClick = { onSave(date!!, kg!!) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}

