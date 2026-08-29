@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.attendancemanager.app.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
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
import com.attendancemanager.app.ui.theme.AbsentRed
import com.attendancemanager.app.ui.theme.HolidayBlue
import com.attendancemanager.app.ui.theme.LeaveOrange
import com.attendancemanager.app.ui.theme.PresentGreen
import com.attendancemanager.app.ui.viewmodel.DashboardViewModel
import com.attendancemanager.app.ui.viewmodel.ViewModelFactory
import com.attendancemanager.app.util.DateUtils
import com.attendancemanager.app.util.WhatsAppShare

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as AttendanceApp
    val viewModel: DashboardViewModel = viewModel(factory = ViewModelFactory.from(app))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.refresh() }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Dashboard") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = DateUtils.displayDate(state.date),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (state.isHoliday) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = HolidayBlue.copy(alpha = 0.12f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Today is a Holiday", fontWeight = FontWeight.Bold, color = HolidayBlue)
                            Text("Attendance is not counted for holidays.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Active", state.totalActiveMembers.toString(), MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    StatCard("Present", state.presentCount.toString(), PresentGreen, Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard("Absent", state.absentCount.toString(), AbsentRed, Modifier.weight(1f))
                    StatCard("Leave", state.leaveCount.toString(), LeaveOrange, Modifier.weight(1f))
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Today's Absentees (${state.absentees.size})", style = MaterialTheme.typography.titleMedium)
                    Button(
                        onClick = {
                            WhatsAppShare.shareAbsentees(context, DateUtils.displayDate(state.date), state.absentees)
                        },
                        enabled = !state.isHoliday
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Share")
                    }
                }
            }

            if (!state.isHoliday) {
                if (state.absentees.isEmpty()) {
                    item {
                        Text(
                            "No absentees today.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                } else {
                    items(state.absentees) { member ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${member.memberId} - ${member.name}", fontWeight = FontWeight.Bold)
                                if (!member.mobile.isNullOrBlank()) {
                                    Text(member.mobile, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "All-time absentee entries: ${state.totalAbsenteeCountAllTime}",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = color, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
