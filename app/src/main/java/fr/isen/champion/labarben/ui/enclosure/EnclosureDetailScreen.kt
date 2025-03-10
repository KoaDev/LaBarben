package fr.isen.champion.labarben.ui.enclosure

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import fr.isen.champion.labarben.R
import fr.isen.champion.labarben.data.entity.EnclosureEntity
import fr.isen.champion.labarben.data.entity.UserReviewEntity
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@SuppressLint("DefaultLocale")
@Composable
fun EnclosureDetailScreen(
    enclosure: EnclosureEntity?,
    onBack: () -> Unit
) {
    if (enclosure == null) return

    Log.d("EnclosureDetailScreen", stringResource(R.string.enclosuredetailscreen_label_display_enclosure) + enclosure.id)

    var rating by remember { mutableStateOf(0) }
    var review by remember { mutableStateOf("") }

    var allReviews by remember { mutableStateOf<List<UserReviewEntity>>(emptyList()) }
    var averageRating by remember { mutableStateOf(0.0) }

    var isLoading by remember { mutableStateOf(true) }

    var userRole by remember { mutableStateOf("") }

    LaunchedEffect(enclosure.id) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            // Pas d'utilisateur connecté => on ne fait rien de plus
            isLoading = false
            return@LaunchedEffect
        }

        // Lecture du rôle dans "/users/{uid}/role"
        val roleSnapshot = FirebaseDatabase.getInstance()
            .getReference("users")
            .child(uid)
            .child("role")
            .get()
            .await()
        userRole = roleSnapshot.value?.toString().orEmpty()

        // Lecture note / review de l'utilisateur
        val fetchedRating = readRatingFromFirebase(enclosure.id_biomes, enclosure.id)
        if (fetchedRating != null) rating = fetchedRating

        val fetchedReview = readReviewFromFirebase(enclosure.id_biomes, enclosure.id)
        if (fetchedReview != null) review = fetchedReview

        // Lecture de tous les avis et notes
        val (all, avg) = readAllRatingsAndReviews(enclosure.id_biomes, enclosure.id)
        allReviews = all
        averageRating = avg

        isLoading = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.enclosuredetailscreen_label_title) + enclosure.id,
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (userRole != "admin") {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.LightGray),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (enclosure.animals.isNotEmpty()) {
                                enclosure.animals.forEach { animal ->
                                    Text("- ${animal.name}", style = MaterialTheme.typography.bodyMedium)
                                }
                            } else {
                                Text(
                                    text = stringResource(R.string.enclosuredetailscreen_label_no_animal_found),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.enclosuredetailscreen_label_rating),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row {
                                for (i in 1..5) {
                                    val starColor = when {
                                        rating == 0 -> Color.Gray
                                        i <= rating -> MaterialTheme.colorScheme.secondary
                                        else -> Color.Gray
                                    }
                                    val icon = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = stringResource(R.string.enclosuredetailscreen_label_star) + i,
                                        tint = starColor,
                                        modifier = Modifier
                                            .scale(1.2f)
                                            .clickable {
                                                rating = i
                                                updateRatingInFirebase(
                                                    enclosure.id_biomes,
                                                    enclosure.id,
                                                    i
                                                )
                                            }
                                            .padding(end = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.enclosuredetailscreen_label_review),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = review,
                                onValueChange = { review = it },
                                label = { Text(stringResource(R.string.enclosuredetailscreen_label_review_hint)) },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    updateReviewInFirebase(
                                        enclosure.id_biomes,
                                        enclosure.id,
                                        review
                                    )
                                }
                            ) {
                                Text(stringResource(R.string.enclosuredetailscreen_label_submit_review))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        border = BorderStroke(1.dp, Color.LightGray),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (allReviews.isEmpty()) {
                                Text(stringResource(R.string.enclosuredetailscreen_label_no_reviews_yet))
                            } else {
                                val avgStr = String.format("%.1f", averageRating)
                                Text(stringResource(R.string.enclosuredetailscreen_label_average_rating) + avgStr)
                                Spacer(modifier = Modifier.height(8.dp))
                                allReviews.forEach { item ->
                                    val starText = if (item.rating > 0) {
                                        " - ${item.rating} ★"
                                    } else {
                                        ""
                                    }
                                    Text("${item.firstName} ${item.lastName}$starText")
                                    if (item.review.isNotEmpty()) {
                                        Text(
                                            "\"${item.review}\"",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = { onBack() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.enclosuredetailscreen_label_back))
                }
            }
        }
    }
}

private suspend fun readRatingFromFirebase(zooId: String, enclosureId: String): Int? {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
    val dbRef = FirebaseDatabase.getInstance().getReference("zoo")
    return suspendCancellableCoroutine { continuation ->
        dbRef.get().addOnSuccessListener { zooSnapshot ->
            zooSnapshot.children.forEach { zooChild ->
                val zooChildId = zooChild.child("id").value?.toString()
                if (zooChildId == zooId) {
                    val enclosuresSnapshot = zooChild.child("enclosures")
                    enclosuresSnapshot.children.forEach { enclosureChild ->
                        val enclosureChildId = enclosureChild.child("id").value?.toString()
                        if (enclosureChildId == enclosureId) {
                            val ratingsSnapshot = enclosureChild.child("ratings")
                            val userRating = ratingsSnapshot.child(uid).value
                            if (userRating != null) {
                                val ratingValue = userRating.toString().toIntOrNull()
                                continuation.resume(ratingValue)
                                return@addOnSuccessListener
                            }
                        }
                    }
                }
            }
            continuation.resume(null)
        }.addOnFailureListener {
            continuation.resume(null)
        }
    }
}

private suspend fun readReviewFromFirebase(zooId: String, enclosureId: String): String? {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return null
    val dbRef = FirebaseDatabase.getInstance().getReference("zoo")
    return suspendCancellableCoroutine { continuation ->
        dbRef.get().addOnSuccessListener { zooSnapshot ->
            zooSnapshot.children.forEach { zooChild ->
                val zooChildId = zooChild.child("id").value?.toString()
                if (zooChildId == zooId) {
                    val enclosuresSnapshot = zooChild.child("enclosures")
                    enclosuresSnapshot.children.forEach { enclosureChild ->
                        val enclosureChildId = enclosureChild.child("id").value?.toString()
                        if (enclosureChildId == enclosureId) {
                            val reviewsSnapshot = enclosureChild.child("reviews")
                            val userReview = reviewsSnapshot.child(uid).value
                            if (userReview != null) {
                                continuation.resume(userReview.toString())
                                return@addOnSuccessListener
                            }
                        }
                    }
                }
            }
            continuation.resume(null)
        }.addOnFailureListener {
            continuation.resume(null)
        }
    }
}

private suspend fun readAllRatingsAndReviews(zooId: String, enclosureId: String): Pair<List<UserReviewEntity>, Double> {
    val dbRef = FirebaseDatabase.getInstance().getReference("zoo")
    val result = mutableListOf<UserReviewEntity>()

    val zooSnapshot = try {
        dbRef.get().await()
    } catch (e: Exception) {
        return Pair(emptyList(), 0.0)
    }

    var enclosureRef: DatabaseReference? = null

    for (zooChild in zooSnapshot.children) {
        val zooChildId = zooChild.child("id").value?.toString()
        if (zooChildId == zooId) {
            val enclosuresSnapshot = zooChild.child("enclosures")
            for (encChild in enclosuresSnapshot.children) {
                val enclosureChildId = encChild.child("id").value?.toString()
                if (enclosureChildId == enclosureId) {
                    enclosureRef = encChild.ref
                    break
                }
            }
            break
        }
    }
    if (enclosureRef == null) return Pair(emptyList(), 0.0)

    val enclosureData = try {
        enclosureRef.get().await()
    } catch (e: Exception) {
        return Pair(emptyList(), 0.0)
    }

    val ratingsSnapshot = enclosureData.child("ratings")
    val reviewsSnapshot = enclosureData.child("reviews")

    val userIds = mutableSetOf<String>()
    ratingsSnapshot.children.forEach { userIds.add(it.key ?: "") }
    reviewsSnapshot.children.forEach { userIds.add(it.key ?: "") }

    val userRef = FirebaseDatabase.getInstance().getReference("users")

    for (uid in userIds) {
        if (uid.isBlank()) continue
        val ratingValue = ratingsSnapshot.child(uid).value?.toString()?.toIntOrNull() ?: 0
        val reviewValue = reviewsSnapshot.child(uid).value?.toString().orEmpty()

        val userSnapshot = try {
            userRef.child(uid).get().await()
        } catch (e: Exception) {
            null
        }
        val firstName = userSnapshot?.child("firstName")?.value?.toString().orEmpty()
        val lastName = userSnapshot?.child("lastName")?.value?.toString().orEmpty()

        result.add(
            UserReviewEntity(
                userId = uid,
                firstName = firstName,
                lastName = lastName,
                rating = ratingValue,
                review = reviewValue
            )
        )
    }

    val validRatings = result.map { it.rating }.filter { it > 0 }
    val average = if (validRatings.isEmpty()) 0.0 else validRatings.average()

    return Pair(result, average)
}

private fun updateRatingInFirebase(zooId: String, enclosureId: String, rating: Int) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val dbRef = FirebaseDatabase.getInstance().getReference("zoo")
    dbRef.get().addOnSuccessListener { zooSnapshot ->
        zooSnapshot.children.forEach { zooChild ->
            val zooChildId = zooChild.child("id").value?.toString()
            if (zooChildId == zooId) {
                val enclosuresSnapshot = zooChild.child("enclosures")
                enclosuresSnapshot.children.forEach { enclosureChild ->
                    val enclosureChildId = enclosureChild.child("id").value?.toString()
                    if (enclosureChildId == enclosureId) {
                        enclosureChild.ref
                            .child("ratings")
                            .child(uid)
                            .setValue(rating)
                    }
                }
            }
        }
    }
}

private fun updateReviewInFirebase(zooId: String, enclosureId: String, review: String) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val dbRef = FirebaseDatabase.getInstance().getReference("zoo")
    dbRef.get().addOnSuccessListener { zooSnapshot ->
        zooSnapshot.children.forEach { zooChild ->
            val zooChildId = zooChild.child("id").value?.toString()
            if (zooChildId == zooId) {
                val enclosuresSnapshot = zooChild.child("enclosures")
                enclosuresSnapshot.children.forEach { enclosureChild ->
                    val enclosureChildId = enclosureChild.child("id").value?.toString()
                    if (enclosureChildId == enclosureId) {
                        enclosureChild.ref
                            .child("reviews")
                            .child(uid)
                            .setValue(review)
                    }
                }
            }
        }
    }
}
