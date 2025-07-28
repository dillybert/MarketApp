package kz.market.service.system

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import kz.market.service.utils.UpdateDefaults
import kz.market.service.utils.UpdateEventBus
import kz.market.service.utils.UpdateStatus
import kz.market.utils.SharedPrefs

class InstallResultReceiver : BroadcastReceiver() {
    @SuppressLint("UnsafeIntentLaunch")
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

                if (confirmIntent != null) {
                    UpdateEventBus.setInstallStatus(UpdateStatus.InstallPending)
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmIntent)
                    return
                } else {
                    Log.d("InstallResultReceiver", "User action required but intent is null")
                }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                SharedPrefs.init(context.applicationContext)
                SharedPrefs.set(UpdateDefaults.KEY_UPDATE_INSTALLED, true)
            }
            else -> {
                Log.d("InstallResultReceiver", "Install failed with status: $status")
            }
        }
    }
}
