package com.upb.petcare.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun findByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE id = :id")
    fun observe(id: Long): Flow<User?>

    @Query("UPDATE users SET name = :name WHERE id = :id")
    suspend fun updateName(id: Long, name: String)
}

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE ownerId = :ownerId ORDER BY name COLLATE NOCASE")
    fun observeByOwner(ownerId: Long): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE ownerId = :ownerId")
    suspend fun getByOwner(ownerId: Long): List<Pet>

    @Query("SELECT * FROM pets WHERE id = :id")
    fun observe(id: Long): Flow<Pet?>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun get(id: Long): Pet?

    @Insert
    suspend fun insert(pet: Pet): Long

    @Update
    suspend fun update(pet: Pet)

    @Delete
    suspend fun delete(pet: Pet)

    @Query("UPDATE pets SET weightKg = :kg WHERE id = :petId")
    suspend fun updateWeight(petId: Long, kg: Double?)

    @Query("DELETE FROM pets WHERE ownerId = :ownerId")
    suspend fun deleteAllForOwner(ownerId: Long)
}

@Dao
interface VaccineDao {
    @Query("SELECT * FROM vaccines WHERE petId = :petId ORDER BY appliedDate DESC")
    fun observeByPet(petId: Long): Flow<List<Vaccine>>

    @Query("SELECT * FROM vaccines WHERE petId = :petId ORDER BY appliedDate DESC")
    suspend fun getByPet(petId: Long): List<Vaccine>

    @Query("SELECT * FROM vaccines WHERE id = :id")
    suspend fun get(id: Long): Vaccine?

    @Query(
        """
        SELECT v.*, p.name AS petName, p.species AS petSpecies, p.photoPath AS petPhoto, p.colorIndex AS petColor
        FROM vaccines v INNER JOIN pets p ON p.id = v.petId
        WHERE p.ownerId = :ownerId
        ORDER BY v.appliedDate DESC
        """
    )
    fun observeByOwner(ownerId: Long): Flow<List<VaccineWithPet>>

    @Query(
        """
        SELECT v.*, p.name AS petName, p.species AS petSpecies, p.photoPath AS petPhoto, p.colorIndex AS petColor
        FROM vaccines v INNER JOIN pets p ON p.id = v.petId
        WHERE p.ownerId = :ownerId
        """
    )
    suspend fun getByOwner(ownerId: Long): List<VaccineWithPet>

    @Upsert
    suspend fun upsert(vaccine: Vaccine): Long

    @Delete
    suspend fun delete(vaccine: Vaccine)

    @Query("DELETE FROM vaccines WHERE petId = :petId")
    suspend fun deleteForPet(petId: Long)

    @Query("DELETE FROM vaccines WHERE petId IN (SELECT id FROM pets WHERE ownerId = :ownerId)")
    suspend fun deleteAllForOwner(ownerId: Long)
}

@Dao
interface WeightDao {
    @Query("SELECT * FROM weights WHERE petId = :petId ORDER BY date ASC, id ASC")
    fun observeByPet(petId: Long): Flow<List<WeightEntry>>

    @Query("SELECT * FROM weights WHERE petId = :petId ORDER BY date DESC, id DESC LIMIT 1")
    suspend fun latest(petId: Long): WeightEntry?

    @Insert
    suspend fun insert(entry: WeightEntry): Long

    @Delete
    suspend fun delete(entry: WeightEntry)

    @Query("DELETE FROM weights WHERE petId = :petId")
    suspend fun deleteForPet(petId: Long)

    @Query("DELETE FROM weights WHERE petId IN (SELECT id FROM pets WHERE ownerId = :ownerId)")
    suspend fun deleteAllForOwner(ownerId: Long)
}
