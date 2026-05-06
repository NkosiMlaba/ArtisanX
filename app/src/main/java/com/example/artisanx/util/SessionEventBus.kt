package com.example.artisanx.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEventBus {
    private val _sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionExpired = _sessionExpired.asSharedFlow()

    private val _sessionChanged = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sessionChanged = _sessionChanged.asSharedFlow()

    fun emitExpired() {
        _sessionExpired.tryEmit(Unit)
    }

    /** Emitted on login success and logout. Listeners should restart any
     *  user-scoped subscriptions/state. */
    fun emitSessionChanged() {
        _sessionChanged.tryEmit(Unit)
    }
}
