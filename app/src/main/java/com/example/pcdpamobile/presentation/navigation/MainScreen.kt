package com.example.pcdpamobile.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pcdpamobile.presentation.home.InicioScreen
import com.example.pcdpamobile.presentation.jugada.RegistroHistoricoJugadasScreen
import com.example.pcdpamobile.presentation.jugada.RegistroJugadaScreen
import com.example.pcdpamobile.presentation.sorteo.SeguimientoSorteoScreen
import java.net.URLDecoder

@Composable
fun MainScreen() {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    selected = currentRoute?.startsWith("inicio") == true,
                    onClick = {
                        if (currentRoute?.startsWith("inicio") != true) {
                            bottomNavController.navigate("inicio") {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6A11CB),
                        selectedTextColor = Color(0xFF6A11CB),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF6A11CB).copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.Edit, contentDescription = "Mi Jugada") },
                    label = { Text("Mi Jugada") },
                    selected = currentRoute?.startsWith("mi_jugada") == true,
                    onClick = {
                        if (currentRoute?.startsWith("mi_jugada") != true) {
                            bottomNavController.navigate("mi_jugada") {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6A11CB),
                        selectedTextColor = Color(0xFF6A11CB),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF6A11CB).copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.History, contentDescription = "Historial") },
                    label = { Text("Historial") },
                    selected = currentRoute?.startsWith("historico_jugadas") == true,
                    onClick = {
                        if (currentRoute?.startsWith("historico_jugadas") != true) {
                            bottomNavController.navigate("historico_jugadas") {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6A11CB),
                        selectedTextColor = Color(0xFF6A11CB),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF6A11CB).copy(alpha = 0.15f)
                    )
                )

                NavigationBarItem(
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Sorteo en Vivo") },
                    label = { Text("Sorteo en Vivo") },
                    selected = currentRoute?.startsWith("sorteo_vivo") == true,
                    onClick = {
                        if (currentRoute?.startsWith("sorteo_vivo") != true) {
                            bottomNavController.navigate("sorteo_vivo") {
                                popUpTo(bottomNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6A11CB),
                        selectedTextColor = Color(0xFF6A11CB),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray,
                        indicatorColor = Color(0xFF6A11CB).copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "inicio",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable("inicio") { InicioScreen(bottomNavController) }
            composable(
                route = "mi_jugada?sorteoName={sorteoName}",
                arguments = listOf(
                    navArgument("sorteoName") {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = "Registro de Jugada"
                    }
                )
            ) { backStackEntry ->
                val rawName = backStackEntry.arguments?.getString("sorteoName") ?: "Registro de Jugada"
                val decodedName = try {
                    URLDecoder.decode(rawName, "UTF-8")
                } catch (_: Exception) {
                    rawName
                }
                RegistroJugadaScreen(bottomNavController, decodedName)
            }
            composable("historico_jugadas") { RegistroHistoricoJugadasScreen(bottomNavController) }
            composable("sorteo_vivo") { SeguimientoSorteoScreen(bottomNavController) }
        }
    }
}
