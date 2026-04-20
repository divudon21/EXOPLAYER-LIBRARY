package com.quick.play.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.quick.play.data.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SettingsRepository(application)
    
    val themePreference = repository.themePreference.stateIn(viewModelScope, SharingStarted.Lazily, 0)
    
    fun setTheme(theme: Int) {
        viewModelScope.launch { repository.setTheme(theme) }
    }
}