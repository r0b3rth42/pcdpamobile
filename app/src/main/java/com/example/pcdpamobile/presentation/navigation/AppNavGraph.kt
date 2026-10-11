package com.example.pcdpamobile.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pcdpamobile.presentation.auth.LoginScreen
import com.example.pcdpamobile.presentation.auth.RegisterScreen


@Composable
fun AppNavGraph () {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("register") { RegisterScreen(navController) }
        composable("login") { LoginScreen(navController) }
    }
}