package com.upb.petcare.util

import com.upb.petcare.data.Vaccine
import com.upb.petcare.data.VaccineWithPet
import java.time.Instant
import java.time.LocalDate
import java.time.Period
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

val SPECIES = listOf("Perro", "Gato", "Ave", "Conejo", "Otro")
val VACCINE_SUGGESTIONS = listOf(
    "Rabia", "Parvovirus", "Moquillo", "Hepatitis", "Triple felina",
    "Leucemia felina", "Desparasitación", "Bordetella",
)

val ES: Locale = Locale("es", "CO")

fun speciesEmoji(species: String) = when (species) {
    "Perro" -> "🐶"
    "Gato" -> "🐱"
    "Ave" -> "🐦"
    "Conejo" -> "🐰"
    else -> "🐾"
}

private val dateFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", ES)
private val shortFormatter = DateTimeFormatter.ofPattern("d MMM", ES)

fun today(): Long = LocalDate.now().toEpochDay()
fun formatDate(epochDay: Long?): String = epochDay?.let { LocalDate.ofEpochDay(it).format(dateFormatter) } ?: "—"
fun formatShort(epochDay: Long): String = LocalDate.ofEpochDay(epochDay).format(shortFormatter)

fun monthName(date: LocalDate): String =
    date.month.getDisplayName(TextStyle.FULL_STANDALONE, ES).replaceFirstChar { it.titlecase(ES) } + " " + date.year

fun todayLong(): String =
    LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", ES)).replaceFirstChar { it.titlecase(ES) }

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

fun shortAge(birthEpochDay: Long?): String {
    if (birthEpochDay == null) return "—"
    val p = Period.between(LocalDate.ofEpochDay(birthEpochDay), LocalDate.now())
    return when {
        p.years > 0 -> "${p.years} ${if (p.years == 1) "año" else "años"}"
        p.months > 0 -> "${p.months} m"
        else -> "${p.days.coerceAtLeast(0)} d"
    }
}

fun formatKg(kg: Double): String =
    if (kg < 1) "${(kg * 1000).toInt()} g" else "%.1f kg".format(ES, kg).replace(",0 kg", " kg")

fun daysUntil(epochDay: Long): Long = ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.ofEpochDay(epochDay))

/** Texto relativo, ej. "en 5 días" o "hace 3 días". */
fun relativeDays(epochDay: Long): String {
    val diff = daysUntil(epochDay)
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

fun List<Vaccine>.latestIds(): Set<Long> =
    groupBy { it.name.trim().lowercase() }.map { (_, l) -> l.maxBy { it.appliedDate }.id }.toSet()

/**
 * Puntaje de salud del carné (0..100): porcentaje de vacunas vigentes
 * (al día, próximas o sin refuerzo) sobre el total de vacunas distintas.
 */
fun healthScore(vaccines: List<Vaccine>, alertDays: Int): Int? {
    if (vaccines.isEmpty()) return null
    val latest = vaccines.groupBy { it.name.trim().lowercase() }.map { (_, l) -> l.maxBy { it.appliedDate } }
    val ok = latest.count { vaccineStatus(it.nextDoseDate, alertDays) != VaccineStatus.OVERDUE }
    return ok * 100 / latest.size
}
