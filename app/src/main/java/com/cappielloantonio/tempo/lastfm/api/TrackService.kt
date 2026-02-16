package com.cappielloantonio.tempo.lastfm.api

import com.cappielloantonio.tempo.lastfm.models.LastFmTrackResponse
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface TrackService {
    @GET(".")
    fun getTrackInfo(
        @Query("method") method: String = "track.getInfo",
        @Query("artist") artist: String,
        @Query("track") track: String,
        @Query("username") username: String,
        @Query("api_key") apiKey: String,
        @Query("format") format: String = "json"
    ): Call<LastFmTrackResponse>
}
