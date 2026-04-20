package com.quick.play.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quick.play.data.Playlist
import com.quick.play.data.PlaylistRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaylistsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = PlaylistRepository(application)

    val allPlaylists: StateFlow<List<Playlist>> = repository.customPlaylists
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun addPlaylist(name: String, url: String, userAgent: String = "") {
        viewModelScope.launch {
            repository.addPlaylist(Playlist(name, url, userAgent, isCustom = true))
        }
    }
    
    fun deletePlaylist(playlist: Playlist) {
        viewModelScope.launch {
            repository.deletePlaylist(playlist)
        }
    }
}