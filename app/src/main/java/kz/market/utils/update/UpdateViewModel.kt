package kz.market.utils.update

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

    init {
        viewModelScope.launch {
            _updateInfo.value = updateManager.checkForUpdates()
        }
    }

    fun confirmUpdate() {
        _updateInfo.value?.let { updateManager.startDownload(it) }
    }

    fun dismissDialog() {
        _updateInfo.value = null
    }
}
