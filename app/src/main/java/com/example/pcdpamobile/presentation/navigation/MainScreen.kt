package com.example.pcdpamobile.presentation.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pcdpamobile.presentation.home.InicioScreen
import com.example.pcdpamobile.presentation.jugada.RegistroJugadaScreen
import com.example.pcdpamobile.presentation.sorteo.SeguimientoSorteoScreen

@Composable
fun MainScreen(rootNavController: NavController) {
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
                    selected = currentRoute == "inicio",
                    onClick = {
                        if (currentRoute != "inicio") {
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
                    icon = { Icon(Icons.Default.List, contentDescription = "Mi Jugada") },
                    label = { Text("Mi Jugada") },
                    selected = currentRoute == "mi_jugada",
                    onClick = {
                        if (currentRoute != "mi_jugada") {
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
                    icon = { Icon(Icons.Default.PlayArrow, contentDescription = "Sorteo en Vivo") },
                    label = { Text("Sorteo en Vivo") },
                    selected = currentRoute == "sorteo_vivo",
                    onClick = {
                        if (currentRoute != "sorteo_vivo") {
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
            composable("inicio") { InicioScreen(rootNavController) }
            composable("mi_jugada") { RegistroJugadaScreen(rootNavController) }
            composable("sorteo_vivo") { SeguimientoSorteoScreen(rootNavController) }
        }
    }
}
