package kz.market.viewmodel

import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kz.market.data.receiver.InstallStateHolder
import kz.market.domain.UpdateStatus
import kz.market.domain.model.UpdateMetaData
import kz.market.domain.usecase.CheckUpdateUseCase
import kz.market.domain.usecase.InstallApkUseCase
import kz.market.domain.usecase.ObserveDownloadUseCase
import kz.market.domain.usecase.StartDownloadUseCase
import java.io.File
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val checkUpdatesUseCase: CheckUpdateUseCase,
    private val installUpdateUseCase: InstallApkUseCase,
    private val observeDownloadUseCase: ObserveDownloadUseCase,
    private val startDownloadUseCase: StartDownloadUseCase,
    installStateHolder: InstallStateHolder
) : ViewModel() {
    private val _updateStatus: MutableStateFlow<UpdateStatus> = MutableStateFlow(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus

    init {
        checkForUpdates()

        installStateHolder.status
            .onEach { _updateStatus.value = it }
            .launchIn(viewModelScope)
    }

    fun checkForUpdates() {
        viewModelScope.launch {
            val updateMetaData = checkUpdatesUseCase()
            if (updateMetaData.isUpdateAvailable) {
                _updateStatus.value = UpdateStatus.Available(updateMetaData)
            }
        }
    }

    fun startDownload(updateMetaData: UpdateMetaData) {
        val uuid = startDownloadUseCase(updateMetaData)
        observeDownloadUseCase(uuid)
            .onEach { _updateStatus.value = it }
            .launchIn(viewModelScope)
    }

    fun installUpdate(apkFile: File, digest: String) {
        installUpdateUseCase(apkFile, digest)
            .onEach { _updateStatus.value = it }
            .launchIn(viewModelScope)
    }

    fun resetUpdateStatus() {
        _updateStatus.value = UpdateStatus.Idle
    }

}