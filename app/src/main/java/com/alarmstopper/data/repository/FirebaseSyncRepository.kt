package com.alarmstopper.data.repository

import com.alarmstopper.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.UUID

interface SyncRepository {
    suspend fun syncAlarm(alarm: Alarm): Result<Unit>
    suspend fun deleteAlarmRemote(alarmId: String): Result<Unit>
    fun observeAlarms(): Flow<List<Alarm>>
    suspend fun broadcastStopAll(): Result<Unit>
    suspend fun broadcastStopInRange(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int
    ): Result<Unit>
    fun listenToRemoteCommands(): Flow<SyncCommand>
}

class FirebaseSyncRepository(
    private val database: FirebaseDatabase,
    private val auth: FirebaseAuth,
    private val deviceId: String
) : SyncRepository {

    private val TAG = "FirebaseSyncRepository"

    private fun getUserId(): String {
        return auth.currentUser?.uid ?: throw IllegalStateException("User not logged in")
    }

    private fun getUserRef(): DatabaseReference {
        return database.reference.child("alarmstopper/users/${getUserId()}")
    }

    override suspend fun syncAlarm(alarm: Alarm): Result<Unit> {
        return try {
            val alarmRef = getUserRef().child("alarms/${alarm.id}")
            alarmRef.setValue(alarm).await()
            Timber.d("Alarm synced: ${alarm.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync alarm")
            Result.failure(e)
        }
    }

    override suspend fun deleteAlarmRemote(alarmId: String): Result<Unit> {
        return try {
            val alarmRef = getUserRef().child("alarms/$alarmId")
            alarmRef.removeValue().await()

            val command = SyncCommand(
                id = UUID.randomUUID().toString(),
                type = CommandType.ALARM_DELETED,
                deviceId = deviceId
            )
            sendCommand(command)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeAlarms(): Flow<List<Alarm>> = callbackFlow {
        val alarmsRef = getUserRef().child("alarms")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alarms = mutableListOf<Alarm>()

                for (childSnapshot in snapshot.children) {
                    try {
                        val alarm = childSnapshot.getValue(Alarm::class.java)
                        alarm?.let { alarms.add(it) }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to parse alarm")
                    }
                }

                trySend(alarms)
                Timber.d("Alarms updated: ${alarms.size} alarms")
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Failed to observe alarms")
                close(error.toException())
            }
        }

        alarmsRef.addValueEventListener(listener)

        awaitClose {
            alarmsRef.removeEventListener(listener)
        }
    }

    override suspend fun broadcastStopAll(): Result<Unit> {
        return try {
            val command = SyncCommand(
                id = UUID.randomUUID().toString(),
                type = CommandType.STOP_ALL,
                deviceId = deviceId
            )

            sendCommand(command)
            Timber.d("Broadcasted STOP_ALL command")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to broadcast STOP_ALL")
            Result.failure(e)
        }
    }

    override suspend fun broadcastStopInRange(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int
    ): Result<Unit> {
        return try {
            val command = SyncCommand(
                id = UUID.randomUUID().toString(),
                type = CommandType.STOP_IN_RANGE,
                deviceId = deviceId,
                timeRange = TimeRangeData(startHour, startMinute, endHour, endMinute)
            )

            sendCommand(command)
            Timber.d("Broadcasted STOP_IN_RANGE command")
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun sendCommand(command: SyncCommand) {
        val commandRef = getUserRef().child("commands/${command.id}")
        commandRef.setValue(command).await()
    }

    override fun listenToRemoteCommands(): Flow<SyncCommand> = callbackFlow {
        val commandsRef = getUserRef().child("commands")

        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                try {
                    val command = snapshot.getValue(SyncCommand::class.java)

                    if (command != null &&
                        command.deviceId != deviceId &&
                        !command.executed) {

                        trySend(command)
                        Timber.d("Received command: ${command.type} from ${command.deviceId}")

                        snapshot.ref.child("executed").setValue(true)
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to parse command")
                }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Failed to listen to commands")
                close(error.toException())
            }
        }

        commandsRef.addChildEventListener(listener)

        awaitClose {
            commandsRef.removeEventListener(listener)
        }
    }

    suspend fun updateDeviceStatus(status: String = "online") {
        try {
            val deviceRef = getUserRef().child("devices/$deviceId")
            deviceRef.child("lastSeen").setValue(ServerValue.TIMESTAMP)
            deviceRef.child("status").setValue(status)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update device status")
        }
    }

    fun setupPresence() {
        val deviceRef = getUserRef().child("devices/$deviceId")

        deviceRef.child("status").onDisconnect().setValue("offline")
        deviceRef.child("lastSeen").onDisconnect().setValue(ServerValue.TIMESTAMP)

        database.reference.child(".info/connected").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    deviceRef.child("status").setValue("online")
                    deviceRef.child("lastSeen").setValue(ServerValue.TIMESTAMP)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e(error.toException(), "Failed to setup presence")
            }
        })
    }
}