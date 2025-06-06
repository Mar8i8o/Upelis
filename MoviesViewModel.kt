package com.example.upelis_mariomarin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upelis_mariomarin.data.api.TmdbApiClient
import com.example.upelis_mariomarin.data.model.Movie
import com.example.upelis_mariomarin.data.model.MovieDetails
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _statusMessage = MutableStateFlow("Cargando...")
    val statusMessage: StateFlow<String> = _statusMessage

    private val apiKey = "8e3b65e494d34716bbdf077be06f14ca"

    // Estado para detalles
    private val _selectedMovieDetails = MutableStateFlow<MovieDetails?>(null)
    val selectedMovieDetails: StateFlow<MovieDetails?> = _selectedMovieDetails

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

    // NUEVO: función para cargar detalles
    fun fetchMovieDetails(movieId: Int) {
        viewModelScope.launch {
            try {
                val details = TmdbApiClient.api.getMovieDetails(movieId, apiKey)
                _selectedMovieDetails.value = details
            } catch (e: Exception) {
                _selectedMovieDetails.value = null
                e.printStackTrace()
            }
        }
    }

    // Para limpiar detalles cuando se vuelve atrás
    fun clearMovieDetails() {
        _selectedMovieDetails.value = null
    }

    // Puedes seguir teniendo esta función para buscar en lista local (opcional)
    fun getMovieById(id: Int): Movie? {
        return _movies.value.find { it.id == id }
    }
}
