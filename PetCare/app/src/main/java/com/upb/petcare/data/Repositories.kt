package com.upb.petcare.data

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.io.File
import java.security.MessageDigest
import java.time.LocalDate

sealed interface AuthResult {
    data class Success(val user: User) : AuthResult
    data class Error(val message: String) : AuthResult
}

class AuthRepository(private val dao: UserDao, private val settings: SettingsRepository) {

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentUser: Flow<User?> = settings.sessionUserId.flatMapLatest { id ->
        if (id == 0L) flowOf(null) else dao.observe(id)
    }

    suspend fun login(email: String, password: String): AuthResult {
        val normalized = email.trim().lowercase()
        val user = dao.findByEmail(normalized)
            ?: return AuthResult.Error("No existe una cuenta con ese correo")
        if (user.passwordHash != hash(normalized, password)) {
            return AuthResult.Error("Contraseña incorrecta")
        }
        settings.setSession(user.id, normalized)
        return AuthResult.Success(user)
    }

    suspend fun register(name: String, email: String, password: String): AuthResult {
        val normalized = email.trim().lowercase()
        if (dao.findByEmail(normalized) != null) return AuthResult.Error("Ese correo ya está registrado")
        return try {
            val user = User(name = name.trim(), email = normalized, passwordHash = hash(normalized, password))
            val id = dao.insert(user)
            settings.setSession(id, normalized)
            AuthResult.Success(user.copy(id = id))
        } catch (e: SQLiteConstraintException) {
            AuthResult.Error("Ese correo ya está registrado")
        }
    }

    suspend fun logout() = settings.setSession(0L)

    suspend fun updateName(id: Long, name: String) = dao.updateName(id, name.trim())

    companion object {
        /** Hash SHA-256 de la contraseña usando el correo como "sal". */
        fun hash(email: String, password: String): String {
            val bytes = MessageDigest.getInstance("SHA-256").digest("$email:$password".toByteArray())
            return bytes.joinToString("") { "%02x".format(it) }
        }
    }
}

class PetRepository(
    private val petDao: PetDao,
    private val vaccineDao: VaccineDao,
    private val weightDao: WeightDao,
) {
    fun pets(ownerId: Long): Flow<List<Pet>> = petDao.observeByOwner(ownerId)
    fun pet(id: Long): Flow<Pet?> = petDao.observe(id)
    suspend fun getPet(id: Long): Pet? = petDao.get(id)
    suspend fun petsOnce(ownerId: Long) = petDao.getByOwner(ownerId)

    /** Guarda la mascota. Si es nueva y tiene peso, crea también el primer registro de peso. */
    suspend fun savePet(pet: Pet): Long {
        return if (pet.id == 0L) {
            val id = petDao.insert(pet)
            pet.weightKg?.let { weightDao.insert(WeightEntry(petId = id, date = LocalDate.now().toEpochDay(), weightKg = it)) }
            id
        } else {
            val old = petDao.get(pet.id)
            petDao.update(pet)
            // Si cambió el peso desde el formulario, queda registrado en el historial
            if (pet.weightKg != null && pet.weightKg != old?.weightKg) {
                weightDao.insert(WeightEntry(petId = pet.id, date = LocalDate.now().toEpochDay(), weightKg = pet.weightKg))
            }
            // Si se cambió o quitó la foto, se borra el archivo anterior
            if (old?.photoPath != null && old.photoPath != pet.photoPath) File(old.photoPath).delete()
            pet.id
        }
    }

    suspend fun deletePet(pet: Pet) {
        vaccineDao.deleteForPet(pet.id)
        weightDao.deleteForPet(pet.id)
        petDao.delete(pet)
        pet.photoPath?.let { File(it).delete() }
    }

    fun vaccinesForPet(petId: Long): Flow<List<Vaccine>> = vaccineDao.observeByPet(petId)
    suspend fun vaccinesForPetOnce(petId: Long) = vaccineDao.getByPet(petId)
    fun vaccinesForOwner(ownerId: Long): Flow<List<VaccineWithPet>> = vaccineDao.observeByOwner(ownerId)
    suspend fun vaccinesForOwnerOnce(ownerId: Long) = vaccineDao.getByOwner(ownerId)
    suspend fun getVaccine(id: Long): Vaccine? = vaccineDao.get(id)
    suspend fun saveVaccine(vaccine: Vaccine) { vaccineDao.upsert(vaccine) }
    suspend fun deleteVaccine(vaccine: Vaccine) = vaccineDao.delete(vaccine)

    fun weights(petId: Long): Flow<List<WeightEntry>> = weightDao.observeByPet(petId)

    /** Agrega una medición y actualiza el peso actual de la mascota con la más reciente. */
    suspend fun addWeight(petId: Long, date: Long, kg: Double) {
        weightDao.insert(WeightEntry(petId = petId, date = date, weightKg = kg))
        petDao.updateWeight(petId, weightDao.latest(petId)?.weightKg)
    }

    suspend fun deleteWeight(entry: WeightEntry) {
        weightDao.delete(entry)
        petDao.updateWeight(entry.petId, weightDao.latest(entry.petId)?.weightKg)
    }

    suspend fun deleteAll(ownerId: Long) {
        petDao.getByOwner(ownerId).forEach { p -> p.photoPath?.let { File(it).delete() } }
        vaccineDao.deleteAllForOwner(ownerId)
        weightDao.deleteAllForOwner(ownerId)
        petDao.deleteAllForOwner(ownerId)
    }

    /** Crea mascotas, vacunas y pesos de ejemplo para mostrar la app en la exposición. */
    suspend fun seedDemo(ownerId: Long) {
        val today = LocalDate.now()
        fun d(daysFromToday: Long) = today.plusDays(daysFromToday).toEpochDay()

        val luna = petDao.insert(Pet(ownerId = ownerId, name = "Luna", species = "Perro", breed = "Golden Retriever",
            sex = "Hembra", birthDate = today.minusYears(3).minusMonths(2).toEpochDay(), weightKg = 28.5,
            notes = "Alérgica al pollo. Le encanta nadar.", colorIndex = 0))
        val milo = petDao.insert(Pet(ownerId = ownerId, name = "Milo", species = "Gato", breed = "Siamés",
            sex = "Macho", birthDate = today.minusYears(1).minusMonths(5).toEpochDay(), weightKg = 4.6,
            notes = "Esterilizado.", colorIndex = 3))
        val kiwi = petDao.insert(Pet(ownerId = ownerId, name = "Kiwi", species = "Ave", breed = "Periquito",
            sex = "Macho", birthDate = today.minusMonths(8).toEpochDay(), weightKg = 0.04, colorIndex = 6))

        listOf(
            Vaccine(petId = luna, name = "Rabia", appliedDate = d(-350), nextDoseDate = d(15), veterinarian = "Dra. Gómez"),
            Vaccine(petId = luna, name = "Parvovirus", appliedDate = d(-200), nextDoseDate = d(165), veterinarian = "Dra. Gómez"),
            Vaccine(petId = luna, name = "Moquillo", appliedDate = d(-200), nextDoseDate = d(165), veterinarian = "Dra. Gómez"),
            Vaccine(petId = luna, name = "Desparasitación", appliedDate = d(-100), nextDoseDate = d(-10), veterinarian = "Clínica Vet UPB"),
            Vaccine(petId = milo, name = "Triple felina", appliedDate = d(-60), nextDoseDate = d(300), veterinarian = "Dr. Restrepo"),
            Vaccine(petId = milo, name = "Leucemia felina", appliedDate = d(-30), nextDoseDate = d(7), veterinarian = "Dr. Restrepo"),
            Vaccine(petId = kiwi, name = "Desparasitación", appliedDate = d(-20), nextDoseDate = d(70), notes = "Dosis oral"),
        ).forEach { vaccineDao.upsert(it) }

        // Historial de peso para la gráfica
        listOf(24.0, 25.2, 26.1, 27.0, 27.8, 28.5).forEachIndexed { i, kg ->
            weightDao.insert(WeightEntry(petId = luna, date = d(-150L + i * 30), weightKg = kg))
        }
        listOf(3.2, 3.8, 4.1, 4.4, 4.6).forEachIndexed { i, kg ->
            weightDao.insert(WeightEntry(petId = milo, date = d(-120L + i * 30), weightKg = kg))
        }
        weightDao.insert(WeightEntry(petId = kiwi, date = d(-20), weightKg = 0.04))
    }
}
