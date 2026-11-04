package com.upb.petcare.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.upb.petcare.data.VaccineWithPet
import com.upb.petcare.ui.AppViewModelProvider
import com.upb.petcare.ui.PetViewModel
import com.upb.petcare.ui.components.CircleIconButton
import com.upb.petcare.ui.components.EmptyState
import com.upb.petcare.ui.components.PetAvatar
import com.upb.petcare.ui.components.SoftCard
import com.upb.petcare.ui.components.StatusBarForLightScreen
import com.upb.petcare.ui.components.StatusChip
import com.upb.petcare.ui.components.StatusBarScrim
import com.upb.petcare.ui.components.statusColors
import com.upb.petcare.ui.navigation.BottomBarSpace
import com.upb.petcare.util.VaccineStatus
import com.upb.petcare.util.formatDate
import com.upb.petcare.util.latestPerVaccine
import com.upb.petcare.util.monthName
import com.upb.petcare.util.relativeDays
import com.upb.petcare.util.today
import com.upb.petcare.util.vaccineStatus
import java.time.LocalDate

/** Evento del calendario: una dosis aplicada o un refuerzo programado. */
private data class CalendarEvent(val day: Long, val item: VaccineWithPet, val status: VaccineStatus, val applied: Boolean)

@Composable
fun CalendarScreen(onPet: (Long) -> Unit, vm: PetViewModel = viewModel(factory = AppViewModelProvider.Factory)) {
    StatusBarForLightScreen()
    val vaccines by vm.vaccines.collectAsStateWithLifecycle()
    val alertDays by vm.alertDays.collectAsStateWithLifecycle()
    var monthStart by rememberSaveable { mutableLongStateOf(LocalDate.now().withDayOfMonth(1).toEpochDay()) }
    var selected by rememberSaveable { mutableStateOf<Long?>(null) }
    val month = LocalDate.ofEpochDay(monthStart)

    val events = remember(vaccines, alertDays) {
        val due = vaccines.latestPerVaccine().mapNotNull { v ->
            v.vaccine.nextDoseDate?.let { CalendarEvent(it, v, vaccineStatus(it, alertDays), applied = false) }
        }
        val applied = vaccines.map { CalendarEvent(it.vaccine.appliedDate, it, VaccineStatus.DONE, applied = true) }
        (due + applied).groupBy { it.day }
    }
    val monthEnd = month.plusMonths(1).toEpochDay()
    val listed = remember(events, selected, monthStart) {
        if (selected != null) events[selected].orEmpty()
        else events.filterKeys { it in monthStart until monthEnd }.values.flatten().sortedBy { it.day }
    }

    Box(Modifier.fillMaxSize()) {
    LazyColumn(
        Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = BottomBarSpace),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(Modifier.statusBarsPadding().padding(top = 16.dp)) {
                Text("Calendario", style = MaterialTheme.typography.headlineLarge)
                Text("Vacunas aplicadas y próximos refuerzos", style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        item {
            SoftCard(Modifier.fillMaxWidth(), radius = 28.dp) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircleIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Mes anterior",
                            { monthStart = month.minusMonths(1).toEpochDay(); selected = null }, MaterialTheme.colorScheme.surfaceContainer)
                        AnimatedContent(month, transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "mes", modifier = Modifier.weight(1f)) { m ->
                            Text(monthName(m), style = MaterialTheme.typography.titleLarge, modifier = Modifier.fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                        CircleIconButton(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Mes siguiente",
                            { monthStart = month.plusMonths(1).toEpochDay(); selected = null }, MaterialTheme.colorScheme.surfaceContainer)
                    }
                    Spacer(Modifier.height(12.dp))
                    Row {
                        listOf("L", "M", "M", "J", "V", "S", "D").forEach {
                            Text(it, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    AnimatedContent(
                        monthStart,
                        transitionSpec = {
                            val dir = if (targetState > initialState) 1 else -1
                            (slideInHorizontally { it * dir / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it * dir / 3 } + fadeOut())
                        },
                        label = "grid",
                    ) { ms ->
                        MonthGrid(LocalDate.ofEpochDay(ms), events, selected) { d -> selected = if (selected == d) null else d }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Legend(statusColors(VaccineStatus.OVERDUE).dot, "Vencida")
                        Legend(statusColors(VaccineStatus.SOON).dot, "Próxima")
                        Legend(statusColors(VaccineStatus.OK).dot, "Al día")
                        Legend(MaterialTheme.colorScheme.outline, "Aplicada")
                    }
                }
            }
        }
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (selected != null) formatDate(selected) else "Este mes", style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f))
                if (selected != null) TextButton(onClick = { selected = null }) { Text("Ver todo el mes") }
            }
        }
        if (listed.isEmpty()) {
            item { EmptyState("📅", "Nada programado", if (selected != null) "No hay vacunas en este día." else "No hay vacunas este mes.") }
        } else {
            items(listed, key = { "${it.item.vaccine.id}-${it.applied}" }) { e ->
                SoftCard(Modifier.fillMaxWidth().animateItem(), onClick = { onPet(e.item.vaccine.petId) }, radius = 22.dp) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        DateBadge(e.day, if (e.applied) MaterialTheme.colorScheme.surfaceContainer else statusColors(e.status).container)
                        Spacer(Modifier.width(12.dp))
                        PetAvatar(e.item.petPhoto, e.item.petSpecies, e.item.petColor, size = 42.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(e.item.vaccine.name, style = MaterialTheme.typography.titleSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                "${e.item.petName} · ${if (e.applied) "aplicada" else "refuerzo " + relativeDays(e.day)}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        if (!e.applied) StatusChip(e.status)
                    }
                }
            }
        }
    }
    StatusBarScrim(modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
private fun MonthGrid(month: LocalDate, events: Map<Long, List<CalendarEvent>>, selected: Long?, onSelect: (Long) -> Unit) {
    val first = month.withDayOfMonth(1)
    val offset = first.dayOfWeek.value - 1 // lunes = 0
    val days = month.lengthOfMonth()
    val rows = (offset + days + 6) / 7
    val todayDay = today()
    Column {
        repeat(rows) { r ->
            Row {
                repeat(7) { c ->
                    val n = r * 7 + c - offset + 1
                    Box(Modifier.weight(1f).aspectRatio(1f).padding(2.dp), contentAlignment = Alignment.Center) {
                        if (n in 1..days) {
                            val day = first.plusDays((n - 1).toLong()).toEpochDay()
                            val isSel = day == selected
                            val isToday = day == todayDay
                            val dayEvents = events[day].orEmpty()
                            Column(
                                Modifier.fillMaxSize().clip(CircleShape)
                                    .background(if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .then(if (isToday && !isSel) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                                    .clickable { onSelect(day) },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    "$n", style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isToday || dayEvents.isNotEmpty()) FontWeight.ExtraBold else FontWeight.Normal,
                                    color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.height(6.dp)) {
                                    dayEvents.take(3).forEach { e ->
                                        val dot = if (e.applied) MaterialTheme.colorScheme.outline else statusColors(e.status).dot
                                        Box(Modifier.size(5.dp).background(if (isSel) Color.White else dot, CircleShape))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Legend(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).background(color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DateBadge(day: Long, container: Color) {
    val d = LocalDate.ofEpochDay(day)
    Column(
        Modifier.size(48.dp).clip(androidx.compose.foundation.shape.RoundedCornerShape(16.dp)).background(container),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("${d.dayOfMonth}", style = MaterialTheme.typography.titleMedium)
        Text(
            d.month.getDisplayName(java.time.format.TextStyle.SHORT, com.upb.petcare.util.ES).uppercase().take(3),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
