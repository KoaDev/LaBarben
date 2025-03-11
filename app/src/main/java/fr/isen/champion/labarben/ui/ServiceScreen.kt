package fr.isen.champion.labarben.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ServiceScreen(
    rootNavController: NavHostController
) {
    val services = listOf(
        "Toilettes",
        "Point d'eau",
        "Boulangerie",
        "Gare",
        "Trajet train",
        "Lodge",
        "Tente pédagogique",
        "Paillote",
        "Café nomade",
        "Petit Café",
        "Plateau des yeux",
        "Espace Pique-nique",
        "Point de vue"
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Nos Services",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Liste verticale (LazyColumn)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(services) { service ->
                    ServiceItem(serviceName = service)
                }
            }
        }
    }
}

@Composable
fun ServiceItem(serviceName: String) {
    // Card pour donner un aspect plus visuel
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RectangleShape), // ou RoundedCornerShape(8.dp) si vous préférez
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Petite icône à gauche (exemple : place)
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Nom du service
            Text(
                text = serviceName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
