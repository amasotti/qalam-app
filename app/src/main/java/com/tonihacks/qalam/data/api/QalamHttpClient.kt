package com.tonihacks.qalam.data.api

import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


fun buildQalamHttpClient(): HttpClient = HttpClient(Android) {
    expectSuccess = true // throw Exceptions if > 2xx occurs

    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }

    install(Logging) {
        level = LogLevel.HEADERS
    }

    install(HttpTimeout) {
        connectTimeoutMillis = 5_000
        requestTimeoutMillis = 10_000
    }
}