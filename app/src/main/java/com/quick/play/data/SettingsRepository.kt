package com.quick.play.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(private val context: Context) {
    private val themeKey = intPreferencesKey("theme_pref")
    
    val themePreference: Flow<Int> = context.dataStore.data.map { it[themeKey] ?: 0 }
    
    suspend fun setTheme(theme: Int) {
        context.dataStore.edit { it[themeKey] = theme }
    }
}