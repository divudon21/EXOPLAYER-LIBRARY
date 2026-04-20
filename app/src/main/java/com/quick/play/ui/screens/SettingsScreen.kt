package com.quick.play.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quick.play.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = viewModel()) {
    val themePref by viewModel.themePreference.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Text(
                "Theme", 
                style = MaterialTheme.typography.titleMedium, 
                modifier = Modifier.padding(16.dp), 
                color = MaterialTheme.colorScheme.primary
            )
            ThemeOptionRow("System Default", Icons.Default.BrightnessAuto, themePref == 0) { viewModel.setTheme(0) }
            ThemeOptionRow("Light", Icons.Default.LightMode, themePref == 1) { viewModel.setTheme(1) }
            ThemeOptionRow("Dark", Icons.Default.Brightness4, themePref == 2) { viewModel.setTheme(2) }
        }
    }
}

@Composable
fun ThemeOptionRow(
    title: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    selected: Boolean, 
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp), 
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon, 
            contentDescription = null, 
            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(title, modifier = Modifier.weight(1f))
        RadioButton(selected = selected, onClick = onClick)
    }
}