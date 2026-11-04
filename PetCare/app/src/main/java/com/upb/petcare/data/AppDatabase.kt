package com.upb.petcare.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [User::class, Pet::class, Vaccine::class, WeightEntry::class],
    version = 2,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun petDao(): PetDao
    abstract fun vaccineDao(): VaccineDao
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        /** Versión 2: foto y color de la mascota + historial de peso. Conserva los datos existentes. */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE pets ADD COLUMN photoPath TEXT")
                db.execSQL("ALTER TABLE pets ADD COLUMN colorIndex INTEGER NOT NULL DEFAULT 0")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS weights (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        petId INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        weightKg REAL NOT NULL,
                        FOREIGN KEY(petId) REFERENCES pets(id) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS index_weights_petId ON weights(petId)")
                // Cada mascota existente recibe un color distinto y su peso actual como primer registro
                db.execSQL("UPDATE pets SET colorIndex = id % 8")
                db.execSQL(
                    "INSERT INTO weights (petId, date, weightKg) " +
                        "SELECT id, CAST(julianday('now') - 2440587.5 AS INTEGER), weightKg FROM pets WHERE weightKg IS NOT NULL"
                )
            }
        }

        fun get(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, "petcare.db")
                .addMigrations(MIGRATION_1_2)
                .build()
                .also { instance = it }
        }
    }
}
