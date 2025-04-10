package fr.isen.champion.labarben.ui.maps

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import fr.isen.champion.labarben.R
import kotlinx.coroutines.*

@Composable
fun ZooMapScreen(navController: NavController) {
    val context = LocalContext.current
    var zooPoints by remember { mutableStateOf(emptyList<ZooPoint>()) }
    val imageWidth = 1080f
    val imageHeight = 1920f
    var currentPoints by remember { mutableStateOf(listOf<ZooPoint>()) }
    var linesToDraw by remember { mutableStateOf(listOf<Pair<ZooPoint, ZooPoint>>()) }
    val radius = 0.02f

    // State to show AlertDialog
    var showAlert by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        zooPoints = loadZooPoints(context) ?: emptyList()
    }

    // Show AlertDialog only once
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Maps") },
            text = { Text("Cliquez sur la route où vous êtes actuellement, puis sur l'endroit où vous souhaitez aller.") },
            confirmButton = {
                TextButton(onClick = { showAlert = false }) {
                    Text("Ok")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { tapOffset ->
                    Log.d("ZooMap", "Tap détecté : (${tapOffset.x}, ${tapOffset.y})")
                    val clickedPoint = getClickedPoint(tapOffset, zooPoints, imageWidth, imageHeight)

                    clickedPoint?.let {
                        currentPoints = currentPoints + it

                        if (currentPoints.size == 2) {
                            val startPoint = currentPoints[0]
                            val endPoint = currentPoints[1]

                            val pointsInRange = zooPoints.filter { point ->
                                val distanceToStart = calculateDistance(startPoint, point)
                                val distanceToEnd = calculateDistance(endPoint, point)
                                distanceToStart <= radius || distanceToEnd <= radius
                            }

                            CoroutineScope(Dispatchers.Default).launch {
                                val path = findPath(startPoint, endPoint, pointsInRange)

                                withContext(Dispatchers.Main) {
                                    linesToDraw = path
                                    currentPoints = emptyList()
                                }
                            }
                        }
                    } ?: run {
//                        val newPoint = ZooPoint(tapOffset.x / imageWidth, tapOffset.y / imageHeight)
//                        addPointToJson(context, newPoint)
//                        zooPoints = loadZooPoints(context) ?: emptyList()
                    }
                }
            }
    ) {
        Image(
            painter = painterResource(id = R.drawable.maps),
            contentDescription = "Carte du zoo",
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            linesToDraw.forEach { pair ->
                val startX = pair.first.x * imageWidth
                val startY = pair.first.y * imageHeight
                val endX = pair.second.x * imageWidth
                val endY = pair.second.y * imageHeight

                drawLine(
                    color = Color.Red,
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 4f
                )
            }
        }
    }
}