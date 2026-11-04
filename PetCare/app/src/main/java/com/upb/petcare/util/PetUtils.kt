package com.upb.petcare.util

import com.upb.petcare.data.VaccineWithPet
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

val SPECIES = listOf("Perro", "Gato", "Ave", "Conejo", "Otro")
val VACCINE_SUGGESTIONS = listOf(
    "Rabia", "Parvovirus", "Moquillo", "Hepatitis", "Triple felina",
    "Leucemia felina", "Desparasitación", "Bordetella",
)

fun speciesEmoji(species: String) = when (species) {
    "Perro" -> "🐶"
    "Gato" -> "🐱"
    "Ave" -> "🐦"
    "Conejo" -> "🐰"
    else -> "🐾"
}

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "CO"))

fun today(): Long = LocalDate.now().toEpochDay()
fun formatDate(epochDay: Long?): String = epochDay?.let { LocalDate.ofEpochDay(it).format(dateFormatter) } ?: "—"

/** Convierte entre los milisegundos UTC del DatePicker y "epochDay". */
fun epochDayToMillis(epochDay: Long): Long =
    LocalDate.ofEpochDay(epochDay).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
fun millisToEpochDay(millis: Long): Long = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toEpochDay()

fun ageText(birthEpochDay: Long?): String {
    if (birthEpochDay == null) return "Edad desconocida"
    val p = Period.between(LocalDate.ofEpochDay(birthEpochDay), LocalDate.now())
    return when {
        p.years > 0 -> "${p.years} ${if (p.years == 1) "año" else "años"}" +
            if (p.months > 0) ", ${p.months} ${if (p.months == 1) "mes" else "meses"}" else ""
        p.months > 0 -> "${p.months} ${if (p.months == 1) "mes" else "meses"}"
        else -> "${p.days.coerceAtLeast(0)} días"
    }
}

/** Texto relativo, ej. "en 5 días" o "hace 3 días". */
fun relativeDays(epochDay: Long): String {
    val diff = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.ofEpochDay(epochDay))
    return when {
        diff == 0L -> "hoy"
        diff == 1L -> "mañana"
        diff == -1L -> "ayer"
        diff > 0 -> "en $diff días"
        else -> "hace ${-diff} días"
    }
}

enum class VaccineStatus(val label: String) {
    OVERDUE("Vencida"), SOON("Próxima"), OK("Al día"), DONE("Sin refuerzo")
}

fun vaccineStatus(nextDose: Long?, alertDays: Int, today: Long = today()): VaccineStatus = when {
    nextDose == null -> VaccineStatus.DONE
    nextDose < today -> VaccineStatus.OVERDUE
    nextDose <= today + alertDays -> VaccineStatus.SOON
    else -> VaccineStatus.OK
}

/**
 * Deja solo la aplicación más reciente de cada vacuna por mascota, para que un refuerzo
 * nuevo "reemplace" la alerta del registro anterior.
 */
fun List<VaccineWithPet>.latestPerVaccine(): List<VaccineWithPet> =
    groupBy { it.vaccine.petId to it.vaccine.name.trim().lowercase() }
        .map { (_, list) -> list.maxBy { it.vaccine.appliedDate } }
