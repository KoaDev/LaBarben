package fr.isen.champion.labarben.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(
        NavigationBarItem.Home,
        NavigationBarItem.Enclosure,
        NavigationBarItem.Service,
        NavigationBarItem.Profile
    )

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        items.forEach { item ->
            val icon = when (item) {
                NavigationBarItem.Home -> Icons.Default.Home
                NavigationBarItem.Enclosure -> Icons.Default.Place
                NavigationBarItem.Service -> Icons.Default.Build
                NavigationBarItem.Profile -> Icons.Default.Person
            }
            val labelText = stringResource(id = item.labelResId)

            NavigationBarItem(
                icon = { Icon(imageVector = icon, contentDescription = labelText) },
                label = { Text(labelText) },
                selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
