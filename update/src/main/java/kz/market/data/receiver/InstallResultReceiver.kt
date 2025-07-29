package kz.market.data.receiver

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import dagger.hilt.android.EntryPointAccessors
import kz.market.di.InstallReceiverEntryPoint
import kz.market.domain.UpdateStatus

class InstallResultReceiver() : BroadcastReceiver() {
    @SuppressLint("UnsafeIntentLaunch")
    override fun onReceive(context: Context, intent: Intent) {
        val applicationContext = context.applicationContext
        val entryPoint = EntryPointAccessors.fromApplication(applicationContext, InstallReceiverEntryPoint::class.java)
        val stateHolder = entryPoint.installStateHolder()

        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_SUCCESS -> {
                stateHolder.emit(UpdateStatus.Installed)
            }
            PackageInstaller.STATUS_FAILURE_STORAGE -> {
                stateHolder.emit(UpdateStatus.Error("Недостаточно памяти"))
            }
            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                stateHolder.emit(UpdateStatus.Error("Пользователь отменил установку"))
            }
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                confirmIntent?.let {
                    stateHolder.emit(UpdateStatus.InstallPending)
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(it)
                } ?: stateHolder.emit(UpdateStatus.Error("Нужно подтверждение, но intent = null"))
            }
            else -> {
                stateHolder.emit(UpdateStatus.Error("Ошибка установки: $message"))
            }
        }
    }
}