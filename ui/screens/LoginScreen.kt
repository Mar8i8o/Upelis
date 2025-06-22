package com.example.upelis_mariomarin.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import androidx.compose.ui.text.input.PasswordVisualTransformation

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val isUserAuthenticated by authViewModel.isUserAuthenticated.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(isUserAuthenticated) {
        if (isUserAuthenticated) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo uPelis más grande
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "u",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    fontSize = 64.sp
                )
            )
            Text(
                text = "Pelis",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFFFFD600),
                    fontSize = 64.sp
                )
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "tu app para gestionar lo que ves",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.Gray,
                fontStyle = FontStyle.Italic
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Iniciar sesión más pequeño y mejor integrado
        Text(
            text = "Iniciar sesión",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,   // Borde cuando está seleccionado
                unfocusedBorderColor = Color.Gray,         // Borde cuando no está seleccionado
                focusedLabelColor = Color.White,     // Color del label al enfocar
                cursorColor = Color.White            // Color del cursor
            )
        )


        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.White,   // Borde cuando está seleccionado
                unfocusedBorderColor = Color.Gray,         // Borde cuando no está seleccionado
                focusedLabelColor = Color.White,     // Color del label al enfocar
                cursorColor = Color.White
            )
        )


        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            Button(
                onClick = { authViewModel.login(email, password) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Entrar")
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¿No tienes cuenta? ",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Regístrate",
                color = Color(0xFFFFD600),
                modifier = Modifier.clickable(onClick = onNavigateToRegister),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline
                )
            )
        }
    }
}
