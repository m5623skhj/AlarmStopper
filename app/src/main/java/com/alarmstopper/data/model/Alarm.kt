package com.alarmstopper.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val isOneTime: Boolean = false,
    val repeatDays: List<Int> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val deviceId: String = "",
    val userId: String = ""
)