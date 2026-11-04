package com.upb.petcare.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.upb.petcare.ui.theme.statusColors
import com.upb.petcare.util.VaccineStatus
import com.upb.petcare.util.epochDayToMillis
import com.upb.petcare.util.formatDate
import com.upb.petcare.util.millisToEpochDay
import com.upb.petcare.util.speciesEmoji

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
            if (onBack != null) IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        actions = { actions() },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

/** Avatar circular con el emoji de la especie. */
@Composable
fun PetAvatar(species: String, size: Dp = 52.dp) {
    val bg = when (species) {
        "Perro" -> MaterialTheme.colorScheme.secondaryContainer
        "Gato" -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    Box(
        modifier = Modifier.size(size).background(bg, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(speciesEmoji(species), fontSize = (size.value * 0.5f).sp)
    }
}

@Composable
fun StatusChip(status: VaccineStatus) {
    val c = statusColors(
        when (status) {
            VaccineStatus.OVERDUE -> "overdue"
            VaccineStatus.SOON -> "soon"
            VaccineStatus.OK -> "ok"
            VaccineStatus.DONE -> "done"
        }
    )
    Surface(color = c.container, contentColor = c.content, shape = RoundedCornerShape(50)) {
        Text(
            status.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
fun EmptyState(icon: ImageVector, title: String, message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(16.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

/** Campo de fecha de solo lectura que abre un DatePicker al tocarlo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateField(
    label: String,
    value: Long?,
    onValueChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    clearable: Boolean = false,
    isError: Boolean = false,
    supportingText: String? = null,
) {
    var show by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    LaunchedEffect(interaction) {
        interaction.interactions.collect { if (it is PressInteraction.Release) show = true }
    }
    OutlinedTextField(
        value = if (value == null) "" else formatDate(value),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
        trailingIcon = {
            if (clearable && value != null) IconButton(onClick = { onValueChange(null) }) {
                Icon(Icons.Default.Clear, contentDescription = "Quitar fecha")
            }
        },
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        interactionSource = interaction,
        singleLine = true,
        modifier = modifier.fillMaxWidth(),
    )
    if (show) {
        val state = rememberDatePickerState(initialSelectedDateMillis = value?.let(::epochDayToMillis))
        DatePickerDialog(
            onDismissRequest = { show = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { onValueChange(millisToEpochDay(it)) }
                    show = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { show = false }) { Text("Cancelar") } },
        ) { DatePicker(state = state) }
    }
}
