package com.attendancemanager.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.attendancemanager.app.AttendanceApp
import com.attendancemanager.app.data.entity.AttendanceStatus
import com.attendancemanager.app.ui.theme.AbsentRed
import com.attendancemanager.app.ui.theme.HolidayBlue
import com.attendancemanager.app.ui.theme.LeaveOrange
import com.attendancemanager.app.ui.theme.PresentGreen
import com.attendancemanager.app.ui.viewmodel.AttendanceViewModel
import com.attendancemanager.app.ui.viewmodel.ViewModelFactory
import com.attendancemanager.app.util.DateUtils

@Composable
fun AttendanceScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as AttendanceApp
    val viewModel: AttendanceViewModel = viewModel(factory = ViewModelFactory.from(app))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.saveMessage) {
        state.saveMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSaveMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Daily Attendance") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            DatePickerField(
                label = "Date",
                isoDate = state.date,
                onDateSelected = { viewModel.loadForDate(it) }
            )

            Spacer(Modifier.height(12.dp))

            when {
                state.isHoliday -> {
                    Card(colors = CardDefaults.cardColors(containerColor = HolidayBlue.copy(alpha = 0.12f))) {
                        Column(Modifier.padding(16.dp)) {
                            Text("This date is marked as a Holiday", fontWeight = FontWeight.Bold, color = HolidayBlue)
                            Text(
                                "No attendance is recorded on holidays. Manage holidays from the More tab.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.rows.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No members are eligible on this date.")
                    }
                }
                else -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${state.rows.size} members", style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(onClick = { viewModel.markAllPresent() }) {
                            Text("Mark All Present")
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.rows, key = { it.member.memberId }) { row ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("${row.member.memberId} - ${row.member.name}", fontWeight = FontWeight.Bold)
                                    Spacer(Modifier.height(8.dp))
                                    StatusSelector(
                                        selected = row.status,
                                        onSelect = { viewModel.setStatus(row.member.memberId, it) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.saveAttendance() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Save Attendance")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusSelector(selected: AttendanceStatus, onSelect: (AttendanceStatus) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        StatusChip("Present", AttendanceStatus.PRESENT, selected, PresentGreen, onSelect)
        StatusChip("Absent", AttendanceStatus.ABSENT, selected, AbsentRed, onSelect)
        StatusChip("Leave", AttendanceStatus.LEAVE, selected, LeaveOrange, onSelect)
    }
}

@Composable
private fun StatusChip(
    label: String,
    status: AttendanceStatus,
    selected: AttendanceStatus,
    color: androidx.compose.ui.graphics.Color,
    onSelect: (AttendanceStatus) -> Unit
) {
    val isSelected = status == selected
    FilterChip(
        selected = isSelected,
        onClick = { onSelect(status) },
        label = { Text(label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color
        )
    )
}
