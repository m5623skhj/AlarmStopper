package com.alarmstopper.presentation.alarmlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alarmstopper.data.model.Alarm
import com.alarmstopper.data.repository.AlarmRepository
import com.alarmstopper.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth

data class AlarmListUiState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AlarmListViewModel(
    private val alarmRepository: AlarmRepository,
    private val createAlarmUseCase: CreateAlarmUseCase,
    private val stopAllAlarmsUseCase: StopAllAlarmsUseCase,
    private val stopAlarmsInRangeUseCase: StopAlarmsInRangeUseCase,
    private val auth: FirebaseAuth,
    private val deviceId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(AlarmListUiState())
    val uiState: StateFlow<AlarmListUiState> = _uiState.asStateFlow()

    init {
        loadAlarms()
    }

    private fun loadAlarms() {
        viewModelScope.launch {
            alarmRepository.getAllAlarms()
                .catch { error ->
                    _uiState.update { it.copy(error = error.message) }
                }
                .collect { alarms ->
                    _uiState.update { it.copy(alarms = alarms, isLoading = false) }
                }
        }
    }

    fun createAlarm(hour: Int, minute: Int, label: String, isOneTime: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val alarm = Alarm(
                hour = hour,
                minute = minute,
                label = label,
                isOneTime = isOneTime,
                userId = auth.currentUser?.uid ?: "",
                deviceId = deviceId
            )

            createAlarmUseCase(alarm)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun toggleAlarm(alarmId: String) {
        viewModelScope.launch {
            val alarm = _uiState.value.alarms.find { it.id == alarmId } ?: return@launch
            alarmRepository.toggleAlarm(alarmId, !alarm.isEnabled)
        }
    }

    fun deleteAlarm(alarmId: String) {
        viewModelScope.launch {
            alarmRepository.deleteAlarm(alarmId)
        }
    }

    fun stopAllAlarms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            stopAllAlarmsUseCase()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun stopAlarmsInRange(
        startHour: Int, startMinute: Int,
        endHour: Int, endMinute: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            stopAlarmsInRangeUseCase(startHour, startMinute, endHour, endMinute)
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}