package com.example.upelis_mariomarin.data.model

import com.google.gson.annotations.SerializedName

data class MovieDetails(
    val id: Int,
    val title: String,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("release_date") val releaseDate: String?,
    val runtime: Int?, // duración en minutos
    val genres: List<Genre>?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("original_title") val originalTitle: String?,
    @SerializedName("original_language") val originalLanguage: String?,
    val popularity: Double?,
    @SerializedName("vote_count") val voteCount: Int?
)


data class Genre(
    val id: Int,
    val name: String
)
