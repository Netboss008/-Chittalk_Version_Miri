package com.example.chittalk.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

sealed class Screen(val route: String, val title: String) {
    object Auth : Screen("auth", "Auth")
    object Chats : Screen("chats", "Chats")
    object Status : Screen("status", "Status")
    object Calls : Screen("calls", "Anrufe")
    object Community : Screen("community", "Community")
}

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(navController)
        }
        composable(Screen.Chats.route) {
            ChatsScreen(navController)
        }
        composable(Screen.Status.route) {
            StatusScreen(navController)
        }
        composable(Screen.Calls.route) {
            CallsScreen(navController)
        }
        composable(Screen.Community.route) {
            CommunityScreen(navController)
        }
    }
}

@Composable
fun BottomNavigation(navController: NavController) {
    val items = listOf(
        Screen.Community,
        Screen.Chats,
        Screen.Status,
        Screen.Calls
    )
    
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = when (screen) {
                            Screen.Community -> Icons.Filled.Groups
                            Screen.Chats -> Icons.Filled.Chat
                            Screen.Status -> Icons.Filled.Update
                            Screen.Calls -> Icons.Filled.Call
                            else -> Icons.Filled.Chat
                        },
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}