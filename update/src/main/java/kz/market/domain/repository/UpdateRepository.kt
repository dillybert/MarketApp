package kz.market.domain.repository

import kotlinx.coroutines.flow.Flow
import kz.market.domain.UpdateStatus
import kz.market.domain.model.UpdateMetaData
import java.io.File
import java.util.UUID

interface UpdateRepository {
    suspend fun fetchUpdateMetaData(): UpdateMetaData
    fun observeDownloadProgress(uuid: UUID): Flow<UpdateStatus>
    fun enqueueDownload(updateMetaData: UpdateMetaData): UUID
    fun installApk(apkFile: File, digest: String): Result<Unit>
}