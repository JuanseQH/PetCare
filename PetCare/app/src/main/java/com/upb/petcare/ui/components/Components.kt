package com.upb.petcare.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.statusBars
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.upb.petcare.data.WeightEntry
import com.upb.petcare.ui.theme.LocalAppExtras
import com.upb.petcare.ui.theme.StatusColors
import com.upb.petcare.ui.theme.petColor
import com.upb.petcare.util.VaccineStatus
import com.upb.petcare.util.epochDayToMillis
import com.upb.petcare.util.formatDate
import com.upb.petcare.util.formatKg
import com.upb.petcare.util.formatShort
import com.upb.petcare.util.millisToEpochDay
import com.upb.petcare.util.speciesEmoji
import java.io.File

// ================================================================= Decoración

/** Dibuja una huella (almohadilla + 4 dedos) centrada en [center]. */
fun DrawScope.drawPaw(center: Offset, size: Float, color: Color, angle: Float = 0f) {
    rotate(angle, center) {
        val pad = Size(size * 0.62f, size * 0.5f)
        drawOval(color, Offset(center.x - pad.width / 2, center.y - pad.height * 0.1f), pad)
        val toe = Size(size * 0.22f, size * 0.28f)
        listOf(-0.36f to 0.02f, -0.13f to -0.2f, 0.13f to -0.2f, 0.36f to 0.02f).forEach { (dx, dy) ->
            drawOval(color, Offset(center.x + dx * size - toe.width / 2, center.y + dy * size - toe.height), toe)
        }
    }
}

/** Patrón de huellas semitransparentes para fondos de encabezados. */
@Composable
fun PawPattern(modifier: Modifier = Modifier, color: Color = Color.White.copy(alpha = 0.10f)) {
    Canvas(modifier) {
        val spots = listOf(
            Triple(0.08f, 0.18f, -20f), Triple(0.30f, 0.62f, 15f), Triple(0.55f, 0.12f, 30f),
            Triple(0.78f, 0.48f, -10f), Triple(0.94f, 0.10f, 25f), Triple(0.68f, 0.88f, -30f),
            Triple(0.14f, 0.92f, 10f), Triple(0.45f, 0.38f, -35f),
        )
        val s = size.minDimension.coerceAtMost(size.width / 4) * 0.42f
        spots.forEach { (x, y, a) -> drawPaw(Offset(size.width * x, size.height * y), s, color, a) }
    }
}

/** Fondo degradado de marca (jade) con huellas. */
@Composable
fun BrandHeader(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(MaterialTheme.colorScheme.primary, Color(0xFF145247)),
    bottomRadius: Dp = 36.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .clip(RoundedCornerShape(bottomStart = bottomRadius, bottomEnd = bottomRadius))
            .background(Brush.linearGradient(colors))
    ) {
        PawPattern(Modifier.matchParentSize())
        content()
    }
}

// ================================================================= Avatares y estados

/** Avatar de la mascota: su foto, o el emoji de la especie sobre su color, con un anillo de color. */
@Composable
fun PetAvatar(
    photoPath: String?,
    species: String,
    colorIndex: Int,
    size: Dp = 56.dp,
    ring: Dp = 3.dp,
    ringColor: Color? = null,
) {
    val color = petColor(colorIndex)
    Box(
        Modifier
            .size(size)
            .border(BorderStroke(ring, ringColor ?: color), CircleShape)
            .padding(ring + 2.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.22f)),
        contentAlignment = Alignment.Center,
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = "Foto",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(speciesEmoji(species), fontSize = (size.value * 0.42f).sp)
        }
    }
}

@Composable
fun statusColors(status: VaccineStatus): StatusColors {
    val e = LocalAppExtras.current
    return when (status) {
        VaccineStatus.OVERDUE -> e.overdue
        VaccineStatus.SOON -> e.soon
        VaccineStatus.OK -> e.ok
        VaccineStatus.DONE -> e.done
    }
}

@Composable
fun StatusChip(status: VaccineStatus, modifier: Modifier = Modifier) {
    val c = statusColors(status)
    Row(
        modifier.clip(RoundedCornerShape(50)).background(c.container).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(7.dp).background(c.dot, CircleShape))
        Spacer(Modifier.width(6.dp))
        Text(status.label, style = MaterialTheme.typography.labelMedium, color = c.content)
    }
}

// ================================================================= Contenedores

/** Tarjeta blanca suave con sombra ligera (la "pieza" básica de la interfaz). */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    radius: Dp = 24.dp,
    elevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(radius)
    val dark = LocalAppExtras.current.dark
    Column(
        modifier
            .shadow(if (dark) 0.dp else elevation, shape, ambientColor = Color(0x33584A3A), spotColor = Color(0x33584A3A))
            .clip(shape)
            .background(color)
            .then(if (dark) Modifier.border(1.dp, MaterialTheme.colorScheme.outlineVariant, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content,
    )
}

/** Ícono dentro de un círculo de color (motivo visual repetido en toda la app). */
@Composable
fun IconBadge(icon: ImageVector, container: Color, content: Color, size: Dp = 44.dp) {
    Box(Modifier.size(size).background(container, CircleShape), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = content, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
fun SectionTitle(title: String, modifier: Modifier = Modifier, action: String? = null, onAction: () -> Unit = {}) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        if (action != null) TextButton(onClick = onAction) { Text(action) }
    }
}

/** Barra superior transparente con botón de volver en círculo. */
@Composable
fun BackTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(Icons.AutoMirrored.Rounded.ArrowBack, "Volver", onBack)
        Spacer(Modifier.width(12.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f), maxLines = 1)
        actions()
    }
}

@Composable
fun CircleIconButton(
    icon: ImageVector,
    description: String,
    onClick: () -> Unit,
    container: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
    content: Color = MaterialTheme.colorScheme.onSurface,
) {
    Surface(onClick = onClick, shape = CircleShape, color = container, contentColor = content, modifier = Modifier.size(44.dp)) {
        Box(contentAlignment = Alignment.Center) { Icon(icon, contentDescription = description, modifier = Modifier.size(22.dp)) }
    }
}

@Composable
fun EmptyState(emoji: String, title: String, message: String, modifier: Modifier = Modifier, action: (@Composable () -> Unit)? = null) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            Modifier.size(120.dp).background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
            contentAlignment = Alignment.Center,
        ) { Text(emoji, fontSize = 52.sp) }
        Spacer(Modifier.height(20.dp))
        Text(title, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Spacer(Modifier.height(6.dp))
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        if (action != null) { Spacer(Modifier.height(20.dp)); action() }
    }
}

// ================================================================= Botones y campos

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    loading: Boolean = false,
    enabled: Boolean = true,
    container: Color = MaterialTheme.colorScheme.primary,
    content: Color = MaterialTheme.colorScheme.onPrimary,
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
        modifier = modifier.height(56.dp),
    ) {
        if (loading) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.5.dp, color = content)
        else {
            if (icon != null) { Icon(icon, null, Modifier.size(20.dp)); Spacer(Modifier.width(8.dp)) }
            Text(text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun TonalButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null) {
    FilledTonalButton(onClick = onClick, shape = RoundedCornerShape(50), modifier = modifier.height(48.dp)) {
        if (icon != null) { Icon(icon, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)) }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Campo de texto con el estilo de la app (relleno blanco y esquinas redondeadas). */
@Composable
fun PetTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailing: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    readOnly: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    interactionSource: MutableInteractionSource? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = leadingIcon?.let { { Icon(it, null) } },
        trailingIcon = trailing,
        isError = isError,
        supportingText = supportingText?.let { { Text(it) } },
        singleLine = singleLine,
        minLines = minLines,
        readOnly = readOnly,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        visualTransformation = visualTransformation,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            errorContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
            unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}

/** Campo de fecha de solo lectura que abre un calendario al tocarlo. */
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
    PetTextField(
        value = if (value == null) "" else formatDate(value),
        onValueChange = {},
        readOnly = true,
        label = label,
        leadingIcon = Icons.Rounded.CalendarMonth,
        trailing = if (clearable && value != null) {
            { IconButton(onClick = { onValueChange(null) }) { Icon(Icons.Rounded.Clear, contentDescription = "Quitar fecha") } }
        } else null,
        isError = isError,
        supportingText = supportingText,
        interactionSource = interaction,
        modifier = modifier,
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

/** Control segmentado en forma de píldora (pestañas). */
@Composable
fun PillTabs(options: List<String>, selected: Int, onSelect: (Int) -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(4.dp),
    ) {
        options.forEachIndexed { i, label ->
            val on = i == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .background(if (on) MaterialTheme.colorScheme.surfaceContainerLowest else Color.Transparent)
                    .clickable { onSelect(i) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (on) FontWeight.ExtraBold else FontWeight.Bold,
                )
            }
        }
    }
}

// ================================================================= Gráficos

/** Anillo de progreso animado (0..1) con contenido al centro. */
@Composable
fun ProgressRing(
    progress: Float,
    size: Dp,
    stroke: Dp = 10.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    track: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), tween(900), label = "ring")
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val s = stroke.toPx()
            val arc = Size(this.size.width - s, this.size.height - s)
            drawArc(track, 0f, 360f, false, Offset(s / 2, s / 2), arc, style = Stroke(s))
            drawArc(color, -90f, 360f * animated, false, Offset(s / 2, s / 2), arc, style = Stroke(s, cap = StrokeCap.Round))
        }
        content()
    }
}

/** Gráfica de línea del peso con área degradada, puntos y etiquetas. */
@Composable
fun WeightChart(entries: List<WeightEntry>, color: Color, modifier: Modifier = Modifier) {
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val grid = MaterialTheme.colorScheme.outlineVariant
    val surface = MaterialTheme.colorScheme.surfaceContainerLowest
    val progress by animateFloatAsState(1f, tween(800), label = "chart")
    Column(modifier) {
        Canvas(Modifier.fillMaxWidth().height(170.dp)) {
            if (entries.isEmpty()) return@Canvas
            val min = entries.minOf { it.weightKg }
            val max = entries.maxOf { it.weightKg }
            val range = (max - min).takeIf { it > 0 } ?: (max.coerceAtLeast(0.1) * 0.2)
            val top = 14.dp.toPx(); val bottom = size.height - 10.dp.toPx()
            val left = 6.dp.toPx(); val right = size.width - 6.dp.toPx()
            fun x(i: Int) = if (entries.size == 1) size.width / 2 else left + (right - left) * i / (entries.size - 1)
            fun y(v: Double) = (bottom - (bottom - top) * ((v - min + range * 0.1) / (range * 1.2))).toFloat()

            // Líneas guía
            repeat(3) { k ->
                val gy = top + (bottom - top) * k / 2
                drawLine(grid, Offset(0f, gy), Offset(size.width, gy), strokeWidth = 1.dp.toPx())
            }
            val pts = entries.mapIndexed { i, e -> Offset(x(i), y(e.weightKg) + (bottom - y(e.weightKg)) * (1 - progress)) }
            val line = Path().apply {
                moveTo(pts[0].x, pts[0].y)
                for (i in 1 until pts.size) {
                    val p0 = pts[i - 1]; val p1 = pts[i]
                    val mx = (p0.x + p1.x) / 2
                    cubicTo(mx, p0.y, mx, p1.y, p1.x, p1.y)
                }
            }
            val area = Path().apply {
                addPath(line); lineTo(pts.last().x, bottom); lineTo(pts.first().x, bottom); close()
            }
            drawPath(area, Brush.verticalGradient(listOf(color.copy(alpha = 0.35f), color.copy(alpha = 0f)), top, bottom))
            drawPath(line, color, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            pts.forEachIndexed { i, p ->
                val last = i == pts.lastIndex
                drawCircle(surface, if (last) 7.dp.toPx() else 5.dp.toPx(), p)
                drawCircle(color, if (last) 5.dp.toPx() else 3.5.dp.toPx(), p)
            }
        }
        if (entries.size > 1) {
            Row(Modifier.fillMaxWidth().padding(top = 6.dp)) {
                Text(formatShort(entries.first().date), style = MaterialTheme.typography.labelSmall, color = labelColor)
                Spacer(Modifier.weight(1f))
                Text(formatShort(entries.last().date), style = MaterialTheme.typography.labelSmall, color = labelColor)
            }
        }
    }
}

/** Pequeña "píldora" informativa (ej. "3 años", "Hembra"). */
@Composable
fun InfoPill(text: String, container: Color, content: Color, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = content,
        modifier = modifier.clip(RoundedCornerShape(50)).background(container).padding(horizontal = 12.dp, vertical = 6.dp),
    )
}

/** Indicador del cambio de peso, ej. "+1,2 kg". */
fun weightDelta(entries: List<WeightEntry>): String? {
    if (entries.size < 2) return null
    val d = entries.last().weightKg - entries.first().weightKg
    val sign = if (d >= 0) "+" else "−"
    return sign + formatKg(kotlin.math.abs(d)).let { if (kotlin.math.abs(d) < 0.0005) "0 g" else it }
}

/** Botón redondo grande de acción rápida con etiqueta debajo. */
@Composable
fun QuickAction(icon: ImageVector, label: String, container: Color, content: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(modifier.clip(RoundedCornerShape(20.dp)).clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(56.dp).clip(RoundedCornerShape(20.dp)).background(container), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = content, modifier = Modifier.size(26.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center, maxLines = 2)
    }
}

/** Borde punteado-circular para el botón "Agregar" en el carrusel de mascotas. */
@Composable
fun DashedCircle(size: Dp, color: Color, content: @Composable BoxScope.() -> Unit) {
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(
                color, style = Stroke(2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f))),
                radius = this.size.minDimension / 2 - 2.dp.toPx(),
            )
        }
        content()
    }
}

/**
 * Ajusta el color de los íconos de la barra de estado según el fondo de la pantalla:
 * [darkIcons] = true para fondos claros.
 */
@Composable
fun StatusBarIcons(darkIcons: Boolean) {
    val view = androidx.compose.ui.platform.LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.DisposableEffect(darkIcons) {
            val window = (view.context as android.app.Activity).window
            androidx.core.view.WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkIcons
            onDispose {}
        }
    }
}

/** Franja del color de fondo bajo la barra de estado, para que el contenido no se mezcle con el reloj al desplazarse. */
@Composable
fun StatusBarScrim(visible: Boolean = true, modifier: Modifier = Modifier) {
    val alpha by animateFloatAsState(if (visible) 1f else 0f, tween(200), label = "scrim")
    Box(
        modifier.fillMaxWidth()
            .windowInsetsTopHeight(androidx.compose.foundation.layout.WindowInsets.statusBars)
            .graphicsLayer { this.alpha = alpha }
            .background(MaterialTheme.colorScheme.background)
    )
}

/** Íconos oscuros solo si la pantalla tiene fondo claro y el tema es claro. */
@Composable
fun StatusBarForLightScreen() = StatusBarIcons(darkIcons = !LocalAppExtras.current.dark)
