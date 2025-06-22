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

    private val _movieDetailsMap = MutableStateFlow<Map<Int, MovieDetails>>(emptyMap())
    val movieDetailsMap: StateFlow<Map<Int, MovieDetails>> = _movieDetailsMap

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres

    private val _genreMoviesMap = MutableStateFlow<Map<Int, List<Movie>>>(emptyMap())
    val genreMoviesMap: StateFlow<Map<Int, List<Movie>>> = _genreMoviesMap

    init {
        fetchNowPlayingMovies()
        loadGenresAndMovies()
    }

    fun loadAllMovies() {
        viewModelScope.launch {
            try {
                // 1. Cargar géneros
                val genreResponse = TmdbApiClient.api.getGenres(apiKey)
                _genres.value = genreResponse.genres

                // 2. Obtener películas por género en paralelo
                val deferredMoviesByGenre = genreResponse.genres.map { genre ->
                    async {
                        try {
                            val moviesResponse = TmdbApiClient.api.getMoviesByGenre(apiKey, genre.id)
                            moviesResponse.results
                        } catch (e: Exception) {
                            e.printStackTrace()
                            emptyList<Movie>()
                        }
                    }
                }

                val moviesByGenreLists = deferredMoviesByGenre.awaitAll()

                // 3. Obtener las últimas películas
                val latestResponse = TmdbApiClient.api.getLatestMovies(apiKey)
                val latestMovies = latestResponse.results

                // 4. Combinar todas las listas y eliminar duplicados por id
                val allMoviesCombined = (moviesByGenreLists.flatten() + latestMovies)
                    .distinctBy { it.id }

                // 5. Actualizar el estado
                _movies.value = allMoviesCombined

                // 6. Actualizar el mapa de películas por género también
                val genreMovieMap = genreResponse.genres.mapIndexed { index, genre ->
                    genre.id to moviesByGenreLists.getOrNull(index).orEmpty()
                }.toMap()
                _genreMoviesMap.value = genreMovieMap

                _statusMessage.value = "Películas cargadas correctamente"

            } catch (e: Exception) {
                e.printStackTrace()
                _statusMessage.value = "Error al cargar películas: ${e.message}"
            }
        }
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

                _movieDetailsMap.value = _movieDetailsMap.value.toMutableMap().apply {
                    put(movieId, details)
                }
            } catch (e: Exception) {
                _selectedMovieDetails.value = null
                e.printStackTrace()
            }
        }
    }

    fun fetchMultipleMovieDetails(movieIds: List<Int>) {
        viewModelScope.launch {
            val deferredList = movieIds.map { movieId ->
                async {
                    try {
                        val details = TmdbApiClient.api.getMovieDetails(movieId, apiKey)
                        movieId to details
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                }
            }

            val results = deferredList.awaitAll().filterNotNull().toMap()

            _movieDetailsMap.value = _movieDetailsMap.value.toMutableMap().apply {
                putAll(results)
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

    /**
     * Devuelve los nombres de géneros correspondientes a los ids recibidos.
     * Útil para mostrar géneros legibles en la UI.
     */
    fun getGenreNamesByIds(genreIds: List<Int>?): List<String> {
        if (genreIds == null) return emptyList()
        val currentGenres = _genres.value
        return currentGenres.filter { it.id in genreIds }.map { it.name }
    }

    /**
     * Función que carga la película si no está ya en la lista actual.
     */
    fun loadMovieIfMissing(movieId: Int) {
        // Si ya está cargada, no hacemos nada
        if (_movies.value.any { it.id == movieId }) return

        viewModelScope.launch {
            try {
                val details = TmdbApiClient.api.getMovieDetails(movieId, apiKey)

                val movie = Movie(
                    id = details.id,
                    title = details.title ?: "Título desconocido",
                    overview = details.overview ?: "",
                    posterPath = details.posterPath,  // puede ser null
                    releaseDate = details.releaseDate ?: ""
                )

                _movies.value = _movies.value + movie

                _movieDetailsMap.value = _movieDetailsMap.value.toMutableMap().apply {
                    put(movieId, details)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}
