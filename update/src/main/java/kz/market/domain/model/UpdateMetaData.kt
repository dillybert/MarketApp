package kz.market.domain.model

data class UpdateMetaData(
    val currentVersion: String,
    val remoteVersion: String,
    val apkUrl: String,
    val apkDigest: String,
    val changelog: String? = null,
) {
    val isEmpty: Boolean
        get() = this == EMPTY

    val isUpdateAvailable: Boolean
        get() = remoteVersion != currentVersion

    companion object {
        val EMPTY = UpdateMetaData(
            currentVersion = "",
            remoteVersion = "",
            apkUrl = "",
            apkDigest = ""
        )
    }
}