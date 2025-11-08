package com.alarmstopper.domain.usecase

import com.alarmstopper.data.repository.AlarmRepository
import com.alarmstopper.data.repository.SyncRepository
import com.alarmstopper.domain.manager.AlarmScheduler
import kotlinx.coroutines.flow.first
import timber.log.Timber

class StopAllAlarmsUseCase(
    private val alarmRepository: AlarmRepository,
    private val syncRepository: SyncRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(): Result<Unit> {
        return try {
            val alarms = alarmRepository.getAllAlarms().first()

            alarms.forEach { alarm ->
                if (alarm.isOneTime) {
                    alarmRepository.deleteAlarm(alarm.id)
                    alarmScheduler.cancel(alarm)
                    Timber.d("Deleted one-time alarm: ${alarm.id}")
                } else {
                    alarmRepository.updateAlarm(alarm.copy(isEnabled = false))
                    alarmScheduler.cancel(alarm)
                    Timber.d("Disabled repeat alarm: ${alarm.id}")
                }
            }

            syncRepository.broadcastStopAll()

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop all alarms")
            Result.failure(e)
        }
    }
}