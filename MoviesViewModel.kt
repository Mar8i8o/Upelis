package com.example.upelis_mariomarin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upelis_mariomarin.data.model.Movie
import com.example.upelis_mariomarin.data.api.TmdbApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _statusMessage = MutableStateFlow("Cargando...")
    val statusMessage: StateFlow<String> = _statusMessage

    private val apiKey = "8e3b65e494d34716bbdf077be06f14ca"

    init {
        fetchNowPlayingMovies()
    }

    private fun fetchNowPlayingMovies() {
        viewModelScope.launch {
            try {
                val response = TmdbApiClient.api.getLatestMovies(apiKey)
                if (response.results.isNotEmpty()) {
                    _movies.value = response.results
                    _statusMessage.value = "Se ha conectado correctamente"
                } else {
                    _statusMessage.value = "Conexión establecida, pero no hay películas"
                }
            } catch (e: Exception) {
                _statusMessage.value = "Error al conectar: ${e.message}"
                e.printStackTrace()
            }
        }
    }
}
