package kz.market.service.usecase

import kotlinx.coroutines.flow.SharedFlow
import kz.market.service.system.Installer
import kz.market.service.utils.UpdateStatus
import java.io.File
import javax.inject.Inject

class InstallUpdateUseCase @Inject constructor(
    private val installer: Installer,
) {
    operator fun invoke(apkFile: File, digest: String?) = installer.install(apkFile, digest)
}