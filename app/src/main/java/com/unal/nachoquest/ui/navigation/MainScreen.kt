package com.unal.nachoquest.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.unal.nachoquest.ui.screens.challenges.ChallengesScreen
import com.unal.nachoquest.ui.screens.inventory.InventoryScreen
import com.unal.nachoquest.ui.screens.map.MapScreen
import com.unal.nachoquest.ui.screens.profile.ProfileScreen
import com.unal.nachoquest.ui.theme.NachoGreen

/**
 * Elemento de la barra de navegaciÃ³n inferior.
 */
data class BottomNavItem<T : Any>(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val route: T
)

/**
 * Pantalla principal con BottomNavigation y las 4 tabs:
 * Mapa, Retos, Inventario, Perfil.
 */
@Composable
fun MainScreen(
    onCerrarSesion: () -> Unit,
    onIniciarReto: (String) -> Unit = {}
) {
    val navController = rememberNavController()

    val items = listOf(
        BottomNavItem("Mapa", Icons.Filled.Map, Icons.Outlined.Map, MapRoute),
        BottomNavItem("Retos", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, ChallengesRoute),
        BottomNavItem("Inventario", Icons.Filled.Inventory2, Icons.Outlined.Inventory2, InventoryRoute),
        BottomNavItem("Perfil", Icons.Filled.Person, Icons.Outlined.Person, ProfileRoute),
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.ui.graphics.Color.White
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { item ->
                    val selected = currentDestination?.hierarchy?.any {
                        it.hasRoute(item.route::class)
                    } == true

                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = { Text(item.label) },
                        selected = selected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = NachoGreen,
                            selectedTextColor = NachoGreen,
                            indicatorColor = NachoGreen.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MapRoute,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<MapRoute> {
                MapScreen(onIniciarReto = onIniciarReto)
            }
            composable<ChallengesRoute> {
                ChallengesScreen()
            }
            composable<InventoryRoute> {
                InventoryScreen()
            }
            composable<ProfileRoute> {
                ProfileScreen(onCerrarSesion = onCerrarSesion)
            }
        }
    }
}
