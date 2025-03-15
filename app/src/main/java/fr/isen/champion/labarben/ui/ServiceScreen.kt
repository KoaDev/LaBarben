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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import fr.isen.champion.labarben.R

@Composable
fun ServiceScreen() {
    val services = listOf(
        stringResource(R.string.servicescreen_label_toilets),
        stringResource(R.string.servicescreen_label_water_point),
        stringResource(R.string.servicescreen_label_bakery),
        stringResource(R.string.servicescreen_label_train_station),
        stringResource(R.string.servicescreen_label_train_route),
        stringResource(R.string.servicescreen_label_lodge),
        stringResource(R.string.servicescreen_label_educational_tent),
        stringResource(R.string.servicescreen_label_beach_hut),
        stringResource(R.string.servicescreen_label_nomadic_coffee),
        stringResource(R.string.servicescreen_label_small_coffee),
        stringResource(R.string.servicescreen_label_eyes_plateau),
        stringResource(R.string.servicescreen_label_picnic_area),
        stringResource(R.string.servicescreen_label_viewpoint)
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.servicescreen_label_title),
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RectangleShape),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = serviceName,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
