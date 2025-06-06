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
}
