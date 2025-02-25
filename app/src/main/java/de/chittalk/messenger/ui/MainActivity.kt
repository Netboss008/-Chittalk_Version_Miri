package de.chittalk.messenger.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import de.chittalk.messenger.ui.theme.ChitTalkTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChitTalkTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChitTalkApp()
                }
            }
        }
    }
}

@Composable
fun ChitTalkApp() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == "home" } == true,
                    onClick = { navController.navigate("home") },
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_home)) }
                )
                
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == "chat" } == true,
                    onClick = { navController.navigate("chat") },
                    icon = { Icon(Icons.Filled.Chat, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_chat)) }
                )
                
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == "stream" } == true,
                    onClick = { navController.navigate("stream") },
                    icon = { Icon(Icons.Filled.Videocam, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_stream)) }
                )
                
                NavigationBarItem(
                    selected = currentDestination?.hierarchy?.any { it.route == "profile" } == true,
                    onClick = { navController.navigate("profile") },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    label = { Text(stringResource(R.string.nav_profile)) }
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("home") { /* HomeScreen() */ }
            composable("chat") { /* ChatScreen() */ }
            composable("stream") { /* StreamScreen() */ }
            composable("profile") { /* ProfileScreen() */ }
        }
    }
}