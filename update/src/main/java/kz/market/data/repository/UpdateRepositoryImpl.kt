package kz.market.data.repository

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
import kotlinx.coroutines.flow.mapNotNull
import kz.market.data.installer.ApkInstallerImpl
import kz.market.data.remote.GitHubUpdateDataSource
import kz.market.data.worker.UpdateDownloadWorker
import kz.market.domain.UpdateStatus
import kz.market.domain.model.UpdateMetaData
import kz.market.domain.repository.UpdateRepository
import kz.market.data.config.UpdateDefaults
import java.io.File
import java.util.UUID
import javax.inject.Inject

class UpdateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val githubUpdateDataSource: GitHubUpdateDataSource,
    private val workManager: WorkManager,
    private val apkInstaller: ApkInstallerImpl
): UpdateRepository {
    override suspend fun fetchUpdateMetaData(): UpdateMetaData =
        githubUpdateDataSource.fetchUpdateMetaData()

    override fun observeDownloadProgress(uuid: UUID): Flow<UpdateStatus> {
        return workManager.getWorkInfoByIdLiveData(uuid)
            .asFlow()
            .mapNotNull { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED,
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getInt(UpdateDefaults.KEY_PROGRESS, 0)
                        val downloaded = workInfo.progress.getLong(UpdateDefaults.KEY_DOWNLOADED_BYTES, 0)
                        val contentSize = workInfo.progress.getLong(UpdateDefaults.KEY_TOTAL_BYTES, 0)

                        UpdateStatus.Downloading(
                            totalBytes = contentSize,
                            progress = progress,
                            downloadedBytes = downloaded
                        )
                    }

                    WorkInfo.State.SUCCEEDED -> {
                        val apkFile = workInfo.outputData.getString(UpdateDefaults.KEY_APK_FILE_PATH)
                        val apkDigest = workInfo.outputData.getString(UpdateDefaults.KEY_APK_DIGEST)

                        if (apkFile != null && apkDigest != null) {
                            UpdateStatus.Downloaded(
                                File(apkFile),
                                apkDigest
                            )
                        } else {
                            UpdateStatus.Error(message = "APK FILE OR DIGEST is null")
                        }
                    }

                    WorkInfo.State.CANCELLED,
                    WorkInfo.State.FAILED,
                    WorkInfo.State.BLOCKED -> {
                        val error = workInfo.outputData.getString(UpdateDefaults.KEY_ERROR)

                        UpdateStatus.Error(error ?: "Unknown error")
                    }
                }
            }
    }

    override fun enqueueDownload(updateMetaData: UpdateMetaData): UUID {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<UpdateDownloadWorker>()
            .setInputData(
                workDataOf(
                    UpdateDefaults.KEY_APK_URL to updateMetaData.apkUrl,
                    UpdateDefaults.KEY_APK_DIGEST to updateMetaData.apkDigest
                )
            )
            .setConstraints(constraints = constraints)
            .build()

        workManager.enqueueUniqueWork(
            UpdateDefaults.UNIQUE_WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest
        )

        return workRequest.id
    }

    override fun installApk(apkFile: File, digest: String): Result<Unit> {
        return apkInstaller.installApk(apkFile, digest)
    }
}