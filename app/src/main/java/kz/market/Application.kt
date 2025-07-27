package kz.market

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import kz.market.utils.SharedPrefs

@HiltAndroidApp
class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        SharedPrefs.init(this)
    }
}