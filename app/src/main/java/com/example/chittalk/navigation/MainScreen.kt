package com.example.chittalk.presentation.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.chittalk.navigation.BottomNavigation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    Scaffold(
        bottomBar = { BottomNavigation(navController) }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            Navigation()
        }
    }
}