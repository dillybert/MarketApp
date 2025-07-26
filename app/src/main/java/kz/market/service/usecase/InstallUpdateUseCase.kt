package kz.market.service.usecase

import kotlinx.coroutines.flow.SharedFlow
import kz.market.service.system.Installer
import kz.market.service.utils.UpdateStatus
import java.io.File
import javax.inject.Inject

class InstallUpdateUseCase @Inject constructor(
    private val installer: Installer,
) {
    fun install(apkFile: File) = installer.install(apkFile)

    fun observeStatus(): SharedFlow<UpdateStatus> = installer.installStatus
}