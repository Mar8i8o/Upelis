package com.example.upelis_mariomarin.data.api

import com.example.upelis_mariomarin.data.model.MovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface TmdbApi {
    @GET("movie/now_playing")
    suspend fun getLatestMovies(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "es-ES",
        @Query("page") page: Int = 1
    ): MovieResponse
}
