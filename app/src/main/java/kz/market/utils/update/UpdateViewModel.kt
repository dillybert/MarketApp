package kz.market.utils.update

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateManager: UpdateManager
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo

    var progress by mutableIntStateOf(0)
        private set

    var downloadingCanceled by mutableStateOf(false)
        private set

    var networkError by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            _updateInfo.value = updateManager.checkForUpdates()
        }
    }

    fun dismissDialog() {
        _updateInfo.value = null
    }

    fun startUpdateFlow(updateInfo: UpdateInfo) {
        val id = updateManager.startDownload(updateInfo)
        updateManager.observeDownloadProgress(
            downloadId = id,
            scope = viewModelScope,
            onProgress = { p ->
                progress = p
                networkError = false
                Log.d("UpdateViewModel", "Progress: $progress")
            },
            onSuccess = {},
            onFailure = {
                progress = 0
                downloadingCanceled = true
                Log.d("UpdateViewModel", "Download failed")
            },
            onNetworkError = {
                networkError = true
            },
            onCancelled = {
                progress = 0
                downloadingCanceled = true
                Log.d("UpdateViewModel", "Download cancelled")
            }
        )
    }

    override fun onCleared() {
        updateManager.cancelProgressTracking()
        super.onCleared()
    }
}
