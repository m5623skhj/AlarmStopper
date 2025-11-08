package com.alarmstopper.data.model

data class SyncCommand(
    val id: String = "",
    val type: CommandType,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceId: String,
    val timeRange: TimeRangeData? = null,
    val executed: Boolean = false
)

enum class CommandType {
    STOP_ALL,
    STOP_IN_RANGE,
    ALARM_UPDATED,
    ALARM_DELETED
}

data class TimeRangeData(
    val startHour: Int,
    val startMinute: Int,
    val endHour: Int,
    val endMinute: Int
)