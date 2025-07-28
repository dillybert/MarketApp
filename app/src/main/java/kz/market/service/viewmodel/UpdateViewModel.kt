package kz.market.service.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kz.market.service.model.UpdateMetaData
import kz.market.service.usecase.GetDownloadWorkRequestUUIDUseCase
import kz.market.service.usecase.GetUpdateMetaDataUseCase
import kz.market.service.usecase.InstallUpdateUseCase
import kz.market.service.usecase.ObserveDownloadProgressUseCase
import kz.market.service.utils.UpdateDefaults
import kz.market.service.utils.UpdateEventBus
import kz.market.service.utils.UpdateStatus
import kz.market.utils.SharedPrefs
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val getUpdateMetaDataUseCase: GetUpdateMetaDataUseCase,
    private val getDownloadWorkRequestUUIDUseCase: GetDownloadWorkRequestUUIDUseCase,
    private val observeDownloadProgressUseCase: ObserveDownloadProgressUseCase,
    private val installUpdateUseCase: InstallUpdateUseCase,
    val updateEventBus: UpdateEventBus
) : ViewModel() {
    var cachedMetaData: UpdateMetaData? = null
        private set

    private var lastUUID: UUID? = null

    init {
        SharedPrefs.get(UpdateDefaults.KEY_UPDATE_INSTALLED, false).run {
            if (this@run) {
                updateEventBus.emit(UpdateStatus.InstallSuccess)
                SharedPrefs.remove(UpdateDefaults.KEY_UPDATE_INSTALLED)
            } else {
                checkForUpdates()
            }
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            val updateMetaData = getUpdateMetaDataUseCase()
            if (updateMetaData.remoteVersionTag != updateMetaData.currentVersionTag) {
                updateEventBus.emit(UpdateStatus.UpdateAvailable(updateMetaData))
                cachedMetaData = updateMetaData
            }
        }
    }


    fun startUpdateProcess() {
        if (lastUUID != null) return

        viewModelScope.launch {
            val meta = cachedMetaData
            if (meta != null) {
                val uuid = getDownloadWorkRequestUUIDUseCase(meta)
                lastUUID = uuid
                observeDownloadProgress(uuid)
            }
        }
    }

    fun installUpdate(apkFile: File, digest: String?) {
        installUpdateUseCase(apkFile, digest)
    }

    fun clearUpdateStatus() {
        updateEventBus.emit(UpdateStatus.Idle)
    }

    private fun observeDownloadProgress(uuid: UUID) {
        observeDownloadProgressUseCase(uuid)
    }
}