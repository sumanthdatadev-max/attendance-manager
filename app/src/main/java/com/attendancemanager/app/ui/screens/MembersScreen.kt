@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.attendancemanager.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
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
import com.attendancemanager.app.ui.navigation.Routes
import com.attendancemanager.app.ui.viewmodel.MemberViewModel
import com.attendancemanager.app.ui.viewmodel.ViewModelFactory

@Composable
fun MembersScreen(navController: NavController) {
    val context = LocalContext.current
    val app = context.applicationContext as AttendanceApp
    val viewModel: MemberViewModel = viewModel(factory = ViewModelFactory.from(app))
    val members by viewModel.members.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Members") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD_MEMBER) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add member")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = {
                    query = it
                    viewModel.onSearchQueryChanged(it)
                },
                label = { Text("Search by name or ID") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(12.dp))

            if (members.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No members found.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(members, key = { it.memberId }) { member ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate(Routes.editMember(member.memberId))
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("${member.memberId} - ${member.name}", fontWeight = FontWeight.Bold)
                                    if (!member.mobile.isNullOrBlank()) {
                                        Text(member.mobile, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                AssistChip(
                                    onClick = { navController.navigate(Routes.memberHistory(member.memberId)) },
                                    label = { Text(if (member.isActive) "Active" else "Inactive") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
