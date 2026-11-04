package com.upb.petcare.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.upb.petcare.data.SettingsRepository
import com.upb.petcare.ui.screens.CreditsScreen
import com.upb.petcare.ui.screens.HomeScreen
import com.upb.petcare.ui.screens.LoginScreen
import com.upb.petcare.ui.screens.PetDetailScreen
import com.upb.petcare.ui.screens.PetFormScreen
import com.upb.petcare.ui.screens.PetListScreen
import com.upb.petcare.ui.screens.RegisterScreen
import com.upb.petcare.ui.screens.SettingsScreen
import com.upb.petcare.ui.screens.VaccineFormScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PETS = "pets"
    const val PET_DETAIL = "pet/{petId}"
    const val PET_FORM = "petForm?petId={petId}"
    const val VACCINE_FORM = "vaccineForm/{petId}?vaccineId={vaccineId}"
    const val SETTINGS = "settings"
    const val CREDITS = "credits"

    fun petDetail(id: Long) = "pet/$id"
    fun petForm(id: Long = 0) = "petForm?petId=$id"
    fun vaccineForm(petId: Long, vaccineId: Long = 0) = "vaccineForm/$petId?vaccineId=$vaccineId"
}

/** Cierra sesión y vuelve al login borrando la pila de navegación. */
private fun NavHostController.goToLogin() = navigate(Routes.LOGIN) { popUpTo(graph.id) { inclusive = true } }
private fun NavHostController.goHome() = navigate(Routes.HOME) { popUpTo(graph.id) { inclusive = true } }

@Composable
fun PetCareNavHost(settings: SettingsRepository) {
    // -1 = cargando la sesión guardada
    val session by settings.sessionUserId.collectAsStateWithLifecycle(initialValue = -1L)
    if (session == -1L) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    // El destino inicial se decide una sola vez, al abrir la app
    val start = remember { if (session == 0L) Routes.LOGIN else Routes.HOME }
    val nav = rememberNavController()
    val longArg = { name: String -> navArgument(name) { type = NavType.LongType; defaultValue = 0L } }

    NavHost(navController = nav, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(onLoggedIn = { nav.goHome() }, onRegister = { nav.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onRegistered = { nav.goHome() }, onBack = { nav.popBackStack() })
        }
        composable(Routes.HOME) {
            HomeScreen(
                onPets = { nav.navigate(Routes.PETS) },
                onAddPet = { nav.navigate(Routes.petForm()) },
                onPet = { nav.navigate(Routes.petDetail(it)) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onCredits = { nav.navigate(Routes.CREDITS) },
                onLoggedOut = { nav.goToLogin() },
            )
        }
        composable(Routes.PETS) {
            PetListScreen(
                onBack = { nav.popBackStack() },
                onPet = { nav.navigate(Routes.petDetail(it)) },
                onAdd = { nav.navigate(Routes.petForm()) },
            )
        }
        composable(Routes.PET_DETAIL, arguments = listOf(longArg("petId"))) { entry ->
            val petId = entry.arguments?.getLong("petId") ?: 0L
            PetDetailScreen(
                petId = petId,
                onBack = { nav.popBackStack() },
                onEdit = { nav.navigate(Routes.petForm(petId)) },
                onAddVaccine = { nav.navigate(Routes.vaccineForm(petId)) },
                onEditVaccine = { nav.navigate(Routes.vaccineForm(petId, it)) },
            )
        }
        composable(Routes.PET_FORM, arguments = listOf(longArg("petId"))) { entry ->
            val petId = entry.arguments?.getLong("petId") ?: 0L
            PetFormScreen(
                petId = petId,
                onBack = { nav.popBackStack() },
                onSaved = { savedId ->
                    nav.popBackStack()
                    // Al crear una mascota nueva, abrir directamente su detalle
                    if (petId == 0L) nav.navigate(Routes.petDetail(savedId))
                },
            )
        }
        composable(Routes.VACCINE_FORM, arguments = listOf(longArg("petId"), longArg("vaccineId"))) { entry ->
            VaccineFormScreen(
                petId = entry.arguments?.getLong("petId") ?: 0L,
                vaccineId = entry.arguments?.getLong("vaccineId") ?: 0L,
                onBack = { nav.popBackStack() },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { nav.popBackStack() }, onLoggedOut = { nav.goToLogin() })
        }
        composable(Routes.CREDITS) {
            CreditsScreen(onBack = { nav.popBackStack() })
        }
    }
}
