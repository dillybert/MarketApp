package kz.market.service.utils

import kotlinx.coroutines.flow.MutableSharedFlow

object UpdateEventBus {
    private val _installStatus = MutableSharedFlow<UpdateStatus>(replay = 1)
    var installStatus: MutableSharedFlow<UpdateStatus> = _installStatus

    fun setInstallStatus(status: UpdateStatus) {
        _installStatus.tryEmit(status)
    }
}