package com.tonihacks.qalam.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val BASE_URL_KEY = stringPreferencesKey("base_url")
        const val DEFAULT_URL = "http://100.118.111.2"
    }

    val baseUrl: Flow<String> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs -> prefs[BASE_URL_KEY] ?: DEFAULT_URL }

    suspend fun setBaseUrl(url: String) {
        check(url.isNotBlank()) { "Base URL cannot be blank" }
        dataStore.edit { prefs -> prefs[BASE_URL_KEY] = url }
    }

}