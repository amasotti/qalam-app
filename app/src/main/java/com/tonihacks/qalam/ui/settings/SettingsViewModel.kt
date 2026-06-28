package com.tonihacks.qalam.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.local.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val apiClient: ApiClient
) : ViewModel() {

    sealed interface ConnectionStatus {
        data object Idle : ConnectionStatus
        data object Checking : ConnectionStatus
        data object Connected : ConnectionStatus
        data class Error(val message: String) : ConnectionStatus
    }

    private val _urlDraft = MutableStateFlow("")
    val urlDraft: StateFlow<String> = _urlDraft

    private val _connectionStatus = MutableStateFlow<ConnectionStatus>(ConnectionStatus.Idle)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus

    init {
        viewModelScope.launch { _urlDraft.value = preferencesRepository.baseUrl.first() }
    }

    fun onUrlChange(url: String) {
        _urlDraft.value = url
        _connectionStatus.value = ConnectionStatus.Idle
    }

    fun testConnection() {
        viewModelScope.launch {
            _connectionStatus.value = ConnectionStatus.Checking
            val result = apiClient.testConnection(_urlDraft.value)
            _connectionStatus.value = when {
                result.isSuccess -> ConnectionStatus.Connected
                else -> ConnectionStatus.Error(result.exceptionOrNull()?.message ?: "Connection failed")
            }
        }
    }

    fun saveUrl() {
        viewModelScope.launch { preferencesRepository.setBaseUrl(_urlDraft.value) }
    }
}