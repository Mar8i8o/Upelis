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

    private val _sharedPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val sharedPlaylists: StateFlow<List<Playlist>> = _sharedPlaylists

    private val database = FirebaseDatabase.getInstance().reference

    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private var playlistsListener: ValueEventListener? = null
    private var sharedPlaylistsListener: ValueEventListener? = null

    init {
        startListeningSharedPlaylists()
    }

    fun startListeningSharedPlaylists() {
        loadPlaylists()
        loadSharedPlaylists()
    }

    fun stopListeningPlaylists() {
        val uid = userId ?: return
        val ref = database.child("users").child(uid).child("playlists")
        playlistsListener?.let { ref.removeEventListener(it) }
        playlistsListener = null

        val sharedRef = database.child("shared_playlists")
        sharedPlaylistsListener?.let { sharedRef.removeEventListener(it) }
        sharedPlaylistsListener = null
    }

    public fun loadPlaylists() {
        val uid = userId ?: return
        val ref = database.child("users").child(uid).child("playlists")

        playlistsListener?.let { ref.removeEventListener(it) }

        playlistsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Playlist>()
                for (child in snapshot.children) {
                    val playlist = child.getValue(Playlist::class.java)
                    if (playlist != null) {
                        // Extraemos movieIds como lista de Int
                        val movieIdsRaw = child.child("movieIds").children.mapNotNull {
                            it.getValue(Long::class.java)?.toInt()
                        }
                        list.add(playlist.copy(id = child.key ?: "", movieIds = movieIdsRaw))
                    }
                }
                _playlists.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejo de error si quieres
            }
        }
        ref.addValueEventListener(playlistsListener!!)
    }

    private fun loadSharedPlaylists() {
        val currentUserUid = userId ?: return
        val sharedRef = database.child("shared_playlists")

        sharedPlaylistsListener?.let { sharedRef.removeEventListener(it) }

        sharedPlaylistsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Playlist>()
                for (child in snapshot.children) {
                    val sharedWith = child.child("sharedWith")
                    if (sharedWith.hasChild(currentUserUid)) {
                        val playlist = child.getValue(Playlist::class.java)
                        if (playlist != null) {
                            val movieIdsRaw = child.child("movieIds").children.mapNotNull {
                                it.getValue(Long::class.java)?.toInt()
                            }
                            list.add(playlist.copy(id = child.key ?: "", movieIds = movieIdsRaw))
                        }
                    }
                }
                _sharedPlaylists.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                _sharedPlaylists.value = emptyList()
            }
        }
        sharedRef.addValueEventListener(sharedPlaylistsListener!!)
    }

    // Función para eliminar playlist del usuario
    fun deletePlaylist(playlistId: String) {
        val uid = userId ?: return
        val db = database.child("users").child(uid).child("playlists")
        db.child(playlistId).removeValue()
    }

    // Función para renombrar playlist del usuario
    fun renamePlaylist(
        playlistId: String,
        newName: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = userId ?: run {
            onError("Usuario no autenticado")
            return
        }
        val playlistRef = database.child("users").child(uid).child("playlists").child(playlistId)
        playlistRef.child("name").setValue(newName)
            .addOnSuccessListener {
                val updatedList = _playlists.value.map { playlist ->
                    if (playlist.id == playlistId) playlist.copy(name = newName) else playlist
                }
                _playlists.value = updatedList
                onSuccess()
            }
            .addOnFailureListener { e ->
                onError("Error al renombrar playlist: ${e.message}")
            }
    }

    // Crear nueva playlist con un movieId
    fun createPlaylist(
        name: String,
        movieId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = userId ?: run {
            onError("Usuario no autenticado")
            return
        }
        val db = database.child("users").child(uid).child("playlists")
        val newKey = db.push().key
        if (newKey == null) {
            onError("Error al generar clave")
            return
        }

        val newPlaylist = Playlist(
            id = newKey,
            name = name,
            movieIds = if (movieId != -1) listOf(movieId) else emptyList()
        )

        db.child(newKey).setValue(newPlaylist)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError("Error al guardar: ${it.message}") }
    }

    // Añadir película a playlist del usuario
    fun addMovieToPlaylist(
        playlistId: String,
        movieId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = userId ?: run {
            onError("Usuario no autenticado")
            return
        }
        val playlistRef = database.child("users").child(uid).child("playlists").child(playlistId)

        playlistRef.get().addOnSuccessListener { snapshot ->
            val playlist = snapshot.getValue(Playlist::class.java)
            if (playlist != null) {
                val currentIds = playlist.movieIds.toMutableList()
                if (!currentIds.contains(movieId)) {
                    currentIds.add(movieId)
                    playlistRef.child("movieIds").setValue(currentIds)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { e -> onError("Error al actualizar: ${e.message}") }
                } else {
                    onError("La película ya está en esa playlist")
                }
            } else {
                onError("Playlist no encontrada")
            }
        }.addOnFailureListener { e ->
            onError("Error al leer la playlist: ${e.message}")
        }
    }

    // Compartir playlist con un amigo
    fun sharePlaylistWithFriend(
        playlistId: String,
        friendUid: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = userId ?: run {
            onError("Usuario no autenticado")
            return
        }

        // Leo playlist original
        val userPlaylistRef = database.child("users").child(uid).child("playlists").child(playlistId)

        userPlaylistRef.get().addOnSuccessListener { snapshot ->
            val playlist = snapshot.getValue(Playlist::class.java)
            if (playlist == null) {
                onError("Playlist no encontrada")
                return@addOnSuccessListener
            }

            val sharedPlaylistRef = database.child("shared_playlists").child(playlistId)

            // Actualizo o creo la playlist compartida incluyendo la lista de usuarios con acceso
            val sharedData = mapOf(
                "ownerUid" to uid,
                "name" to playlist.name,
                "movieIds" to playlist.movieIds,
                "sharedWith/$friendUid" to true
            )

            sharedPlaylistRef.updateChildren(sharedData).addOnSuccessListener {
                onSuccess()
            }.addOnFailureListener { e ->
                onError("Error al compartir playlist: ${e.message}")
            }

        }.addOnFailureListener { e ->
            onError("Error al leer playlist: ${e.message}")
        }
    }
}
