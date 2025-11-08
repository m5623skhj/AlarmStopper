package com.alarmstopper.service

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.alarmstopper.R
import com.alarmstopper.data.model.CommandType
import com.alarmstopper.data.repository.FirebaseSyncRepository
import com.alarmstopper.domain.usecase.StopAllAlarmsUseCase
import com.alarmstopper.domain.usecase.StopAlarmsInRangeUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import timber.log.Timber

class SyncService : Service() {

    private val TAG = "SyncService"
    private val serviceScope = CoroutineScope(Dispatchers.IO + Job())

    private val syncRepository: FirebaseSyncRepository by inject()
    private val stopAllAlarmsUseCase: StopAllAlarmsUseCase by inject()
    private val stopAlarmsInRangeUseCase: StopAlarmsInRangeUseCase by inject()

    override fun onCreate() {
        super.onCreate()
        Timber.d("$TAG created")

        startForeground(2, createNotification())
        startListeningToCommands()
        syncRepository.setupPresence()
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, "sync_channel")
            .setContentTitle("AlarmStopper")
            .setContentText("디바이스 동기화 중")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startListeningToCommands() {
        serviceScope.launch {
            syncRepository.listenToRemoteCommands().collect { command ->
                Timber.d("Executing command: ${command.type}")

                when (command.type) {
                    CommandType.STOP_ALL -> {
                        stopAllAlarmsUseCase()
                        Timber.d("Executed STOP_ALL command")
                    }

                    CommandType.STOP_IN_RANGE -> {
                        command.timeRange?.let { range ->
                            stopAlarmsInRangeUseCase(
                                range.startHour,
                                range.startMinute,
                                range.endHour,
                                range.endMinute
                            )
                            Timber.d("Executed STOP_IN_RANGE command")
                        }
                    }

                    CommandType.ALARM_DELETED -> {
                        Timber.d("Alarm deleted notification received")
                    }

                    CommandType.ALARM_UPDATED -> {
                        Timber.d("Alarm updated notification received")
                    }
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Timber.d("$TAG destroyed")

        serviceScope.launch {
            syncRepository.updateDeviceStatus("offline")
        }
    }
}