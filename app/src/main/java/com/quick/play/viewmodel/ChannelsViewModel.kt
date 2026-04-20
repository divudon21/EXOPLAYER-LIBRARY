package com.quick.play.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.quick.play.data.Channel
import com.quick.play.data.M3uParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChannelsViewModel : ViewModel() {
    private val _channels = MutableStateFlow<List<Channel>>(emptyList())
    val channels: StateFlow<List<Channel>> = _channels.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private var allChannels = listOf<Channel>()

    fun loadChannels(url: String, userAgent: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val parsedChannels = M3uParser.parse(url, userAgent)
                allChannels = parsedChannels
                _channels.value = parsedChannels
                if (parsedChannels.isEmpty()) {
                    _error.value = "No channels found in this playlist."
                }
            } catch (e: Exception) {
                _error.value = "Failed to load playlist: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        filterChannels()
    }

    private fun filterChannels() {
        val query = _searchQuery.value
        if (query.isEmpty()) {
            _channels.value = allChannels
        } else {
            _channels.value = allChannels.filter {
                it.name.contains(query, ignoreCase = true) || it.group.contains(query, ignoreCase = true)
            }
        }
    }
}