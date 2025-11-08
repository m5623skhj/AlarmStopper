package com.alarmstopper.presentation.alarmlist

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alarmstopper.data.model.Alarm
import com.alarmstopper.presentation.common.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmListScreen(
    isAnonymous: Boolean,
    onUpgradeClick: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: AlarmListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddAlarmDialog by remember { mutableStateOf(false) }
    var showTimeRangeDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AlarmStopper") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "설정")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddAlarmDialog = true }
            ) {
                Icon(Icons.Default.Add, "알람 추가")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isAnonymous) {
                UpgradeBanner(onClick = onUpgradeClick)
            }

            QuickActionButtons(
                onStopAll = { viewModel.stopAllAlarms() },
                onStopInRange = { showTimeRangeDialog = true }
            )

            if (uiState.alarms.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.alarms,
                        key = { it.id }
                    ) { alarm ->
                        AlarmItem(
                            alarm = alarm,
                            onToggle = { viewModel.toggleAlarm(alarm.id) },
                            onDelete = { viewModel.deleteAlarm(alarm.id) }
                        )
                    }
                }
            }
        }

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }

    if (showAddAlarmDialog) {
        AddAlarmDialog(
            onDismiss = { showAddAlarmDialog = false },
            onConfirm = { hour, minute, label, isOneTime ->
                viewModel.createAlarm(hour, minute, label, isOneTime)
                showAddAlarmDialog = false
            }
        )
    }

    if (showTimeRangeDialog) {
        TimeRangeDialog(
            onDismiss = { showTimeRangeDialog = false },
            onConfirm = { startHour, startMinute, endHour, endMinute ->
                viewModel.stopAlarmsInRange(startHour, startMinute, endHour, endMinute)
                showTimeRangeDialog = false
            }
        )
    }
}