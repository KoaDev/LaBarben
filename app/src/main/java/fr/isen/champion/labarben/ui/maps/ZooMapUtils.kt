package fr.isen.champion.labarben.ui.maps

import android.content.Context
import android.util.Log
import androidx.compose.ui.geometry.Offset
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import kotlin.math.atan2

// Fonction pour calculer l'angle entre deux points (en radians)
fun calculateAngle(from: ZooPoint, to: ZooPoint): Float {
    return atan2(to.y - from.y, to.x - from.x)
}

// Fonction pour vérifier si un voisin est dans l'arc de cercle de 110° autour de la direction vers le endPoint
fun isWithinArc(from: ZooPoint, to: ZooPoint, endPoint: ZooPoint, angleMargin: Double): Boolean {
    val angleToEnd = calculateAngle(from, endPoint)
    val angleToNeighbor = calculateAngle(from, to)

    val angleDifference = Math.abs(angleToNeighbor - angleToEnd)
    return angleDifference <= angleMargin / 2
}

// Trouve un chemin entre startPoint et endPoint avec une direction de 110° autour du vecteur vers endPoint
// Utilise un rayon de recherche qui augmente si aucun voisin n'est trouvé.
fun findPath(startPoint: ZooPoint, endPoint: ZooPoint, allPoints: List<ZooPoint>): List<Pair<ZooPoint, ZooPoint>> {
    val path = mutableListOf<Pair<ZooPoint, ZooPoint>>()
    var currentPoint = startPoint
    val visited = mutableSetOf<ZooPoint>()
    var radius = 0.01f
    val angleMargin = Math.toRadians(110.0) // 110° en radians

    visited.add(currentPoint)

    while (calculateDistance(currentPoint, endPoint) > 0.01f) {
        // Recherche des voisins dans le rayon actuel, non visités
        val neighbors = allPoints
            .filter { it != currentPoint && it !in visited && calculateDistance(currentPoint, it) <= radius }
            .filter { isWithinArc(currentPoint, it, endPoint, angleMargin) }

        if (neighbors.isEmpty()) {
            // Aucun voisin trouvé, on élargit le rayon
            Log.d("ZooMap", "Voisin pas trouvé, rayon élargi")
            radius += 0.01f // Augmenter le rayon de manière plus significative
            continue
        }

        // Choisir le voisin le plus proche du point actuel qui respecte l'arc de 110°
        val nextPoint = neighbors.minByOrNull { calculateDistance(it, endPoint) } ?: break

        // Ajout du voisin trouvé au chemin
        path.add(Pair(currentPoint, nextPoint))
        currentPoint = nextPoint
        visited.add(currentPoint)

        // Réinitialise le rayon de recherche à la valeur initiale après chaque étape
        radius = 0.01f
    }

    // Si on est proche du point final, on ajoute le dernier segment
    if (calculateDistance(currentPoint, endPoint) <= 0.02f && currentPoint != endPoint) {
        path.add(Pair(currentPoint, endPoint))
    }

    return path
}

// Calcule la distance euclidienne entre deux points 2D.
fun calculateDistance(point1: ZooPoint, point2: ZooPoint): Float {
    val dx = point2.x - point1.x
    val dy = point2.y - point1.y
    return Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()
}

// Recherche un point cliqué dans une liste de points en fonction de la position sur l'écran (tapOffset).
fun getClickedPoint(tapOffset: Offset, zooPoints: List<ZooPoint>, imageWidth: Float, imageHeight: Float): ZooPoint? {
    return zooPoints.find { point ->
        val pointX = point.x * imageWidth
        val pointY = point.y * imageHeight

        val distanceSquared = (tapOffset.x - pointX).let { it * it } +
                (tapOffset.y - pointY).let { it * it }

        distanceSquared < 900
    }
}

// Charge les points de zoo depuis un fichier JSON local dans l'application.
fun loadZooPoints(context: Context): List<ZooPoint>? {
    val file = File(context.filesDir, "click_points.json")

    return try {
        if (!file.exists()) {
            val jsonFromAssets = context.assets.open("click_points.json").bufferedReader().use { it.readText() }
            file.writeText(jsonFromAssets)
        }

        val jsonString = file.readText()
        Json.decodeFromString(jsonString)
    } catch (e: Exception) {
        Log.e("ZooMap", "Erreur lors de la lecture des points", e)
        null
    }
}

// Ajoute un nouveau point dans le fichier JSON local contenant les points.
// Nous avons utilisé cette fonction pour créer le fichier click_points.json
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
