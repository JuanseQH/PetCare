package com.upb.petcare

import android.app.Application
import android.content.Context
import com.upb.petcare.data.AppDatabase
import com.upb.petcare.data.AuthRepository
import com.upb.petcare.data.PetRepository
import com.upb.petcare.data.SettingsRepository

class PetCareApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/** Contenedor manual de dependencias (inyección de dependencias sencilla). */
class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)
    val settings = SettingsRepository(context)
    val auth = AuthRepository(db.userDao(), settings)
    val pets = PetRepository(db.petDao(), db.vaccineDao())
}
