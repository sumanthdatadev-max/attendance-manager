@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.attendancemanager.app.ui.screens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.attendancemanager.app.AttendanceApp
import com.attendancemanager.app.ui.navigation.Routes
import com.attendancemanager.app.ui.viewmodel.ReportViewModel
import com.attendancemanager.app.ui.viewmodel.ViewModelFactory
import com.attendancemanager.app.util.DateUtils

@Composable
fun ReportsScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as AttendanceApp
    val viewModel: ReportViewModel = viewModel(factory = ViewModelFactory.from(app))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Monthly Report") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.loadMonth(state.yearMonth.minusMonths(1)) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month")
                }
                Text(DateUtils.displayMonth(state.yearMonth), style = MaterialTheme.typography.titleMedium)
                IconButton(onClick = { viewModel.loadMonth(state.yearMonth.plusMonths(1)) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = "Next month")
                }
            }

            Spacer(Modifier.height(12.dp))

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        SummaryRow("Present", state.totalPresent)
                        SummaryRow("Absent", state.totalAbsent)
                        SummaryRow("Leave", state.totalLeave)
                        SummaryRow("Holidays", state.totalHolidays)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Overall Attendance: ${"%.1f".format(state.overallAttendancePercentage)}%",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text("Per-Member Breakdown", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(state.memberStats, key = { it.member.memberId }) { stat ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { navController.navigate(Routes.memberHistory(stat.member.memberId)) }
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("${stat.member.memberId} - ${stat.member.name}", fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Present: ${stat.present}  Absent: ${stat.absent}  Leave: ${stat.leave}  " +
                                        "Attendance: ${"%.1f".format(stat.percentage)}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value.toString(), fontWeight = FontWeight.Bold)
    }
}
