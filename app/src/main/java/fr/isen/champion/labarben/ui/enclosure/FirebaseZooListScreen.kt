package fr.isen.champion.labarben.ui.enclosure

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.firebase.database.FirebaseDatabase
import fr.isen.champion.labarben.data.entity.ZooEntity

@Composable
fun FirebaseZooListScreen() {
    var zoos by remember { mutableStateOf<List<ZooEntity>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val database = FirebaseDatabase.getInstance().reference
        database.child("zoo").get()
            .addOnSuccessListener { snapshot ->
                val list = mutableListOf<ZooEntity>()
                snapshot.children.forEach { child ->
                    val zoo = child.getValue(ZooEntity::class.java)
                    if (zoo != null) {
                        list.add(zoo)
                    }
                }
                zoos = list
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        EnclosureListScreen(zoos = zoos)
    }
}
