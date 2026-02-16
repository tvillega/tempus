package com.cappielloantonio.tempo.lastfm

import com.cappielloantonio.tempo.lastfm.api.TrackClient

object LastFm {
    const val BASE_URL = "https://ws.audioscrobbler.com/2.0/"

    val trackClient: TrackClient by lazy { TrackClient() }
}
