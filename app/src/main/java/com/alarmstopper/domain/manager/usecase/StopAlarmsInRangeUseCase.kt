package com.alarmstopper.domain.usecase

import com.alarmstopper.data.model.TimeRange
import com.alarmstopper.data.repository.AlarmRepository
import com.alarmstopper.data.repository.SyncRepository
import com.alarmstopper.domain.manager.AlarmScheduler
import kotlinx.coroutines.flow.first
import timber.log.Timber

class StopAlarmsInRangeUseCase(
    private val alarmRepository: AlarmRepository,
    private val syncRepository: SyncRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int
    ): Result<Unit> {
        return try {
            val timeRange = TimeRange(
                name = "임시",
                startHour = startHour,
                startMinute = startMinute,
                endHour = endHour,
                endMinute = endMinute
            )

            val alarms = alarmRepository.getAllAlarms().first()

            alarms.forEach { alarm ->
                if (timeRange.contains(alarm.hour, alarm.minute)) {
                    if (alarm.isOneTime) {
                        alarmRepository.deleteAlarm(alarm.id)
                        alarmScheduler.cancel(alarm)
                        Timber.d("Deleted alarm in range: ${alarm.id}")
                    } else {
                        alarmRepository.updateAlarm(alarm.copy(isEnabled = false))
                        alarmScheduler.cancel(alarm)
                        Timber.d("Disabled alarm in range: ${alarm.id}")
                    }
                }
            }

            syncRepository.broadcastStopInRange(startHour, startMinute, endHour, endMinute)

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop alarms in range")
            Result.failure(e)
        }
    }
}