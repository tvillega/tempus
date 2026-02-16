package com.cappielloantonio.tempo.subsonic.api.navidrome

import android.util.Log
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.util.Preferences
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class NavidromeClient {

    companion object {
        private const val TAG = "NavidromeClient"

        @Volatile
        private var instance: NavidromeClient? = null

        @JvmStatic
        fun getInstance(): NavidromeClient {
            return instance ?: synchronized(this) {
                instance ?: NavidromeClient().also { instance = it }
            }
        }

        @JvmStatic
        fun refresh() {
            instance = null
        }
    }

    private val service: NavidromeService
    private var jwtToken: String? = null

    init {
        val baseUrl = getBaseUrl()
        Log.d(TAG, "init: baseUrl=$baseUrl")

        val gson = GsonBuilder().setLenient().create()

        val client = OkHttpClient.Builder()
            .callTimeout(2, TimeUnit.MINUTES)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
            .build()

        service = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
            .create(NavidromeService::class.java)
    }

    private fun getBaseUrl(): String {
        val server = Preferences.getServer() ?: ""
        return if (server.endsWith("/")) server else "$server/"
    }

    private fun authenticate(): String? {
        val username = Preferences.getUser() ?: return null
        val password = Preferences.getPassword() ?: return null

        try {
            val response = service.login(NavidromeCredentials(username, password)).execute()
            if (response.isSuccessful && response.body()?.token != null) {
                jwtToken = "Bearer ${response.body()!!.token}"
                Log.d(TAG, "authenticate: success")
                return jwtToken
            }
            Log.e(TAG, "authenticate: failed, code=${response.code()}")
        } catch (e: Exception) {
            Log.e(TAG, "authenticate: exception", e)
        }
        return null
    }

    fun getRecentlyPlayedSongs(count: Int): List<Child> {
        val token = authenticate() ?: return emptyList()

        try {
            val response = service.getSongs(
                auth = token,
                sort = "play_date",
                order = "DESC",
                start = 0,
                end = count,
                recentlyPlayed = true
            ).execute()

            if (response.isSuccessful && response.body() != null) {
                val songs = response.body()!!
                Log.d(TAG, "getRecentlyPlayedSongs: got ${songs.size} songs")
                return songs.map { it.toChild() }
            }
            Log.e(TAG, "getRecentlyPlayedSongs: failed, code=${response.code()}")
        } catch (e: Exception) {
            Log.e(TAG, "getRecentlyPlayedSongs: exception", e)
        }
        return emptyList()
    }

    fun getRecentlyPlayedArtists(count: Int): List<ArtistID3> {
        val token = authenticate() ?: return emptyList()

        try {
            val response = service.getArtists(
                auth = token,
                sort = "play_date",
                order = "DESC",
                start = 0,
                end = count,
                recentlyPlayed = true
            ).execute()

            if (response.isSuccessful && response.body() != null) {
                val artists = response.body()!!
                Log.d(TAG, "getRecentlyPlayedArtists: got ${artists.size} artists")
                return artists.map { it.toArtistID3() }
            }
            Log.e(TAG, "getRecentlyPlayedArtists: failed, code=${response.code()}")
        } catch (e: Exception) {
            Log.e(TAG, "getRecentlyPlayedArtists: exception", e)
        }
        return emptyList()
    }

    fun getTopPlayedArtists(count: Int): List<ArtistID3> {
        val token = authenticate() ?: return emptyList()

        try {
            val response = service.getArtistsSorted(
                auth = token,
                sort = "play_count",
                order = "DESC",
                start = 0,
                end = count
            ).execute()

            if (response.isSuccessful && response.body() != null) {
                val artists = response.body()!!
                Log.d(TAG, "getTopPlayedArtists: got ${artists.size} artists")
                return artists.map { it.toArtistID3() }
            }
            Log.e(TAG, "getTopPlayedArtists: failed, code=${response.code()}")
        } catch (e: Exception) {
            Log.e(TAG, "getTopPlayedArtists: exception", e)
        }
        return emptyList()
    }

    fun getTopPlayedSongs(count: Int): List<Child> {
        val token = authenticate() ?: return emptyList()

        try {
            val response = service.getSongsSorted(
                auth = token,
                sort = "play_count",
                order = "DESC",
                start = 0,
                end = count
            ).execute()

            if (response.isSuccessful && response.body() != null) {
                val songs = response.body()!!
                Log.d(TAG, "getTopPlayedSongs: got ${songs.size} songs")
                return songs.map { it.toChild() }
            }
            Log.e(TAG, "getTopPlayedSongs: failed, code=${response.code()}")
        } catch (e: Exception) {
            Log.e(TAG, "getTopPlayedSongs: exception", e)
        }
        return emptyList()
    }

    private fun NavidromeArtist.toArtistID3(): ArtistID3 {
        return ArtistID3(
            id = id,
            name = name,
            coverArtId = coverArtId ?: "ar-$id",
            albumCount = albumCount ?: 0,
            starred = if (starred == true) java.util.Date() else null
        )
    }

    private fun NavidromeSong.toChild(): Child {
        return Child(
            id = id,
            title = title,
            album = album,
            artist = artist,
            albumId = albumId,
            artistId = artistId,
            duration = duration?.toInt(),
            bitrate = bitRate,
            size = size,
            suffix = suffix,
            contentType = contentType,
            path = path,
            genre = genre,
            year = year,
            track = trackNumber,
            discNumber = discNumber,
            playCount = playCount,
            coverArtId = coverArtId ?: id,
            userRating = rating,
            starred = if (starred == true) java.util.Date() else null
        )
    }
}
