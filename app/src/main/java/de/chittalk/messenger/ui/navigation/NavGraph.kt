package de.chittalk.messenger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import de.chittalk.messenger.ui.screens.auth.LoginScreen
import de.chittalk.messenger.ui.screens.auth.RegisterScreen
import de.chittalk.messenger.ui.screens.chat.ChatScreen
import de.chittalk.messenger.ui.screens.profile.ProfileScreen
import de.chittalk.messenger.ui.screens.stream.StreamScreen

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Chat : Screen("chat")
    object Stream : Screen("stream")
    object Profile : Screen("profile")
}

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigateUp()
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Chat.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Chat.route) {
            ChatScreen()
        }

        composable(Screen.Stream.route) {
            StreamScreen(
                onStartStream = {
                    // Handle stream start
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateToSettings = {
                    // Navigate to settings
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}