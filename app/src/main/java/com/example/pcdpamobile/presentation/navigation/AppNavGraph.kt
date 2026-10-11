package com.example.pcdpamobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pcdpamobile.presentation.auth.LoginScreen
import com.example.pcdpamobile.presentation.auth.RegisterScreen
import com.example.pcdpamobile.presentation.jugada.RegistroJugadaScreen


@Composable
fun AppNavGraph () {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("registro_jugada") { RegistroJugadaScreen(navController) }
    }
}
