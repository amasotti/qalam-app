package com.tonihacks.qalam.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonihacks.qalam.data.api.ApiClient
import com.tonihacks.qalam.data.local.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val apiClient: ApiClient
) : ViewModel() {

    sealed interface ConnectionState {
        data object Unknown : ConnectionState
        data object Online : ConnectionState
        data object Offline : ConnectionState
    }

    val baseUrl: StateFlow<String> = preferencesRepository.baseUrl.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(3_000),
        initialValue = PreferencesRepository.DEFAULT_URL,
    )

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Unknown)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    fun ping() {
        viewModelScope.launch {
            val result = apiClient.testConnection(baseUrl.value)
            _connectionState.value = if (result.isSuccess) ConnectionState.Online else ConnectionState.Offline
        }
    }
}