package fr.isen.champion.labarben.navigation

import fr.isen.champion.labarben.R

sealed class NavigationBarItem(val route: String, val labelResId: Int) {
    object Home : NavigationBarItem("home", R.string.navigation_bar_label_home)
    object Profile : NavigationBarItem("profile", R.string.navigation_bar_label_profile)
    object Enclosure : NavigationBarItem("enclosure", R.string.navigation_bar_label_enclosure)
    object Service : NavigationBarItem("service", R.string.navigation_bar_label_service)
    object Map : NavigationBarItem("map", R.string.navigation_bar_label_map)
}
