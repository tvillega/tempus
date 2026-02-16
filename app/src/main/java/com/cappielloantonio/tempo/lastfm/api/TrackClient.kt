package com.cappielloantonio.tempo.lastfm.api

import com.cappielloantonio.tempo.lastfm.LastFmRetrofitClient
import com.cappielloantonio.tempo.lastfm.models.LastFmTrackResponse
import retrofit2.Call

class TrackClient {
    private val trackService: TrackService =
        LastFmRetrofitClient().retrofit.create(TrackService::class.java)

    fun getTrackInfo(artist: String, track: String, username: String, apiKey: String): Call<LastFmTrackResponse> {
        return trackService.getTrackInfo(artist = artist, track = track, username = username, apiKey = apiKey)
    }
}
