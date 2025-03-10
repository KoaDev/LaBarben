package fr.isen.champion.labarben.ui.user

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import fr.isen.champion.labarben.road.Screen

@Composable
fun ProfileScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Affichage des infos utilisateur
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Profil Utilisateur",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(24.dp))
                if (user != null) {
                    Text(
                        text = "Email : ${user.email}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    user.displayName?.let {
                        Text(
                            text = "Nom : $it",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    // D'autres informations peuvent être ajoutées ici
                } else {
                    Text(
                        text = "Aucun utilisateur connecté.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            // Bouton de déconnexion en bas
            Button(
                onClick = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Déconnexion")
            }
        }
    }
}
