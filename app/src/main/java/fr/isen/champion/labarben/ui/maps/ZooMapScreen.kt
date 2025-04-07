package fr.isen.champion.labarben.ui.maps

import android.content.Context
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
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
data class ZooPoint(val x: Float, val y: Float)

@Composable
fun ZooMapScreen(navController: NavController) {
    val context = LocalContext.current
    var zooPoints by remember { mutableStateOf(emptyList<ZooPoint>()) }
    val imageWidth = 1080f
    val imageHeight = 1920f
    var currentPoints by remember { mutableStateOf(listOf<ZooPoint>()) }
    var linesToDraw by remember { mutableStateOf(listOf<Pair<ZooPoint, ZooPoint>>()) }
    val radius = 0.07f

    // State to show AlertDialog
    var showAlert by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        zooPoints = loadZooPoints(context) ?: emptyList()
    }

    // Show AlertDialog only once
    if (showAlert) {
        AlertDialog(
            onDismissRequest = { showAlert = false },
            title = { Text("Information") },
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

        DisplayZooPoints(zooPoints, imageWidth, imageHeight)

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

fun calculateDistance(point1: ZooPoint, point2: ZooPoint): Float {
    val dx = point2.x - point1.x
    val dy = point2.y - point1.y
    return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
}

fun findPath(startPoint: ZooPoint, endPoint: ZooPoint, allPoints: List<ZooPoint>): List<Pair<ZooPoint, ZooPoint>> {
    val path = mutableListOf<Pair<ZooPoint, ZooPoint>>()
    var currentPoint = startPoint
    val usedPoints = mutableSetOf<ZooPoint>()
    var currentRadius = 0.07f

    while (currentPoint != endPoint) {
        var nextPoint: ZooPoint? = null
        var attempts = 0

        while (nextPoint == null && attempts < 10) {
            val nearbyPoints = allPoints
                .filter { it != currentPoint && !usedPoints.contains(it) && calculateDistance(currentPoint, it) <= currentRadius }
                .sortedBy { calculateDistance(currentPoint, it) }

            for (candidate in nearbyPoints) {
                val vectorToCandidate = Offset(candidate.x - currentPoint.x, candidate.y - currentPoint.y)
                val vectorToEnd = Offset(endPoint.x - currentPoint.x, endPoint.y - currentPoint.y)
                val dotProduct = vectorToCandidate.x * vectorToEnd.x + vectorToCandidate.y * vectorToEnd.y

                if (dotProduct > 0) {
                    nextPoint = candidate
                    break
                }
            }

            if (nextPoint == null) {
                currentRadius += 0.05f
                attempts++
            }
        }

        if (nextPoint == null) {
            path.add(Pair(currentPoint, endPoint))
            break
        }

        path.add(Pair(currentPoint, nextPoint))
        usedPoints.add(nextPoint)
        currentPoint = nextPoint
        currentRadius = 0.07f
    }

    return path
}

@Composable
fun DisplayZooPoints(zooPoints: List<ZooPoint>, imageWidth: Float, imageHeight: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        zooPoints.forEach { point ->
            val pointX = point.x * imageWidth
            val pointY = point.y * imageHeight

            drawCircle(
                color = Color.Blue,
                radius = 10f,
                center = Offset(pointX, pointY)
            )
        }
    }
}

fun loadZooPoints(context: Context): List<ZooPoint>? {
    val file = File(context.filesDir, "click_points.json")

    return try {
        if (!file.exists()) {
            // Lire depuis assets et copier vers filesDir
            val jsonFromAssets = context.assets.open("click_points.json").bufferedReader().use { it.readText() }
            file.writeText(jsonFromAssets)
        }

        // Lire le JSON depuis le fichier local
        val jsonString = file.readText()
        Json.decodeFromString(jsonString)
    } catch (e: Exception) {
        Log.e("ZooMap", "Erreur lors de la lecture des points", e)
        null
    }
}

fun getClickedPoint(tapOffset: Offset, zooPoints: List<ZooPoint>, imageWidth: Float, imageHeight: Float): ZooPoint? {
    return zooPoints.find { point ->
        val pointX = point.x * imageWidth
        val pointY = point.y * imageHeight

        val distanceSquared = (tapOffset.x - pointX).let { it * it } +
                (tapOffset.y - pointY).let { it * it }

        distanceSquared < 400
    }
}

fun addPointToJson(context: Context, newPoint: ZooPoint) {
    try {
        val file = File(context.filesDir, "click_points.json")
        val currentPoints: MutableList<ZooPoint> = if (file.exists()) {
            Json.decodeFromString(file.readText())
        } else {
            mutableListOf()
        }

        currentPoints.add(newPoint)
        file.writeText(Json.encodeToString(currentPoints))

        Log.d("ZooMap", "Point ajouté : $newPoint")
    } catch (e: Exception) {
        Log.e("ZooMap", "Erreur lors de l'ajout d'un point", e)
    }
}
