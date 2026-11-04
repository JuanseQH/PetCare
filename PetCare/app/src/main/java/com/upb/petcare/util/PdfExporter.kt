package com.upb.petcare.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.upb.petcare.data.Pet
import com.upb.petcare.data.Vaccine
import com.upb.petcare.data.WeightEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** Genera el carné de vacunación de una mascota en PDF (A4) y permite compartirlo. */
object PdfExporter {
    private const val W = 595
    private const val H = 842
    private const val M = 40f

    private val JADE = 0xFF1E6B5C.toInt()
    private val JADE_LIGHT = 0xFFCDEBE2.toInt()
    private val INK = 0xFF1E2522.toInt()
    private val MUTED = 0xFF5E6763.toInt()
    private val LINE = 0xFFE3DCD2.toInt()

    suspend fun export(
        context: Context,
        pet: Pet,
        vaccines: List<Vaccine>,
        weights: List<WeightEntry>,
        ownerName: String,
        alertDays: Int,
    ): File = withContext(Dispatchers.IO) {
        val doc = PdfDocument()
        var pageNo = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create())
        var c = page.canvas

        val title = paint(22f, bold = true, color = 0xFFFFFFFF.toInt())
        val h2 = paint(14f, bold = true, color = JADE)
        val body = paint(10.5f, color = INK)
        val small = paint(9f, color = MUTED)
        val bold = paint(10.5f, bold = true, color = INK)

        // Encabezado
        c.drawRect(0f, 0f, W.toFloat(), 120f, Paint().apply { color = JADE })
        c.drawText("Carné de vacunación", M, 58f, title)
        c.drawText("PetCare · generado el ${formatDate(today())}", M, 82f, paint(11f, color = 0xFFCDEBE2.toInt()))

        // Foto o emoji de la mascota
        val photo = pet.photoPath?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
        val avatar = RectF(W - M - 84f, 18f, W - M, 102f)
        c.drawOval(avatar, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFFFFFFFF.toInt() })
        if (photo != null) {
            val crop = centerCrop(photo)
            c.save()
            c.clipPath(android.graphics.Path().apply { addOval(RectF(avatar).apply { inset(3f, 3f) }, android.graphics.Path.Direction.CW) })
            c.drawBitmap(crop, null, RectF(avatar).apply { inset(3f, 3f) }, Paint(Paint.FILTER_BITMAP_FLAG))
            c.restore()
        } else {
            c.drawText(speciesEmoji(pet.species), avatar.centerX() - 20f, avatar.centerY() + 14f, paint(40f))
        }

        // Datos de la mascota
        var y = 160f
        c.drawText(pet.name, M, y, paint(24f, bold = true, color = INK)); y += 22f
        c.drawText(listOf(pet.species, pet.breed).filter { it.isNotBlank() }.joinToString(" · "), M, y, paint(12f, color = MUTED)); y += 26f

        val box = RectF(M, y, W - M, y + 64f)
        c.drawRoundRect(box, 12f, 12f, Paint(Paint.ANTI_ALIAS_FLAG).apply { color = JADE_LIGHT })
        val cols = listOf(
            "Edad" to ageText(pet.birthDate),
            "Sexo" to pet.sex.ifBlank { "—" },
            "Peso" to (pet.weightKg?.let { formatKg(it) } ?: "—"),
            "Nacimiento" to formatDate(pet.birthDate),
        )
        val colW = (W - 2 * M) / cols.size
        cols.forEachIndexed { i, (k, v) ->
            c.drawText(k, M + 14f + i * colW, y + 24f, small)
            c.drawText(v, M + 14f + i * colW, y + 44f, bold)
        }
        y += 84f
        if (pet.notes.isNotBlank()) {
            c.drawText("Notas: ${pet.notes}", M, y, body); y += 22f
        }
        c.drawText("Propietario: $ownerName", M, y, body); y += 34f

        // Tabla de vacunas
        c.drawText("Historial de vacunas", M, y, h2); y += 18f
        val colX = floatArrayOf(M, M + 150f, M + 245f, M + 340f, M + 430f)
        val headers = listOf("Vacuna", "Aplicada", "Refuerzo", "Estado", "Veterinario")
        c.drawRect(M, y, W - M, y + 22f, Paint().apply { color = 0xFFF0E9E0.toInt() })
        headers.forEachIndexed { i, s -> c.drawText(s, colX[i] + 6f, y + 15f, paint(9.5f, bold = true, color = MUTED)) }
        y += 22f
        val latest = vaccines.latestIds()
        if (vaccines.isEmpty()) {
            c.drawText("Sin vacunas registradas.", M + 6f, y + 18f, body); y += 28f
        }
        vaccines.forEach { v ->
            if (y > H - 90) {
                doc.finishPage(page); pageNo++
                page = doc.startPage(PdfDocument.PageInfo.Builder(W, H, pageNo).create()); c = page.canvas; y = M + 10f
            }
            val status = if (v.id in latest) vaccineStatus(v.nextDoseDate, alertDays) else null
            c.drawText(v.name.take(24), colX[0] + 6f, y + 17f, bold)
            c.drawText(formatDate(v.appliedDate), colX[1] + 6f, y + 17f, body)
            c.drawText(formatDate(v.nextDoseDate), colX[2] + 6f, y + 17f, body)
            c.drawText(status?.label ?: "Renovada", colX[3] + 6f, y + 17f, paint(10.5f, bold = true, color = statusColor(status)))
            c.drawText(v.veterinarian.take(22), colX[4] + 6f, y + 17f, small)
            y += 26f
            c.drawLine(M, y, W - M, y, Paint().apply { color = LINE })
        }

        // Peso
        if (weights.isNotEmpty() && y < H - 120) {
            y += 30f
            c.drawText("Control de peso", M, y, h2); y += 20f
            val first = weights.first(); val last = weights.last()
            c.drawText(
                "Primera medición: ${formatKg(first.weightKg)} (${formatDate(first.date)})  ·  " +
                    "Última: ${formatKg(last.weightKg)} (${formatDate(last.date)})",
                M, y, body,
            )
        }

        c.drawText("Documento generado con PetCare · Aplicaciones Móviles UPB", M, H - 30f, small)
        doc.finishPage(page)

        val dir = File(context.cacheDir, "pdf").apply { mkdirs() }
        val file = File(dir, "Carne_${pet.name.replace(Regex("[^A-Za-z0-9]"), "_")}.pdf")
        file.outputStream().use { doc.writeTo(it) }
        doc.close()
        file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Carné de vacunación")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir carné").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun statusColor(s: VaccineStatus?) = when (s) {
        VaccineStatus.OVERDUE -> 0xFFC0362C.toInt()
        VaccineStatus.SOON -> 0xFFB7800F.toInt()
        VaccineStatus.OK -> 0xFF1E6B5C.toInt()
        else -> MUTED
    }

    private fun paint(size: Float, bold: Boolean = false, color: Int = INK) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size
        this.color = color
        typeface = Typeface.create(Typeface.SANS_SERIF, if (bold) Typeface.BOLD else Typeface.NORMAL)
    }

    private fun centerCrop(b: Bitmap): Bitmap {
        val s = minOf(b.width, b.height)
        return Bitmap.createBitmap(b, (b.width - s) / 2, (b.height - s) / 2, s, s)
    }
}
