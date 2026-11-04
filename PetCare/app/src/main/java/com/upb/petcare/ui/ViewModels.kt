package com.upb.petcare.ui

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.upb.petcare.PetCareApp
import com.upb.petcare.data.AuthRepository
import com.upb.petcare.data.AuthResult
import com.upb.petcare.data.Pet
import com.upb.petcare.data.PetRepository
import com.upb.petcare.data.SettingsRepository
import com.upb.petcare.data.ThemeMode
import com.upb.petcare.data.User
import com.upb.petcare.data.Vaccine
import com.upb.petcare.data.VaccineWithPet
import com.upb.petcare.data.WeightEntry
import com.upb.petcare.util.PdfExporter
import com.upb.petcare.util.PhotoStorage
import com.upb.petcare.util.Reminders
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

private fun CreationExtras.app() =
    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PetCareApp

object AppViewModelProvider {
    val Factory = viewModelFactory {
        initializer { app().container.let { AuthViewModel(it.auth, it.settings) } }
        initializer { app().container.let { PetViewModel(it.appContext, it.pets, it.auth, it.settings) } }
        initializer { app().container.let { SettingsViewModel(it.appContext, it.auth, it.pets, it.settings) } }
    }
}

private fun <T> Flow<T>.stateIn(vm: ViewModel, initial: T): StateFlow<T> =
    stateIn(vm.viewModelScope, SharingStarted.WhileSubscribed(5_000), initial)

class AuthViewModel(private val auth: AuthRepository, private val settings: SettingsRepository) : ViewModel() {
    val currentUser: StateFlow<User?> = auth.currentUser.stateIn(this, null)
    val lastEmail: StateFlow<String> = settings.lastEmail.stateIn(this, "")

    fun login(email: String, password: String, onResult: (AuthResult) -> Unit) =
        viewModelScope.launch { onResult(auth.login(email, password)) }

    fun register(name: String, email: String, password: String, onResult: (AuthResult) -> Unit) =
        viewModelScope.launch { onResult(auth.register(name, email, password)) }

    fun logout(onDone: () -> Unit) = viewModelScope.launch { auth.logout(); onDone() }

    fun completeOnboarding(onDone: () -> Unit) = viewModelScope.launch { settings.setOnboardingDone(); onDone() }
}

@OptIn(ExperimentalCoroutinesApi::class)
class PetViewModel(
    private val context: Context,
    private val repo: PetRepository,
    private val auth: AuthRepository,
    private val settings: SettingsRepository,
) : ViewModel() {

    val user: StateFlow<User?> = auth.currentUser.stateIn(this, null)

    val pets: StateFlow<List<Pet>?> =
        settings.sessionUserId.flatMapLatest { if (it == 0L) flowOf(emptyList()) else repo.pets(it) }.stateIn(this, null)

    val vaccines: StateFlow<List<VaccineWithPet>> =
        settings.sessionUserId.flatMapLatest { if (it == 0L) flowOf(emptyList()) else repo.vaccinesForOwner(it) }
            .stateIn(this, emptyList())

    val alertDays: StateFlow<Int> = settings.alertDays.stateIn(this, 30)

    fun pet(id: Long): Flow<Pet?> = repo.pet(id)
    fun vaccinesForPet(petId: Long): Flow<List<Vaccine>> = repo.vaccinesForPet(petId)
    fun weights(petId: Long): Flow<List<WeightEntry>> = repo.weights(petId)

    suspend fun getPet(id: Long) = repo.getPet(id)
    suspend fun getVaccine(id: Long) = repo.getVaccine(id)

    /** Copia la foto elegida al almacenamiento interno y devuelve su ruta. */
    suspend fun importPhoto(uri: Uri): String? = PhotoStorage.save(context, uri)

    fun savePet(pet: Pet, onSaved: (Long) -> Unit) = viewModelScope.launch {
        val ownerId = settings.sessionUserId.first()
        onSaved(repo.savePet(pet.copy(ownerId = ownerId)))
    }

    fun deletePet(pet: Pet, onDone: () -> Unit) = viewModelScope.launch { repo.deletePet(pet); onDone() }

    fun saveVaccine(vaccine: Vaccine, onDone: () -> Unit = {}) = viewModelScope.launch { repo.saveVaccine(vaccine); onDone() }

    fun deleteVaccine(vaccine: Vaccine) = viewModelScope.launch { repo.deleteVaccine(vaccine) }

    fun addWeight(petId: Long, date: Long, kg: Double) = viewModelScope.launch { repo.addWeight(petId, date, kg) }

    fun deleteWeight(entry: WeightEntry) = viewModelScope.launch { repo.deleteWeight(entry) }

    /** Genera el PDF del carné y abre el menú de compartir del sistema. */
    fun sharePdf(pet: Pet, onDone: (Boolean) -> Unit) = viewModelScope.launch {
        val ok = runCatching {
            val file: File = PdfExporter.export(
                context, pet,
                repo.vaccinesForPetOnce(pet.id),
                repo.weights(pet.id).first(),
                auth.currentUser.first()?.name ?: "",
                alertDays.value,
            )
            PdfExporter.share(context, file)
        }.isSuccess
        onDone(ok)
    }
}

class SettingsViewModel(
    private val context: Context,
    private val auth: AuthRepository,
    private val pets: PetRepository,
    private val settings: SettingsRepository,
) : ViewModel() {
    val user: StateFlow<User?> = auth.currentUser.stateIn(this, null)
    val themeMode: StateFlow<ThemeMode> = settings.themeMode.stateIn(this, ThemeMode.SYSTEM)
    val alertDays: StateFlow<Int> = settings.alertDays.stateIn(this, 30)
    val reminders: StateFlow<Boolean> = settings.remindersEnabled.stateIn(this, false)

    fun setTheme(mode: ThemeMode) = viewModelScope.launch { settings.setThemeMode(mode) }
    fun setAlertDays(days: Int) = viewModelScope.launch { settings.setAlertDays(days) }
    fun updateName(name: String) = viewModelScope.launch { user.value?.let { auth.updateName(it.id, name) } }
    fun seedDemo(onDone: () -> Unit) = viewModelScope.launch { user.value?.let { pets.seedDemo(it.id) }; onDone() }
    fun deleteAll(onDone: () -> Unit) = viewModelScope.launch { user.value?.let { pets.deleteAll(it.id) }; onDone() }
    fun logout(onDone: () -> Unit) = viewModelScope.launch { auth.logout(); onDone() }

    fun setReminders(enabled: Boolean) = viewModelScope.launch {
        settings.setRemindersEnabled(enabled)
        if (enabled) { Reminders.schedule(context); Reminders.runNow(context) } else Reminders.cancel(context)
    }

    fun testReminder() = Reminders.runNow(context)
}
