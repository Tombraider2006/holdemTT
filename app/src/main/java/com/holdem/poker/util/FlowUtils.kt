package com.holdem.poker.util

import kotlinx.coroutines.flow.SharingStarted

/**
 * Custom SharingStarted strategy that stops upstream flows 5 seconds after the last subscriber disconnects.
 * This optimizes resource consumption while preserving state for quick configuration changes.
 */
private const val STOP_TIMEOUT_MILLIS = 5_000L

val WhileUiSubscribed: SharingStarted = SharingStarted.WhileSubscribed(
    stopTimeoutMillis = STOP_TIMEOUT_MILLIS
)

