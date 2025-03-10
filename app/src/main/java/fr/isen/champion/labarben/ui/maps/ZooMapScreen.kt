package fr.isen.champion.labarben.ui.maps

import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.isen.champion.labarben.R

@Composable
fun ZooMapScreen(navController: NavController) {
    // États pour gérer les interactions
    var clickedArea by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Afficher l'image de la carte
        Image(
            painter = painterResource(id = R.drawable.maps),
            contentDescription = "Carte du zoo",
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        // Détecter le clic et vérifier les zones
                        clickedArea = getClickedArea(offset)
                    }
                }
        )

        // Afficher un message si une zone est cliquée
        clickedArea?.let { area ->
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .background(Color.White, CircleShape)
                    .padding(16.dp)
            ) {
                Text(
                    text = "Vous avez cliqué sur : $area",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// Fonction pour déterminer la zone cliquée
private fun getClickedArea(offset: Offset): String? {
    // Définir les zones cliquables (coordonnées x, y, largeur, hauteur)
    val clickableAreas = mapOf(
        Rect(100, 200, 300, 400) to "Enclos des Lions", // Exemple de zone
        Rect(400, 500, 600, 700) to "Restaurant"       // Exemple de zone
    )

    // Vérifier si le clic est dans une zone cliquable
    for ((rect, areaName) in clickableAreas) {
        if (rect.contains(offset.x.toInt(), offset.y.toInt())) {
            return areaName
        }
    }
    return null
}