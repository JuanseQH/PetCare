package com.upb.petcare.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.upb.petcare.data.SettingsRepository
import com.upb.petcare.ui.screens.CalendarScreen
import com.upb.petcare.ui.screens.CreditsScreen
import com.upb.petcare.ui.screens.HomeScreen
import com.upb.petcare.ui.screens.LoginScreen
import com.upb.petcare.ui.screens.OnboardingScreen
import com.upb.petcare.ui.screens.PetDetailScreen
import com.upb.petcare.ui.screens.PetFormScreen
import com.upb.petcare.ui.screens.PetListScreen
import com.upb.petcare.ui.screens.RegisterScreen
import com.upb.petcare.ui.screens.SettingsScreen
import com.upb.petcare.ui.screens.VaccineFormScreen
import com.upb.petcare.ui.theme.LocalAppExtras
import kotlinx.coroutines.flow.combine

object Routes {
    const val ONBOARDING = "onboarding"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val PETS = "pets"
    const val CALENDAR = "calendar"
    const val SETTINGS = "settings"
    const val CREDITS = "credits"
    const val PET_DETAIL = "pet/{petId}"
    const val PET_FORM = "petForm?petId={petId}"
    const val VACCINE_FORM = "vaccineForm/{petId}?vaccineId={vaccineId}"

    fun petDetail(id: Long) = "pet/$id"
    fun petForm(id: Long = 0) = "petForm?petId=$id"
    fun vaccineForm(petId: Long, vaccineId: Long = 0) = "vaccineForm/$petId?vaccineId=$vaccineId"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val TABS = listOf(
    Tab(Routes.HOME, "Inicio", Icons.Rounded.Home),
    Tab(Routes.PETS, "Mascotas", Icons.Rounded.Pets),
    Tab(Routes.CALENDAR, "Calendario", Icons.Rounded.CalendarMonth),
    Tab(Routes.SETTINGS, "Ajustes", Icons.Rounded.Settings),
)

private fun NavHostController.resetTo(route: String) = navigate(route) { popUpTo(graph.id) { inclusive = true } }

/** Cambia de pestaña conservando el estado de cada una. */
private fun NavHostController.switchTab(route: String) = navigate(route) {
    popUpTo(Routes.HOME) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

private data class StartState(val session: Long, val onboarding: Boolean)

@Composable
fun PetCareNavHost(settings: SettingsRepository, dark: Boolean) {
    val startFlow = remember { combine(settings.sessionUserId, settings.onboardingDone) { s, o -> StartState(s, o) } }
    val state by startFlow.collectAsStateWithLifecycle(initialValue = null)
    val loaded = state
    if (loaded == null) {
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    // El destino inicial se decide una sola vez, al abrir la app
    val start = remember {
        when {
            !loaded.onboarding -> Routes.ONBOARDING
            loaded.session == 0L -> Routes.LOGIN
            else -> Routes.HOME
        }
    }
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val current = backStack?.destination?.route
    val longArg = { name: String -> navArgument(name) { type = NavType.LongType; defaultValue = 0L } }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        NavHost(
            navController = nav,
            startDestination = start,
            enterTransition = { fadeIn(tween(220)) + slideInHorizontally(tween(260)) { it / 12 } },
            exitTransition = { fadeOut(tween(180)) },
            popEnterTransition = { fadeIn(tween(220)) },
            popExitTransition = { fadeOut(tween(180)) + slideOutHorizontally(tween(260)) { it / 12 } },
        ) {
            composable(Routes.ONBOARDING) {
                OnboardingScreen(onFinish = { nav.resetTo(if (loaded.session != 0L) Routes.HOME else Routes.LOGIN) })
            }
            composable(Routes.LOGIN) {
                LoginScreen(onLoggedIn = { nav.resetTo(Routes.HOME) }, onRegister = { nav.navigate(Routes.REGISTER) })
            }
            composable(Routes.REGISTER) {
                RegisterScreen(onRegistered = { nav.resetTo(Routes.HOME) }, onBack = { nav.popBackStack() })
            }
            composable(Routes.HOME) {
                HomeScreen(
                    onPet = { nav.navigate(Routes.petDetail(it)) },
                    onAddPet = { nav.navigate(Routes.petForm()) },
                    onAllPets = { nav.switchTab(Routes.PETS) },
                    onCalendar = { nav.switchTab(Routes.CALENDAR) },
                    onSettings = { nav.switchTab(Routes.SETTINGS) },
                    onCredits = { nav.navigate(Routes.CREDITS) },
                )
            }
            composable(Routes.PETS) {
                PetListScreen(onPet = { nav.navigate(Routes.petDetail(it)) }, onAdd = { nav.navigate(Routes.petForm()) })
            }
            composable(Routes.CALENDAR) {
                CalendarScreen(onPet = { nav.navigate(Routes.petDetail(it)) })
            }
            composable(Routes.SETTINGS) {
                SettingsScreen(onCredits = { nav.navigate(Routes.CREDITS) }, onLoggedOut = { nav.resetTo(Routes.LOGIN) })
            }
            composable(Routes.CREDITS) {
                CreditsScreen(onBack = { nav.popBackStack() })
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
        }

        // Barra de navegación flotante, solo en las 4 pestañas principales
        AnimatedVisibility(
            visible = TABS.any { it.route == current },
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            FloatingTabBar(current, dark) { nav.switchTab(it) }
        }
    }
}

@Composable
private fun FloatingTabBar(current: String?, dark: Boolean, onSelect: (String) -> Unit) {
    val shape = RoundedCornerShape(32.dp)
    Row(
        Modifier
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .fillMaxWidth()
            .shadow(if (LocalAppExtras.current.dark) 0.dp else 16.dp, shape, spotColor = Color(0x55584A3A))
            .clip(shape)
            .background(if (dark) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TABS.forEach { tab ->
            val selected = tab.route == current
            Row(
                Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSelect(tab.route) }
                    .animateContentSize(tween(250))
                    .padding(horizontal = if (selected) 16.dp else 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    tab.icon, contentDescription = tab.label, modifier = Modifier.size(24.dp),
                    tint = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (selected) {
                    Spacer(Modifier.width(8.dp))
                    Text(tab.label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
        }
    }
}

/** Espacio inferior que deben dejar las pestañas para que la barra flotante no tape contenido. */
val BottomBarSpace = 112.dp

