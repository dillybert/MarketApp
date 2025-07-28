package kz.market.service.system

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log
import kz.market.service.utils.UpdateDefaults
import kz.market.utils.SharedPrefs

class InstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        when (status) {
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
