package com.example.upelis_mariomarin.viewmodel

import androidx.lifecycle.ViewModel
import com.example.upelis_mariomarin.data.model.Playlist
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PlaylistsViewModel : ViewModel() {

    private val _playlists = MutableStateFlow<List<Playlist>>(emptyList())
    val playlists: StateFlow<List<Playlist>> = _playlists

    private val _sharedPlaylists = MutableStateFlow<List<Playlist>>(emptyList())
    val sharedPlaylists: StateFlow<List<Playlist>> = _sharedPlaylists

    private val database = FirebaseDatabase.getInstance().reference
    private val firestore = Firebase.firestore

    private val userId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    private var playlistsListener: ValueEventListener? = null

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
    }

    private fun loadPlaylists() {
        val uid = userId ?: return
        val ref = database.child("users").child(uid).child("playlists")

        // Eliminar listener anterior si existe
        playlistsListener?.let { ref.removeEventListener(it) }

        playlistsListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Playlist>()
                for (child in snapshot.children) {
                    val playlist = child.getValue(Playlist::class.java)
                    if (playlist != null) {
                        val movieIdsRaw = child.child("movieIds").children.mapNotNull {
                            it.getValue(Long::class.java)?.toInt()
                        }
                        list.add(playlist.copy(id = child.key ?: "", movieIds = movieIdsRaw))
                    }
                }
                _playlists.value = list
            }

            override fun onCancelled(error: DatabaseError) {
                // Puedes añadir logs o manejo de error aquí si quieres
            }
        }
        ref.addValueEventListener(playlistsListener!!)
    }

    fun loadSharedPlaylists() {
        val currentUserUid = userId ?: return
        val sharedRef = firestore.collection("shared_playlists")

        sharedRef.whereEqualTo("sharedWithUid", currentUserUid).get()
            .addOnSuccessListener { snapshot ->
                val playlistIds = snapshot.documents.mapNotNull { it.getString("playlistId") }
                if (playlistIds.isEmpty()) {
                    _sharedPlaylists.value = emptyList()
                    return@addOnSuccessListener
                }

                val playlistsList = mutableListOf<Playlist>()

                database.child("users").get().addOnSuccessListener { usersSnapshot ->
                    for (userSnapshot in usersSnapshot.children) {
                        val userPlaylistsSnapshot = userSnapshot.child("playlists")
                        for (pid in playlistIds) {
                            val playlistSnapshot = userPlaylistsSnapshot.child(pid)
                            val playlist = playlistSnapshot.getValue(Playlist::class.java)
                            if (playlist != null && playlistsList.none { it.id == pid }) {
                                val movieIdsRaw = playlistSnapshot.child("movieIds").children.mapNotNull {
                                    it.getValue(Long::class.java)?.toInt()
                                }
                                playlistsList.add(playlist.copy(id = pid, movieIds = movieIdsRaw))
                            }
                        }
                    }
                    _sharedPlaylists.value = playlistsList
                }.addOnFailureListener {
                    _sharedPlaylists.value = emptyList()
                }
            }
            .addOnFailureListener {
                _sharedPlaylists.value = emptyList()
            }
    }

    fun deletePlaylist(playlistId: String) {
        val uid = userId ?: return
        val db = database.child("users").child(uid).child("playlists")
        db.child(playlistId).removeValue()
    }

    fun renamePlaylist(
        playlistId: String,
        newName: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = userId
        if (uid == null) {
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
            .addOnFailureListener { exception ->
                onError("Error al renombrar playlist: ${exception.message}")
            }
    }

    fun createPlaylist(
        name: String,
        movieId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = userId
        if (uid == null) {
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

    fun addMovieToPlaylist(
        playlistId: String,
        movieId: Int,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val uid = userId
        if (uid == null) {
            onError("Usuario no autenticado")
            return
        }
        val db = database.child("users").child(uid).child("playlists")
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

    fun sharePlaylistWithFriend(
        playlistId: String,
        friendUid: String,
        onSuccess: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val uid = userId
        if (uid == null) {
            onError("Usuario no autenticado")
            return
        }
        val sharedRef = firestore.collection("shared_playlists")

        val data = mapOf(
            "playlistId" to playlistId,
            "sharedWithUid" to friendUid,
            "sharedByUid" to uid,
            "sharedAt" to System.currentTimeMillis()
        )

        sharedRef.add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError("Error al compartir playlist: ${e.message}") }
    }
}
