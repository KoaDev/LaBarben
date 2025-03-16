package fr.isen.champion.labarben.ui.maps

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import fr.isen.champion.labarben.R

@Composable
fun ZooMapScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Afficher l'image de la carte
        Image(
            painter = painterResource(id = R.drawable.maps),
            contentDescription = "Carte du zoo",
            modifier = Modifier
                .fillMaxSize()
        )
    }
}
