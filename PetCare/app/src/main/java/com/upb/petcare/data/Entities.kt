package com.upb.petcare.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Usuario registrado en el dispositivo. La contraseña se guarda como hash SHA-256. */
@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val email: String,
    val passwordHash: String,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Mascota que pertenece a un usuario. Las fechas se guardan como "epochDay" (días desde 1970). */
@Entity(
    tableName = "pets",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["ownerId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("ownerId")],
)
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val species: String,
    val breed: String = "",
    val sex: String = "",
    val birthDate: Long? = null,
    val weightKg: Double? = null,
    val notes: String = "",
)

/** Registro de una vacuna (o desparasitación) aplicada a una mascota. */
@Entity(
    tableName = "vaccines",
    foreignKeys = [ForeignKey(
        entity = Pet::class,
        parentColumns = ["id"],
        childColumns = ["petId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("petId")],
)
data class Vaccine(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val name: String,
    val appliedDate: Long,
    val nextDoseDate: Long? = null,
    val veterinarian: String = "",
    val notes: String = "",
)

/** Vacuna junto con el nombre y especie de su mascota (para el menú principal). */
data class VaccineWithPet(
    @Embedded val vaccine: Vaccine,
    val petName: String,
    val petSpecies: String,
)
