package com.example.upelis_mariomarin

import MoviesScreen
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.upelis_mariomarin.ui.theme.UPelis_MarioMarinTheme
import com.example.upelis_mariomarin.viewmodel.MoviesViewModel

class MainActivity : ComponentActivity() {

    private val moviesViewModel by viewModels<MoviesViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UPelis_MarioMarinTheme{
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MoviesScreen(
                        viewModel = moviesViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
