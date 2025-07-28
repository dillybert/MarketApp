package kz.market.service.utils

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<UpdateStatus>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val events: SharedFlow<UpdateStatus> = _events

    fun emit(event: UpdateStatus) {
        val eventStatus = _events.tryEmit(event)

        if (!eventStatus) {
            Log.e("UpdateEventBus", "Failed to emit event: $event")
        }
    }
}