package com.example.upelis_mariomarin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.upelis_mariomarin.data.model.Playlist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlaylistsViewModel : ViewModel() {
    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val database = FirebaseDatabase.getInstance().reference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        if (userId == null) return

        val ref = database.child("users").child(userId).child("playlists")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Playlist>()
                for (child in snapshot.children) {
                    val playlist = child.getValue(Playlist::class.java)
                    if (playlist != null) {
                        // Firebase puede devolver movieIds como List<Long>, aquí convertimos a List<Int>
                        val movieIdsRaw = child.child("movieIds").children.mapNotNull { it.getValue(Long::class.java)?.toInt() }
                        list.add(playlist.copy(id = child.key ?: "", movieIds = movieIdsRaw))
                    }
                }
                _playlists.value = list
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // --- NUEVAS FUNCIONES ---

    fun deletePlaylist(playlistId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseDatabase.getInstance().reference.child("users").child(userId).child("playlists")
        db.child(playlistId).removeValue()
    }


    fun createPlaylist(
        name: String,
        movieId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (userId == null) {
            onError("Usuario no autenticado")
            return
        }
        val db = database.child("users").child(userId).child("playlists")
        val newKey = db.push().key
        if (newKey == null) {
            onError("Error al generar clave")
            return
        }

        val newPlaylist = Playlist(
            id = newKey,
            name = name,
            movieIds = listOf(movieId)
        )

        db.child(newKey).setValue(newPlaylist)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError("Error al guardar: ${it.message}") }
    }

    fun addMovieToPlaylist(
        playlistId: String,
        movieId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (userId == null) {
            onError("Usuario no autenticado")
            return
        }
        val db = database.child("users").child(userId).child("playlists")
        val playlistRef = db.child(playlistId)

        playlistRef.get().addOnSuccessListener { snapshot ->
            val playlist = snapshot.getValue(Playlist::class.java)
            if (playlist != null) {
                val currentIds = playlist.movieIds.toMutableList()
                if (!currentIds.contains(movieId)) {
                    currentIds.add(movieId)
                    playlistRef.child("movieIds").setValue(currentIds)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { onError("Error al actualizar: ${it.message}") }
                } else {
                    onError("La película ya está en esa playlist")
                }
            } else {
                onError("Playlist no encontrada")
            }
        }.addOnFailureListener {
            onError("Error al leer la playlist: ${it.message}")
        }
    }
}

