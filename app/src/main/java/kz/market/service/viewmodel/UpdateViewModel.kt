package kz.market.service.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kz.market.service.model.UpdateMetaData
import kz.market.service.usecase.GetDownloadWorkRequestUUIDUseCase
import kz.market.service.usecase.GetUpdateMetaDataUseCase
import kz.market.service.usecase.InstallUpdateUseCase
import kz.market.service.usecase.ObserveDownloadProgressUseCase
import kz.market.service.utils.UpdateStatus
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val getUpdateMetaDataUseCase: GetUpdateMetaDataUseCase,
    private val getDownloadWorkRequestUUIDUseCase: GetDownloadWorkRequestUUIDUseCase,
    private val observeDownloadProgressUseCase: ObserveDownloadProgressUseCase,
    private val installUpdateUseCase: InstallUpdateUseCase
) : ViewModel() {
    var cachedMetaData: UpdateMetaData? = null
        private set
    private var lastUUID: UUID? = null

    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()

    init {
        checkForUpdates()
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            val updateMetaData = getUpdateMetaDataUseCase()
            if (updateMetaData.remoteVersionTag != updateMetaData.currentVersionTag) {
                _updateStatus.value = UpdateStatus.UpdateAvailable(updateMetaData)
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

    fun installUpdate(apkFile: File) {
        installUpdateUseCase.install(apkFile)

        viewModelScope.launch {
            installUpdateUseCase.observeStatus().collect { status ->
                _updateStatus.value = status
            }
        }
    }

    fun clearUpdateStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

    private fun observeDownloadProgress(uuid: UUID) {
        viewModelScope.launch {
            observeDownloadProgressUseCase(uuid)
                .collect { status ->
                    _updateStatus.value = status
                }
        }
    }
}