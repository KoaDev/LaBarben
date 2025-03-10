package fr.isen.champion.labarben

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import fr.isen.champion.labarben.road.Screen
import fr.isen.champion.labarben.ui.user.LoginScreen
import fr.isen.champion.labarben.ui.user.RegisterScreen
import fr.isen.champion.labarben.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val rootNavController = rememberNavController()
                val firebaseAuth = FirebaseAuth.getInstance()
                val startDestination = if (firebaseAuth.currentUser != null) {
                    Screen.Home.route
                } else {
                    Screen.Login.route
                }
                NavHost(
                    navController = rootNavController,
                    startDestination = startDestination
                ) {
                    composable(Screen.Login.route) { LoginScreen(rootNavController) }
                    composable(Screen.Register.route) { RegisterScreen(rootNavController) }
                    composable(Screen.Home.route) { MainScreen(rootNavController) }
                }
            }
        }
    }
}
