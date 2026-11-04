package com.upb.petcare.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Vaccines
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.VaccineWithPet
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.PetViewModel
import com.upb.petcare.ui.components.BrandHeader
import com.upb.petcare.ui.components.DashedCircle
import com.upb.petcare.ui.components.EmptyState
import com.upb.petcare.ui.components.IconBadge
import com.upb.petcare.ui.components.PetAvatar
import com.upb.petcare.ui.components.PrimaryButton
import com.upb.petcare.ui.components.QuickAction
import com.upb.petcare.ui.components.SectionTitle
import com.upb.petcare.ui.components.SoftCard
import com.upb.petcare.ui.components.StatusBarIcons
import com.upb.petcare.ui.components.StatusBarScrim
import com.upb.petcare.ui.theme.LocalAppExtras
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.derivedStateOf
import com.upb.petcare.ui.components.StatusChip
import com.upb.petcare.ui.components.statusColors
import com.upb.petcare.ui.navigation.BottomBarSpace
import com.upb.petcare.ui.theme.PetPalette
import com.upb.petcare.util.VaccineStatus
import com.upb.petcare.util.daysUntil
import com.upb.petcare.util.formatShort
import com.upb.petcare.util.latestPerVaccine
import com.upb.petcare.util.relativeDays
import com.upb.petcare.util.todayLong
import com.upb.petcare.util.vaccineStatus

@Composable
fun HomeScreen(
    onPet: (Long) -> Unit,
    onAddPet: () -> Unit,
    onAllPets: () -> Unit,
    onCalendar: () -> Unit,
    onSettings: () -> Unit,
    onCredits: () -> Unit,
    vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val user by vm.user.collectAsStateWithLifecycle()
    val pets by vm.pets.collectAsStateWithLifecycle()
    val vaccines by vm.vaccines.collectAsStateWithLifecycle()
    val alertDays by vm.alertDays.collectAsStateWithLifecycle()

    // Alertas: última aplicación de cada vacuna que está vencida o próxima
    val alerts = remember(vaccines, alertDays) {
        vaccines.latestPerVaccine()
            .filter { vaccineStatus(it.vaccine.nextDoseDate, alertDays) in setOf(VaccineStatus.OVERDUE, VaccineStatus.SOON) }
            .sortedBy { it.vaccine.nextDoseDate }
    }
    val overdue = alerts.count { vaccineStatus(it.vaccine.nextDoseDate, alertDays) == VaccineStatus.OVERDUE }
    val firstName = user?.name?.substringBefore(' ') ?: ""

    val listState = rememberLazyListState()
    val scrolled by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 420 } }
    StatusBarIcons(darkIcons = scrolled && !LocalAppExtras.current.dark)
    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        state = listState,
        contentPadding = PaddingValues(bottom = BottomBarSpace),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // ---------- Encabezado + tarjeta destacada
        item {
            Box {
                BrandHeader(Modifier.fillMaxWidth().height(250.dp), bottomRadius = 40.dp) {
                    Row(Modifier.statusBarsPadding().padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(48.dp).background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(initials(user?.name), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onTertiaryContainer)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(todayLong(), style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.75f))
                            Text("Hola, $firstName 👋", style = MaterialTheme.typography.headlineSmall, color = Color.White,
                                maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Box {
                            Surface(onClick = onCalendar, shape = CircleShape, color = Color.White.copy(alpha = 0.16f), modifier = Modifier.size(46.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Rounded.NotificationsNone, "Alertas", tint = Color.White)
                                }
                            }
                            if (alerts.isNotEmpty()) {
                                Box(
                                    Modifier.align(Alignment.TopEnd).offset(2.dp, (-2).dp).size(20.dp)
                                        .background(MaterialTheme.colorScheme.secondary, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) { Text("${alerts.size}", color = Color.White, fontSize = 11.sp, style = MaterialTheme.typography.labelSmall) }
                            }
                        }
                    }
                }
                HeroCard(
                    loading = pets == null,
                    next = alerts.firstOrNull(),
                    hasPets = !pets.isNullOrEmpty(),
                    alertDays = alertDays,
                    onOpen = { alerts.firstOrNull()?.let { onPet(it.vaccine.petId) } },
                    onAddPet = onAddPet,
                    modifier = Modifier.padding(top = 150.dp, start = 20.dp, end = 20.dp),
                )
            }
        }

        // ---------- Menú de accesos rápidos
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                QuickAction(Icons.Rounded.Add, "Nueva\nmascota", MaterialTheme.colorScheme.secondaryContainer,
                    MaterialTheme.colorScheme.onSecondaryContainer, onAddPet, Modifier.weight(1f))
                QuickAction(Icons.Rounded.CalendarMonth, "Calendario", MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.onPrimaryContainer, onCalendar, Modifier.weight(1f))
                QuickAction(Icons.Rounded.Settings, "Ajustes", MaterialTheme.colorScheme.tertiaryContainer,
                    MaterialTheme.colorScheme.onTertiaryContainer, onSettings, Modifier.weight(1f))
                QuickAction(Icons.Rounded.Info, "Créditos", PetPalette[3].copy(alpha = 0.22f),
                    PetPalette[3], onCredits, Modifier.weight(1f))
            }
        }

        if (pets != null && pets!!.isEmpty()) {
            item {
                EmptyState("🐾", "Aún no tienes mascotas", "Registra a tu primera mascota para empezar a llevar su carné.") {
                    PrimaryButton("Agregar mascota", onAddPet, icon = Icons.Rounded.Add)
                }
            }
            return@LazyColumn
        }

        // ---------- Carrusel de mascotas
        item {
            SectionTitle("Tus mascotas", Modifier.padding(horizontal = 20.dp), action = "Ver todas", onAction = onAllPets)
            LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(pets.orEmpty(), key = { it.id }) { p ->
                    Column(
                        Modifier.width(80.dp).clip(RoundedCornerShape(20.dp)).clickable { onPet(p.id) }.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        PetAvatar(p.photoPath, p.species, p.colorIndex, size = 72.dp)
                        Spacer(Modifier.height(6.dp))
                        Text(p.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                item {
                    Column(
                        Modifier.width(80.dp).clip(RoundedCornerShape(20.dp)).clickable(onClick = onAddPet).padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        DashedCircle(72.dp, MaterialTheme.colorScheme.outline) {
                            Icon(Icons.Rounded.Add, "Agregar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Agregar", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // ---------- Resumen
        item {
            Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatTile("${pets?.size ?: 0}", "Mascotas", Icons.Rounded.Pets, MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                StatTile("${vaccines.size}", "Vacunas", Icons.Rounded.Vaccines, PetPalette[3].copy(alpha = 0.2f),
                    PetPalette[3], Modifier.weight(1f))
                StatTile("$overdue", "Vencidas", Icons.Rounded.WarningAmber, MaterialTheme.colorScheme.errorContainer,
                    MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }
        }

        // ---------- Alertas
        item { SectionTitle("Necesitan atención", Modifier.padding(horizontal = 20.dp)) }
        if (alerts.isEmpty()) {
            item {
                SoftCard(Modifier.padding(horizontal = 20.dp).fillMaxWidth()) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🎉", fontSize = 30.sp)
                        Spacer(Modifier.width(14.dp))
                        Text("¡Todo al día! No hay vacunas pendientes en los próximos $alertDays días.",
                            style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            items(alerts, key = { it.vaccine.id }) { a ->
                AlertRow(a, alertDays, Modifier.padding(horizontal = 20.dp).animateItem()) { onPet(a.vaccine.petId) }
            }
        }
    }
    StatusBarScrim(scrolled, Modifier.align(Alignment.TopCenter))
    }
}

private fun initials(name: String?): String =
    name.orEmpty().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "🐾" }

@Composable
private fun HeroCard(
    loading: Boolean,
    next: VaccineWithPet?,
    hasPets: Boolean,
    alertDays: Int,
    onOpen: () -> Unit,
    onAddPet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    SoftCard(modifier.fillMaxWidth(), onClick = if (next != null) onOpen else null, radius = 28.dp, elevation = 10.dp) {
        if (loading) {
            // Mientras cargan los datos se muestra un espacio neutro (evita un parpadeo de "Bienvenido")
            Box(Modifier.fillMaxWidth().height(136.dp))
            return@SoftCard
        }
        if (next == null) {
            Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(64.dp).background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center) { Text(if (hasPets) "🎉" else "🐾", fontSize = 32.sp) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (hasPets) "¡Todo al día!" else "Bienvenido a PetCare", style = MaterialTheme.typography.titleLarge)
                    Text(
                        if (hasPets) "Ninguna vacuna vence en los próximos $alertDays días."
                        else "Agrega tu primera mascota para empezar.",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (!hasPets) {
                        Text("Agregar mascota →", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 6.dp).clickable(onClick = onAddPet))
                    }
                }
            }
            return@SoftCard
        }
        val date = next.vaccine.nextDoseDate ?: return@SoftCard
        val status = vaccineStatus(date, alertDays)
        val days = daysUntil(date)
        val c = statusColors(status)
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("PRÓXIMA ATENCIÓN", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    PetAvatar(next.petPhoto, next.petSpecies, next.petColor, size = 52.dp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(next.vaccine.name, style = MaterialTheme.typography.titleLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${next.petName} · ${formatShort(date)}", style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StatusChip(status)
                    Spacer(Modifier.width(10.dp))
                    Text("Ver carné", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                }
            }
            // Cuenta regresiva
            Column(
                Modifier.size(width = 86.dp, height = 96.dp).background(c.container, RoundedCornerShape(24.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(if (days < 0) "${-days}" else "$days", style = MaterialTheme.typography.displaySmall, color = c.content)
                Text(
                    when { days < 0 -> "días tarde"; days == 0L -> "hoy"; days == 1L -> "día"; else -> "días" },
                    style = MaterialTheme.typography.labelMedium, color = c.content, textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, icon: ImageVector, container: Color, content: Color, modifier: Modifier) {
    SoftCard(modifier) {
        Column(Modifier.padding(14.dp)) {
            IconBadge(icon, container, content, size = 38.dp)
            Spacer(Modifier.height(10.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AlertRow(item: VaccineWithPet, alertDays: Int, modifier: Modifier, onClick: () -> Unit) {
    val next = item.vaccine.nextDoseDate ?: return
    SoftCard(modifier.fillMaxWidth(), onClick = onClick, radius = 22.dp) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            PetAvatar(item.petPhoto, item.petSpecies, item.petColor, size = 50.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(item.vaccine.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${item.petName} · ${relativeDays(next)}", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusChip(vaccineStatus(next, alertDays))
        }
    }
}
