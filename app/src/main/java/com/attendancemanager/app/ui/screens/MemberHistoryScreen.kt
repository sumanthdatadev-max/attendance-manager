package com.attendancemanager.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.attendancemanager.app.AttendanceApp
import com.attendancemanager.app.data.entity.AttendanceStatus
import com.attendancemanager.app.data.entity.Member
import com.attendancemanager.app.ui.theme.AbsentRed
import com.attendancemanager.app.ui.theme.LeaveOrange
import com.attendancemanager.app.ui.theme.PresentGreen
import com.attendancemanager.app.ui.viewmodel.MemberViewModel
import com.attendancemanager.app.ui.viewmodel.ViewModelFactory
import com.attendancemanager.app.util.DateUtils

@Composable
fun MemberHistoryScreen(navController: NavController, memberId: String) {
    val context = LocalContext.current
    val app = context.applicationContext as AttendanceApp
    val viewModel: MemberViewModel = viewModel(factory = ViewModelFactory.from(app))
    val records by viewModel.memberHistory(memberId).collectAsStateWithLifecycle()
    var member by remember { mutableStateOf<Member?>(null) }

    LaunchedEffect(memberId) {
        member = viewModel.getMember(memberId)
    }

    val present = records.count { it.status == AttendanceStatus.PRESENT }
    val absent = records.count { it.status == AttendanceStatus.ABSENT }
    val leave = records.count { it.status == AttendanceStatus.LEAVE }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(member?.name ?: memberId) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Member ID: $memberId", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Present: $present", color = PresentGreen, fontWeight = FontWeight.Bold)
                Text("Absent: $absent", color = AbsentRed, fontWeight = FontWeight.Bold)
                Text("Leave: $leave", color = LeaveOrange, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))

            if (records.isEmpty()) {
                Text("No attendance history yet.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(records) { record ->
                        val color = when (record.status) {
                            AttendanceStatus.PRESENT -> PresentGreen
                            AttendanceStatus.ABSENT -> AbsentRed
                            AttendanceStatus.LEAVE -> LeaveOrange
                        }
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(DateUtils.displayDate(record.date))
                                Text(record.status.name, color = color, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
