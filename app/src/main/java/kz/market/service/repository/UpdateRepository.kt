package kz.market.service.repository

import android.content.Context
import androidx.lifecycle.asFlow
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kz.market.service.manager.UpdateManager
import kz.market.service.model.UpdateMetaData
import kz.market.service.system.DownloadWorker
import kz.market.service.utils.UpdateDefaults
import kz.market.service.utils.UpdateEventBus
import kz.market.service.utils.UpdateStatus
import java.io.File
import java.util.UUID
import javax.inject.Inject

class UpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val updateManager: UpdateManager,
    private val updateEventBus: UpdateEventBus
) {
    suspend fun getUpdateMetaData() = updateManager.getUpdateMetaData()

    fun getDownloadWorkRequestUUID(updateMetaData: UpdateMetaData): UUID {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(workDataOf(UpdateDefaults.KEY_APK_URL to updateMetaData.apkUrl))
            .setConstraints(constraints = constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            UpdateDefaults.UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest
        )

        return workRequest.id
    }

    fun observeDownload(uuid: UUID) {
        workManager.getWorkInfoByIdLiveData(uuid)
            .asFlow()
            .onEach { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt(UpdateDefaults.KEY_PROGRESS, 0)
                        val downloaded = workInfo.progress.getLong(UpdateDefaults.KEY_DOWNLOADED_BYTES, 0)
                        val contentSize = workInfo.progress.getLong(UpdateDefaults.KEY_TOTAL_BYTES, 0)

                        updateEventBus.emit(UpdateStatus.Downloading(contentSize, progress, downloaded))
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val apkFile = workInfo.outputData.getString(UpdateDefaults.KEY_APK_FILE_PATH)

                        if (apkFile != null) {
                            updateEventBus.emit(UpdateStatus.DownloadComplete(File(apkFile)))
                        } else {
                            updateEventBus.emit(UpdateStatus.Error(message = "APK FILE is null"))
                        }
                    }

                    WorkInfo.State.BLOCKED,
                    WorkInfo.State.CANCELLED,
                    WorkInfo.State.FAILED -> {
                        val error = workInfo.outputData.getString(UpdateDefaults.KEY_ERROR)
                        error?.let {
                            updateEventBus.emit(UpdateStatus.Error(message = it))
                        }
                    }
                }
            }.launchIn(CoroutineScope(Dispatchers.IO))
    }
}