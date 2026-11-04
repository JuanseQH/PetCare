package com.upb.petcare.data

import androidx.room.ColumnInfo
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
    /** Ruta de la foto guardada en el almacenamiento interno de la app (opcional). */
    val photoPath: String? = null,
    /** Índice del color que identifica a la mascota en la paleta de la app. */
    @ColumnInfo(defaultValue = "0") val colorIndex: Int = 0,
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

/** Medición de peso de una mascota (para la gráfica de evolución). */
@Entity(
    tableName = "weights",
    foreignKeys = [ForeignKey(
        entity = Pet::class,
        parentColumns = ["id"],
        childColumns = ["petId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("petId")],
)
data class WeightEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val date: Long,
    val weightKg: Double,
)

/** Vacuna junto con los datos básicos de su mascota (para el inicio y el calendario). */
data class VaccineWithPet(
    @Embedded val vaccine: Vaccine,
    val petName: String,
    val petSpecies: String,
    val petPhoto: String?,
    val petColor: Int,
)
