package com.alarmstopper.util

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

object GoogleSignInHelper {

    fun getClient(context: Context, webClientId: String): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(context, gso)
    }
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