package com.alarmstopper.data.repository

import com.alarmstopper.data.local.AlarmDao
import com.alarmstopper.data.model.Alarm
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

interface AlarmRepository {
    suspend fun createAlarm(alarm: Alarm): Result<Alarm>
    suspend fun updateAlarm(alarm: Alarm): Result<Unit>
    suspend fun deleteAlarm(alarmId: String): Result<Unit>
    suspend fun getAlarm(alarmId: String): Result<Alarm?>
    fun getAllAlarms(): Flow<List<Alarm>>
    suspend fun toggleAlarm(alarmId: String, enabled: Boolean): Result<Unit>
}

class AlarmRepositoryImpl(
    private val localDataSource: AlarmDao,
    private val remoteDataSource: FirebaseSyncRepository
) : AlarmRepository {

    override suspend fun createAlarm(alarm: Alarm): Result<Alarm> {
        return try {
            localDataSource.insertAlarm(alarm)
            remoteDataSource.syncAlarm(alarm)
            Timber.d("Alarm created: ${alarm.id}")
            Result.success(alarm)
        } catch (e: Exception) {
            Timber.e(e, "Failed to create alarm")
            Result.failure(e)
        }
    }

    override suspend fun updateAlarm(alarm: Alarm): Result<Unit> {
        return try {
            localDataSource.updateAlarm(alarm)
            remoteDataSource.syncAlarm(alarm)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAlarm(alarmId: String): Result<Unit> {
        return try {
            localDataSource.deleteAlarm(alarmId)
            remoteDataSource.deleteAlarmRemote(alarmId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAlarm(alarmId: String): Result<Alarm?> {
        return try {
            val alarm = localDataSource.getAlarmById(alarmId)
            Result.success(alarm)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAllAlarms(): Flow<List<Alarm>> {
        return localDataSource.getAllAlarms()
    }

    override suspend fun toggleAlarm(alarmId: String, enabled: Boolean): Result<Unit> {
        return try {
            val alarm = localDataSource.getAlarmById(alarmId)
            alarm?.let {
                val updated = it.copy(isEnabled = enabled)
                localDataSource.updateAlarm(updated)
                remoteDataSource.syncAlarm(updated)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}