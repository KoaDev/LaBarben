package fr.isen.champion.labarben.ui.enclosure

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.champion.labarben.R
import fr.isen.champion.labarben.data.entity.EnclosureEntity
import fr.isen.champion.labarben.data.entity.ZooEntity

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EnclosureListScreen(zoos: List<ZooEntity>) {
    var selectedEnclosure by remember { mutableStateOf<EnclosureEntity?>(null) }
    val expandedMap = remember { mutableStateMapOf<String, Boolean>() }

    var userRole by remember { mutableStateOf("") }

    val maintenanceMap = remember {
        mutableStateMapOf<String, Boolean>().apply {
            zoos.forEach { zoo ->
                zoo.enclosures.forEach { enclosure ->
                    this[enclosure.id] = enclosure.maintenance
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect
        val roleRef = FirebaseDatabase.getInstance().getReference("users").child(uid).child("role")
        roleRef.get().addOnSuccessListener { snapshot ->
            userRole = snapshot.value?.toString() ?: ""
        }
    }

    if (selectedEnclosure != null) {
        EnclosureDetailScreen(
            enclosure = selectedEnclosure,
            onBack = { selectedEnclosure = null }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = stringResource(R.string.enclosurelistscreen_label_title),
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                items(zoos) { zoo ->
                    val isExpanded = expandedMap[zoo.id] ?: false
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .animateContentSize(),
                        elevation = CardDefaults.cardElevation(0.dp),
                        border = BorderStroke(1.dp, Color.LightGray),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Transparent
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        expandedMap[zoo.id] = !isExpanded
                                    },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = zoo.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = if (isExpanded)
                                        Icons.Default.KeyboardArrowUp
                                    else
                                        Icons.Default.KeyboardArrowDown,
                                    contentDescription = null
                                )
                            }
                            if (isExpanded) {
                                Spacer(modifier = Modifier.height(8.dp))
                                zoo.enclosures.forEach { enclosure ->
                                    val currentMaintenance = maintenanceMap[enclosure.id]
                                        ?: enclosure.maintenance
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable(enabled = !currentMaintenance) {
                                                selectedEnclosure = enclosure
                                            }
                                            .alpha(if (currentMaintenance) 0.5f else 1f),
                                        elevation = CardDefaults.cardElevation(0.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color.Transparent
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = stringResource(R.string.enclosurelistscreen_label_number) + enclosure.id,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                if (currentMaintenance) {
                                                    Text(
                                                        text = stringResource(R.string.enclosurelistscreen_label_no_available),
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                            if (userRole == "admin") {
                                                Switch(
                                                    modifier = Modifier.scale(0.8f),
                                                    checked = currentMaintenance,
                                                    onCheckedChange = { newValue ->
                                                        maintenanceMap[enclosure.id] = newValue
                                                        updateMaintenanceInFirebase(zoo.id, enclosure.id, newValue)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

private fun updateMaintenanceInFirebase(zooId: String, enclosureId: String, newValue: Boolean) {
    val dbRef = FirebaseDatabase.getInstance().getReference("zoo")
    dbRef.get().addOnSuccessListener { zooSnapshot ->
        zooSnapshot.children.forEach { zooChild ->
            val zooChildId = zooChild.child("id").value?.toString()
            if (zooChildId == zooId) {
                val enclosuresSnapshot = zooChild.child("enclosures")
                enclosuresSnapshot.children.forEach { enclosureChild ->
                    val enclosureChildId = enclosureChild.child("id").value?.toString()
                    if (enclosureChildId == enclosureId) {
                        enclosureChild.ref.child("maintenance").setValue(newValue)
                    }
                }
            }
        }
    }
}
