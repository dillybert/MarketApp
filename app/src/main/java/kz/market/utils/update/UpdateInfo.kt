package kz.market.utils.update

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class UpdateInfo(
    val version: String,
    val downloadUrl: String,
    val changelog: String? = null
) : Parcelable