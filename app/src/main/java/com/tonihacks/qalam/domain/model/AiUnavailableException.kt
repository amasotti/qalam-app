package com.tonihacks.qalam.domain.model

/** Raised when an AI endpoint returns 503 (OPENROUTER_API_KEY not configured on the backend). */
class AiUnavailableException : Exception("AI not configured")
