package com.upb.petcare.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "petcare_settings")

enum class ThemeMode(val label: String) { SYSTEM("Sistema"), LIGHT("Claro"), DARK("Oscuro") }

/** Preferencias guardadas con DataStore: sesión, tema, alertas, recordatorios y onboarding. */
class SettingsRepository(private val context: Context) {

    private object Keys {
        val SESSION = longPreferencesKey("session_user_id")
        val THEME = stringPreferencesKey("theme_mode")
        val ALERT_DAYS = intPreferencesKey("alert_days")
        val LAST_EMAIL = stringPreferencesKey("last_email")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val REMINDERS = booleanPreferencesKey("reminders_enabled")
    }

    /** Id del usuario con sesión iniciada; 0 si no hay sesión. */
    val sessionUserId: Flow<Long> = context.dataStore.data.map { it[Keys.SESSION] ?: 0L }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM
    }

    /** Con cuántos días de anticipación una vacuna se marca como "Próxima". */
    val alertDays: Flow<Int> = context.dataStore.data.map { it[Keys.ALERT_DAYS] ?: 30 }
    val lastEmail: Flow<String> = context.dataStore.data.map { it[Keys.LAST_EMAIL] ?: "" }
    val onboardingDone: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }
    val remindersEnabled: Flow<Boolean> = context.dataStore.data.map { it[Keys.REMINDERS] ?: false }

    suspend fun setSession(userId: Long, email: String? = null) = context.dataStore.edit {
        it[Keys.SESSION] = userId
        if (email != null) it[Keys.LAST_EMAIL] = email
    }

    suspend fun setThemeMode(mode: ThemeMode) = context.dataStore.edit { it[Keys.THEME] = mode.name }
    suspend fun setAlertDays(days: Int) = context.dataStore.edit { it[Keys.ALERT_DAYS] = days }
    suspend fun setOnboardingDone() = context.dataStore.edit { it[Keys.ONBOARDING_DONE] = true }
    suspend fun setRemindersEnabled(enabled: Boolean) = context.dataStore.edit { it[Keys.REMINDERS] = enabled }
}
