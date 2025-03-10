package fr.isen.champion.labarben.navigation

import fr.isen.champion.labarben.R

sealed class NavigationBarItem(val route: String, val labelResId: Int) {
    object Home : NavigationBarItem("home", R.string.navigation_bar_label_home)
    object Profile : NavigationBarItem("profile", R.string.navigation_bar_label_profile)
}
