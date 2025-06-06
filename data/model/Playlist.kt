package com.example.upelis_mariomarin.data.model

data class Playlist(
    val id: String = "",
    val name: String = "",
    val movieIds: List<Int> = emptyList()
)
