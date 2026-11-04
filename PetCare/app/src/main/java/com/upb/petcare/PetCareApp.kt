package com.upb.petcare

import android.app.Application
import android.content.Context
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.upb.petcare.data.AppDatabase
import com.upb.petcare.data.AuthRepository
import com.upb.petcare.data.PetRepository
import com.upb.petcare.data.SettingsRepository
import com.upb.petcare.util.Reminders

class PetCareApp : Application(), ImageLoaderFactory {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        Reminders.createChannel(this)
    }

    /** Cargador de imágenes para las fotos de las mascotas (Coil), con animación suave. */
    override fun newImageLoader(): ImageLoader = ImageLoader.Builder(this).crossfade(true).build()
}

/** Contenedor manual de dependencias (inyección de dependencias sencilla). */
class AppContainer(context: Context) {
    val appContext: Context = context.applicationContext
    private val db = AppDatabase.get(context)
    val settings = SettingsRepository(context)
    val auth = AuthRepository(db.userDao(), settings)
    val pets = PetRepository(db.petDao(), db.vaccineDao(), db.weightDao())
}
