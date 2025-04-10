package fr.isen.champion.labarben.ui.user

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import fr.isen.champion.labarben.R
import fr.isen.champion.labarben.road.Screen

@Composable
fun RegisterScreen(navController: NavController) {
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    // Variable pour le rôle, initialisé à "user"
    var selectedRole by remember { mutableStateOf("user") }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance().reference

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.registerScreen_label_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text(stringResource(R.string.registerScreen_label_firstname)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text(stringResource(R.string.registerScreen_label_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text(stringResource(R.string.registerScreen_label_email)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text(stringResource(R.string.registerScreen_label_password)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Groupe de boutons radio pour le rôle
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Sélectionnez le rôle",  // Vous pouvez aussi gérer l'internationalisation ici
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRole = "user" }
                        .padding(4.dp)
                ) {
                    RadioButton(
                        selected = (selectedRole == "user"),
                        onClick = { selectedRole = "user" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "User")
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedRole = "admin" }
                        .padding(4.dp)
                ) {
                    RadioButton(
                        selected = (selectedRole == "admin"),
                        onClick = { selectedRole = "admin" }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Admin")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val uid = auth.currentUser?.uid
                                if (uid != null) {
                                    val userData = mapOf(
                                        "firstName" to firstName,
                                        "lastName" to lastName,
                                        "email" to email,
                                        "role" to selectedRole  // Utilisation du rôle sélectionné
                                    )
                                    database.child("users").child(uid).setValue(userData)
                                        .addOnSuccessListener {
                                            // Navigation après confirmation de l'écriture réussie
                                            navController.navigate(Screen.Home.route) {
                                                popUpTo(Screen.Register.route) { inclusive = true }
                                            }
                                        }
                                        .addOnFailureListener { error ->
                                            Toast.makeText(context, "Erreur lors de la création de l'utilisateur: ${error.localizedMessage}", Toast.LENGTH_LONG).show()
                                        }
                                }
                            } else {
                                Toast.makeText(context, R.string.registerScreen_notification_error, Toast.LENGTH_SHORT).show()
                            }
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text(stringResource(R.string.registerScreen_label_submit))
            }
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(
                onClick = { navController.navigate(Screen.Login.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.registerScreen_label_have_account))
            }
        }
    }
}
