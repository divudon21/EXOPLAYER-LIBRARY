package com.quick.play.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore by preferencesDataStore(name = "playlists")

class PlaylistRepository(private val context: Context) {
    private val customPlaylistsKey = stringPreferencesKey("custom_playlists")

    val customPlaylists: Flow<List<Playlist>> = context.dataStore.data.map { preferences ->
        val jsonString = preferences[customPlaylistsKey] ?: "[]"
        try {
            Json.decodeFromString<List<Playlist>>(jsonString)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addPlaylist(playlist: Playlist) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[customPlaylistsKey] ?: "[]"
            val currentList = try {
                Json.decodeFromString<List<Playlist>>(currentJson)
            } catch (e: Exception) { emptyList() }
            
            val newList = currentList + playlist
            preferences[customPlaylistsKey] = Json.encodeToString(newList)
        }
    }
    
    suspend fun deletePlaylist(playlist: Playlist) {
        context.dataStore.edit { preferences ->
            val currentJson = preferences[customPlaylistsKey] ?: "[]"
            val currentList = try {
                Json.decodeFromString<List<Playlist>>(currentJson)
            } catch (e: Exception) { emptyList() }
            
            val newList = currentList.filter { it.url != playlist.url || it.name != playlist.name }
            preferences[customPlaylistsKey] = Json.encodeToString(newList)
        }
    }
}