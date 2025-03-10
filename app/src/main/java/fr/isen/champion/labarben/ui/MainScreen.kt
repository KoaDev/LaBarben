package fr.isen.champion.labarben.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import fr.isen.champion.labarben.navigation.BottomNavigationBar
import fr.isen.champion.labarben.navigation.NavigationBarItem
import fr.isen.champion.labarben.ui.enclosure.FirebaseZooListScreen
import fr.isen.champion.labarben.ui.user.ProfileScreen

@Composable
fun MainScreen(rootNavController: NavHostController) {
    val innerNavController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(innerNavController) }
    ) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        ) {
            NavHost(
                navController = innerNavController,
                startDestination = NavigationBarItem.Home.route
            ) {
                composable(NavigationBarItem.Home.route) {
                    HomeScreen(rootNavController)
                }
                composable(NavigationBarItem.Enclosure.route) {
                    FirebaseZooListScreen()
                }
                composable(NavigationBarItem.Profile.route) {
                    ProfileScreen(rootNavController)
                }
            }
        }
    }
}
