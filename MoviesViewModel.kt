package com.example.upelis_mariomarin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.upelis_mariomarin.data.api.TmdbApiClient
import com.example.upelis_mariomarin.data.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MoviesViewModel : ViewModel() {

    private val apiKey = "8e3b65e494d34716bbdf077be06f14ca"

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    val movies: StateFlow<List<Movie>> = _movies

    private val _statusMessage = MutableStateFlow("Cargando...")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _selectedMovieDetails = MutableStateFlow<MovieDetails?>(null)
    val selectedMovieDetails: StateFlow<MovieDetails?> = _selectedMovieDetails

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres

    private val _genreMoviesMap = MutableStateFlow<Map<Int, List<Movie>>>(emptyMap())
    val genreMoviesMap: StateFlow<Map<Int, List<Movie>>> = _genreMoviesMap

    init {
        fetchNowPlayingMovies()
        loadGenresAndMovies()
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

    fun clearMovieDetails() {
        _selectedMovieDetails.value = null
    }

    fun loadGenresAndMovies() {
        viewModelScope.launch {
            try {
                val genreResponse = TmdbApiClient.api.getGenres(apiKey)
                _genres.value = genreResponse.genres

                val deferredList = genreResponse.genres.map { genre ->
                    async {
                        try {
                            val movieResponse = TmdbApiClient.api.getMoviesByGenre(apiKey, genre.id)
                            genre.id to movieResponse.results
                        } catch (e: Exception) {
                            e.printStackTrace()
                            genre.id to emptyList()
                        }
                    }
                }

                val results = deferredList.awaitAll()
                val genreMovieMap = results.toMap()

                _genreMoviesMap.value = genreMovieMap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Función para cargar películas de un género bajo demanda
    fun loadMoviesByGenre(genreId: Int) {
        viewModelScope.launch {
            try {
                val response = TmdbApiClient.api.getMoviesByGenre(apiKey, genreId)
                val currentMap = _genreMoviesMap.value.toMutableMap()
                currentMap[genreId] = response.results
                _genreMoviesMap.value = currentMap
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}

