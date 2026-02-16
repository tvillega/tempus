package com.cappielloantonio.tempo.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cappielloantonio.tempo.repository.PlaylistRepository;
import com.cappielloantonio.tempo.subsonic.models.Playlist;

import java.util.List;

public class PlaylistCatalogueViewModel extends AndroidViewModel {
    private final PlaylistRepository playlistRepository;

    private String type;

    private final MutableLiveData<List<Playlist>> playlistList = new MutableLiveData<>(null);
    private LiveData<List<Playlist>> pinnedPlaylists;

    public PlaylistCatalogueViewModel(@NonNull Application application) {
        super(application);

        playlistRepository = new PlaylistRepository();
        pinnedPlaylists = playlistRepository.getPinnedPlaylists();
    }

    public LiveData<List<Playlist>> getPlaylistList(LifecycleOwner owner) {
        if (playlistList.getValue() == null) {
            playlistRepository.getPlaylists(false, -1).observe(owner, playlistList::postValue);
        }

        return playlistList;
    }

    public LiveData<List<Playlist>> getPinnedPlaylists() {
        return pinnedPlaylists;
    }

    public void pinPlaylist(Playlist playlist) {
        playlistRepository.insert(playlist);
    }

    public void unpinPlaylist(Playlist playlist) {
        playlistRepository.delete(playlist);
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
