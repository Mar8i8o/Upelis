package com.example.upelis_mariomarin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.upelis_mariomarin.ui.screens.LoginScreen
import com.example.upelis_mariomarin.ui.screens.RegisterScreen
import com.example.upelis_mariomarin.ui.screens.MoviesScreen
import com.example.upelis_mariomarin.ui.theme.UPelis_MarioMarinTheme
import com.example.upelis_mariomarin.viewmodel.AuthViewModel
import com.example.upelis_mariomarin.viewmodel.MoviesViewModel

class MainActivity : ComponentActivity() {

    private val moviesViewModel by viewModels<MoviesViewModel>()
    private val authViewModel by viewModels<AuthViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            UPelis_MarioMarinTheme {
                var isLoggedIn by remember { mutableStateOf(authViewModel.currentUser != null) }
                var showRegister by remember { mutableStateOf(false) }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when {
                        !isLoggedIn && !showRegister -> {
                            LoginScreen(
                                authViewModel = authViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onLoginSuccess = { isLoggedIn = true },
                                onNavigateToRegister = { showRegister = true }
                            )
                        }
                        !isLoggedIn && showRegister -> {
                            RegisterScreen(
                                authViewModel = authViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onRegisterSuccess = { isLoggedIn = true },
                                onNavigateToLogin = { showRegister = false }
                            )
                        }
                        else -> {
                            MoviesScreen(
                                moviesViewModel = moviesViewModel,
                                authViewModel = authViewModel,
                                modifier = Modifier.padding(innerPadding),
                                onLogout = {
                                    // Navegar a login: resetea estados
                                    isLoggedIn = false
                                    showRegister = false
                                }
                            )

                        }

                    }
                }
            }
        }
    }
}
