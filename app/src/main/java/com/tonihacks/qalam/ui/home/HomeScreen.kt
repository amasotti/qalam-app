package com.tonihacks.qalam.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier


@Composable
fun HomeScreen(onNavigateToSettings: () -> Unit = {}) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Home")
        IconButton(
            onClick = onNavigateToSettings,
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            Icon(Icons.Outlined.Settings, contentDescription = "Settings")
        }
    }
}