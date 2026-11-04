package com.upb.petcare.data

import android.database.sqlite.SQLiteConstraintException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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

class PetRepository(private val petDao: PetDao, private val vaccineDao: VaccineDao) {

    fun pets(ownerId: Long): Flow<List<Pet>> = petDao.observeByOwner(ownerId)
    fun pet(id: Long): Flow<Pet?> = petDao.observe(id)
    suspend fun getPet(id: Long): Pet? = petDao.get(id)

    suspend fun savePet(pet: Pet): Long =
        if (pet.id == 0L) petDao.insert(pet) else pet.id.also { petDao.update(pet) }

    suspend fun deletePet(pet: Pet) {
        vaccineDao.deleteForPet(pet.id)
        petDao.delete(pet)
    }

    fun vaccinesForPet(petId: Long): Flow<List<Vaccine>> = vaccineDao.observeByPet(petId)
    fun vaccinesForOwner(ownerId: Long): Flow<List<VaccineWithPet>> = vaccineDao.observeByOwner(ownerId)
    suspend fun getVaccine(id: Long): Vaccine? = vaccineDao.get(id)
    suspend fun saveVaccine(vaccine: Vaccine) { vaccineDao.upsert(vaccine) }
    suspend fun deleteVaccine(vaccine: Vaccine) = vaccineDao.delete(vaccine)

    suspend fun deleteAll(ownerId: Long) {
        vaccineDao.deleteAllForOwner(ownerId)
        petDao.deleteAllForOwner(ownerId)
    }

    /** Crea mascotas y vacunas de ejemplo para mostrar la app en la exposición. */
    suspend fun seedDemo(ownerId: Long) {
        val today = LocalDate.now()
        fun d(daysFromToday: Long) = today.plusDays(daysFromToday).toEpochDay()

        val luna = petDao.insert(Pet(ownerId = ownerId, name = "Luna", species = "Perro", breed = "Golden Retriever",
            sex = "Hembra", birthDate = today.minusYears(3).minusMonths(2).toEpochDay(), weightKg = 28.5,
            notes = "Alérgica al pollo."))
        val milo = petDao.insert(Pet(ownerId = ownerId, name = "Milo", species = "Gato", breed = "Siamés",
            sex = "Macho", birthDate = today.minusYears(1).minusMonths(5).toEpochDay(), weightKg = 4.2))
        val kiwi = petDao.insert(Pet(ownerId = ownerId, name = "Kiwi", species = "Ave", breed = "Periquito",
            sex = "Macho", birthDate = today.minusMonths(8).toEpochDay(), weightKg = 0.04))

        listOf(
            Vaccine(petId = luna, name = "Rabia", appliedDate = d(-350), nextDoseDate = d(15), veterinarian = "Dra. Gómez"),
            Vaccine(petId = luna, name = "Parvovirus", appliedDate = d(-200), nextDoseDate = d(165), veterinarian = "Dra. Gómez"),
            Vaccine(petId = luna, name = "Desparasitación", appliedDate = d(-100), nextDoseDate = d(-10), veterinarian = "Clínica Vet UPB"),
            Vaccine(petId = milo, name = "Triple felina", appliedDate = d(-60), nextDoseDate = d(300), veterinarian = "Dr. Restrepo"),
            Vaccine(petId = milo, name = "Leucemia felina", appliedDate = d(-30), nextDoseDate = d(7), veterinarian = "Dr. Restrepo"),
            Vaccine(petId = kiwi, name = "Desparasitación", appliedDate = d(-20), notes = "Dosis única"),
        ).forEach { vaccineDao.upsert(it) }
    }
}
