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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kz.market.service.manager.UpdateManager
import kz.market.service.model.UpdateMetaData
import kz.market.service.system.DownloadWorker
import kz.market.service.utils.UpdateDefaults
import kz.market.service.utils.UpdateStatus
import java.io.File
import java.util.UUID
import javax.inject.Inject

class UpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val workManager: WorkManager,
    private val updateManager: UpdateManager
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

    fun observeDownload(uuid: UUID): Flow<UpdateStatus> = flow {
        workManager.getWorkInfoByIdLiveData(uuid)
            .asFlow()
            .collect { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt(UpdateDefaults.KEY_PROGRESS, 0)
                        val downloaded = workInfo.progress.getLong(UpdateDefaults.KEY_DOWNLOADED_BYTES, 0)
                        val contentSize = workInfo.progress.getLong(UpdateDefaults.KEY_TOTAL_BYTES, 0)

                        emit(UpdateStatus.Downloading(contentSize, progress, downloaded))
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val apkFile = workInfo.outputData.getString(UpdateDefaults.KEY_APK_FILE_PATH)

                        if (apkFile != null) {
                            emit(UpdateStatus.DownloadComplete(File(apkFile)))
                        } else {
                            emit(UpdateStatus.Error(message = "APK FILE is null"))
                        }
                    }

                    WorkInfo.State.BLOCKED,
                    WorkInfo.State.CANCELLED,
                    WorkInfo.State.FAILED -> {
                        val error = workInfo.outputData.getString(UpdateDefaults.KEY_ERROR)
                        error?.let {
                            emit(UpdateStatus.Error(message = it))
                        }
                    }
                }
            }
    }
}