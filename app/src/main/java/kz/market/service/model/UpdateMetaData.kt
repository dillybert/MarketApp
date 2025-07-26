package kz.market.service.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateMetaData(
    val remoteVersionTag: String,
    val currentVersionTag: String,
    val apkUrl: String,
    val description: String
) : Parcelable {

    fun isEmpty() = this == EMPTY
    companion object {
        val EMPTY = UpdateMetaData(
            remoteVersionTag = "",
            currentVersionTag = "",
            apkUrl = "",
            description = ""
        )
    }
}