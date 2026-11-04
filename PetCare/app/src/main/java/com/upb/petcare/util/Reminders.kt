package com.upb.petcare.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.upb.petcare.MainActivity
import com.upb.petcare.PetCareApp
import com.upb.petcare.R
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/** Recordatorios de vacunas: una revisión diaria en segundo plano con WorkManager. */
object Reminders {
    const val CHANNEL_ID = "vaccine_reminders"
    const val KEY_FORCE = "force"
    private const val WORK_NAME = "daily_vaccine_check"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        val channel = NotificationChannel(CHANNEL_ID, "Recordatorios de vacunas", NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Avisos de vacunas vencidas o próximas a vencer"
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun schedule(context: Context) {
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS).build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
    }

    fun cancel(context: Context) = WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)

    /** Ejecuta la revisión de inmediato (útil para probar o para la demostración). */
    fun runNow(context: Context) {
        WorkManager.getInstance(context).enqueue(
            OneTimeWorkRequestBuilder<ReminderWorker>().setInputData(workDataOf(KEY_FORCE to true)).build()
        )
    }

    fun notify(context: Context, title: String, lines: List<String>) {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return
        val open = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val style = NotificationCompat.InboxStyle().also { s -> lines.take(6).forEach { s.addLine(it) } }
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_paw)
            .setColor(0xFF1E6B5C.toInt())
            .setContentTitle(title)
            .setContentText(lines.firstOrNull() ?: "")
            .setStyle(style)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        try {
            manager.notify(NOTIFICATION_ID, notification)
        } catch (_: SecurityException) {
            // El usuario no concedió el permiso de notificaciones
        }
    }
}

class ReminderWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val container = (applicationContext as PetCareApp).container
        val ownerId = container.settings.sessionUserId.first()
        if (ownerId == 0L || !container.settings.remindersEnabled.first()) return Result.success()
        val alertDays = container.settings.alertDays.first()

        val pending = container.pets.vaccinesForOwnerOnce(ownerId).latestPerVaccine()
            .mapNotNull { v -> v.vaccine.nextDoseDate?.let { next -> Triple(v, next, vaccineStatus(next, alertDays)) } }
            .filter { it.third == VaccineStatus.OVERDUE || it.third == VaccineStatus.SOON }
            .sortedBy { it.second }

        if (pending.isNotEmpty()) {
            val overdue = pending.count { it.third == VaccineStatus.OVERDUE }
            val title = if (overdue > 0) "🐾 $overdue ${if (overdue == 1) "vacuna vencida" else "vacunas vencidas"}"
            else "🐾 ${pending.size} ${if (pending.size == 1) "vacuna próxima" else "vacunas próximas"}"
            val lines = pending.map { (v, next, _) -> "${v.petName}: ${v.vaccine.name} · ${relativeDays(next)}" }
            Reminders.notify(applicationContext, title, lines)
        } else if (inputData.getBoolean(Reminders.KEY_FORCE, false)) {
            Reminders.notify(applicationContext, "🐾 ¡Todo al día!", listOf("No hay vacunas pendientes en los próximos $alertDays días."))
        }
        return Result.success()
    }
}
