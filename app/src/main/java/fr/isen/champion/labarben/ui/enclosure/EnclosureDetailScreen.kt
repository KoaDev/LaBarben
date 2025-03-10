package fr.isen.champion.labarben.ui.enclosure

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.champion.labarben.R
import fr.isen.champion.labarben.data.entity.EnclosureEntity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@Composable
fun EnclosureDetailScreen(
    enclosure: EnclosureEntity?,
    onBack: () -> Unit,
) {
    if (enclosure == null) return
    Log.d("EnclosureDetailScreen", stringResource(R.string.enclosuredetailscreen_label_display_enclosure) + enclosure.id)
    var rating by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(enclosure.id) {
        val fetchedRating = readRatingFromFirebase(enclosure.id_biomes, enclosure.id)
        if (fetchedRating != null) {
            rating = fetchedRating
        }
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
                                                zooId = enclosure.id_biomes,
                                                enclosureId = enclosure.id,
                                                rating = i
                                            )
                                        }
                                        .padding(end = 4.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {
                        onBack()
                    },
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

private fun updateRatingInFirebase(zooId: String, enclosureId: String, rating: Int) {
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: run {
        return
    }
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
