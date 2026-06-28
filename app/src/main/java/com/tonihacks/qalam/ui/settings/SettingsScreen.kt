package com.tonihacks.qalam.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tonihacks.qalam.ui.theme.Typography

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val urlDraft by viewModel.urlDraft.collectAsStateWithLifecycle()
    val status by viewModel.connectionStatus.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }

        Text(text = "Settings", style = Typography.headlineMedium)

        OutlinedTextField(
            value = urlDraft,
            onValueChange = viewModel::onUrlChange,
            label = { Text("Base URL") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = viewModel::testConnection,
                enabled = status != SettingsViewModel.ConnectionStatus.Checking,
            ) {
                Text("Test")
            }
            Button(onClick = viewModel::saveUrl) {
                Text("Save")
            }
        }

        when (val s = status) {
            is SettingsViewModel.ConnectionStatus.Idle -> {}
            is SettingsViewModel.ConnectionStatus.Checking -> Text("Checking…")
            is SettingsViewModel.ConnectionStatus.Connected -> Text("Connected")
            is SettingsViewModel.ConnectionStatus.Error -> Text("E: ${s.message}")
        }
    }
}