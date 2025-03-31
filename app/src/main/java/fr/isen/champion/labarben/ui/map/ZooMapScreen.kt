package fr.isen.champion.labarben.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import fr.isen.champion.labarben.R

@Composable
fun ZooMapScreen(navController: NavController) {
    // États de transformation (zoom et translation)
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    // États pour stocker les coordonnées (dans le repère de l'image d'origine)
    var startPoint by remember { mutableStateOf<Offset?>(null) }
    var destination by remember { mutableStateOf<Offset?>(null) }

    // État pour la transformation via geste
    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    // Conteneur extérieur non transformé
    Box(modifier = Modifier.fillMaxSize()) {
        // Calque de la carte (transformé)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset ->
                        // Le tapOffset ici est dans le repère non transformé,
                        // on calcule la coordonnée relative à l'image d'origine
                        val imageCoordinate = (tapOffset - offset) / scale
                        when {
                            startPoint == null -> startPoint = imageCoordinate
                            destination == null -> destination = imageCoordinate
                            else -> {
                                startPoint = imageCoordinate
                                destination = null
                            }
                        }
                    }
                }
                .transformable(state = transformableState)
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offset.x,
                    translationY = offset.y
                )
        ) {
            Image(
                painter = painterResource(id = R.drawable.maps),
                contentDescription = "Carte du zoo",
                modifier = Modifier.fillMaxSize()
            )
        }

        // Calque d'overlay pour les marqueurs et la ligne (non transformé)
        // On applique manuellement la transformation aux coordonnées
        fun toScreen(coord: Offset): Offset = coord * scale + offset

        // Affichage du marqueur pour le point de départ
        startPoint?.let { imageCoord ->
            val screenCoord = toScreen(imageCoord)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        translationX = screenCoord.x - 16.dp.toPx() // centrer l'icône
                        translationY = screenCoord.y - 16.dp.toPx()
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Point de départ",
                    tint = Color.Green,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Affichage du marqueur pour la destination
        destination?.let { imageCoord ->
            val screenCoord = toScreen(imageCoord)
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer {
                        translationX = screenCoord.x - 16.dp.toPx()
                        translationY = screenCoord.y - 16.dp.toPx()
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = "Destination",
                    tint = Color.Red,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Dessiner la ligne (itinéraire) entre les deux points s'ils sont définis
        if (startPoint != null && destination != null) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val startScreen = toScreen(startPoint!!)
                val destScreen = toScreen(destination!!)
                drawLine(
                    color = Color.Blue,
                    start = startScreen,
                    end = destScreen,
                    strokeWidth = 5f
                )
            }
        }
    }
}
