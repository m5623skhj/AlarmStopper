package com.alarmstopper

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.alarmstopper.di.appModule
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber

class AlarmStopperApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        FirebaseApp.initializeApp(this)

        startKoin {
            androidLogger()
            androidContext(this@AlarmStopperApplication)
            modules(appModule)
        }

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)

            val alarmChannel = NotificationChannel(
                "alarm_channel",
                "알람",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "알람 알림"
                setSound(null, null)
            }

            val syncChannel = NotificationChannel(
                "sync_channel",
                "동기화",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "디바이스 간 동기화"
            }

            notificationManager.createNotificationChannel(alarmChannel)
            notificationManager.createNotificationChannel(syncChannel)
        }
    }
}