package com.example.upelis_mariomarin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class WatchedMoviesViewModel : ViewModel() {

    private val _watchedMovies = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val watchedMovies: StateFlow<Map<Int, Boolean>> = _watchedMovies.asStateFlow()

    private val userId: String? = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseDatabase.getInstance().reference

    init {
        loadAllWatchedMovies()
    }

    fun loadAllWatchedMovies() {
        if (userId == null) {
            _watchedMovies.value = emptyMap()
            return
        }

        db.child("users").child(userId).child("watchedMovies")
            .get()
            .addOnSuccessListener { snapshot ->
                val map = mutableMapOf<Int, Boolean>()
                snapshot.children.forEach { child ->
                    val movieId = child.key?.toIntOrNull()
                    val watched = child.getValue(Boolean::class.java) ?: false
                    if (movieId != null && watched) {
                        map[movieId] = true
                    }
                }
                _watchedMovies.value = map
            }
            .addOnFailureListener {
                _watchedMovies.value = emptyMap()
            }
    }

    fun toggleWatched(movieId: Int) {
        if (userId == null) return

        val currentMap = _watchedMovies.value.toMutableMap()
        val isCurrentlyWatched = currentMap[movieId] == true

        val newValue = !isCurrentlyWatched

        db.child("users").child(userId).child("watchedMovies").child(movieId.toString())
            .setValue(newValue)
            .addOnSuccessListener {
                currentMap[movieId] = newValue
                _watchedMovies.value = currentMap
            }
            .addOnFailureListener {
                // Opcional: manejar error
            }
    }

    // Función suspend que consulta Firebase puntualmente si la película está vista
    suspend fun checkIfWatched(movieId: Int): Boolean = suspendCancellableCoroutine { cont ->
        if (userId == null) {
            cont.resume(false)
            return@suspendCancellableCoroutine
        }

        db.child("users").child(userId).child("watchedMovies").child(movieId.toString())
            .get()
            .addOnSuccessListener { snapshot ->
                val watched = snapshot.getValue(Boolean::class.java) ?: false
                cont.resume(watched)
            }
            .addOnFailureListener { e ->
                cont.resumeWithException(e)
            }
    }
}
