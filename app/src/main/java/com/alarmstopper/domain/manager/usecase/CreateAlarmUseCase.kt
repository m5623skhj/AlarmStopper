package com.alarmstopper.domain.usecase

import com.alarmstopper.data.model.Alarm
import com.alarmstopper.data.repository.AlarmRepository
import com.alarmstopper.domain.manager.AlarmScheduler

class CreateAlarmUseCase(
    private val alarmRepository: AlarmRepository,
    private val alarmScheduler: AlarmScheduler
) {
    suspend operator fun invoke(alarm: Alarm): Result<Unit> {
        return alarmRepository.createAlarm(alarm)
            .onSuccess {
                if (alarm.isEnabled) {
                    alarmScheduler.schedule(alarm)
                }
            }
            .map { }
    }
}