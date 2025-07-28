package kz.market.service.system

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import dagger.hilt.android.EntryPointAccessors
import kz.market.di.InstallReceiverEntryPoint
import kz.market.service.utils.UpdateDefaults
import kz.market.service.utils.UpdateStatus
import kz.market.utils.SharedPrefs
import kotlin.jvm.java

class InstallResultReceiver() : BroadcastReceiver() {
    @SuppressLint("UnsafeIntentLaunch")
    override fun onReceive(context: Context, intent: Intent) {
        val applicationContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, InstallReceiverEntryPoint::class.java)
        val updateEventBus = entryPoint.updateEventBus()

        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

                if (confirmIntent != null) {
                    updateEventBus.emit(UpdateStatus.InstallPending)
                    confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirmIntent)
                    return
                } else {
                    updateEventBus.emit(UpdateStatus.Error("User action required but intent is null"))
                    Log.d("InstallResultReceiver", "User action required but intent is null")
                }
            }

            PackageInstaller.STATUS_SUCCESS -> {
                SharedPrefs.set(UpdateDefaults.KEY_UPDATE_INSTALLED, true)
            }
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                updateEventBus.emit(UpdateStatus.Error(message ?: "Install failed with status: $status"))
                Log.d("InstallResultReceiver", message ?: "Install failed with status: $status")
            }
        }
    }
}
