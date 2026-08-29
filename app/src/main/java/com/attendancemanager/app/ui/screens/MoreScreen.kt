@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.attendancemanager.app.ui.screens
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
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
import com.attendancemanager.app.backup.BackupManager
import com.attendancemanager.app.ui.viewmodel.HolidayViewModel
import com.attendancemanager.app.ui.viewmodel.MoreViewModel
import com.attendancemanager.app.ui.viewmodel.ViewModelFactory
import com.attendancemanager.app.util.DateUtils

@Composable
fun MoreScreen() {
    val context = LocalContext.current
    val app = context.applicationContext as AttendanceApp
    val holidayViewModel: HolidayViewModel = viewModel(factory = ViewModelFactory.from(app))
    val moreViewModel: MoreViewModel = viewModel(factory = ViewModelFactory.from(app))

    val holidays by holidayViewModel.holidays.collectAsStateWithLifecycle()
    val statusMessage by moreViewModel.statusMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var newHolidayDate by remember { mutableStateOf(DateUtils.today()) }
    var newHolidayDesc by remember { mutableStateOf("") }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { moreViewModel.exportBackup(context, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { moreViewModel.importBackup(context, it) }
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            moreViewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("More") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Backup & Restore", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = { exportLauncher.launch(BackupManager.SUGGESTED_FILE_NAME) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Export")
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Restore")
                    }
                }
            }
            item {
                Text(
                    "Export saves all members, attendance and holidays to a JSON file you choose. " +
                        "Restore replaces current data with a previously exported backup file.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            item { Divider() }

            item {
                Text("Manage Holidays", style = MaterialTheme.typography.titleMedium)
            }
            item {
                DatePickerField(
                    label = "Holiday date",
                    isoDate = newHolidayDate,
                    onDateSelected = { newHolidayDate = it }
                )
            }
            item {
                OutlinedTextField(
                    value = newHolidayDesc,
                    onValueChange = { newHolidayDesc = it },
                    label = { Text("Description (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            item {
                Button(
                    onClick = {
                        holidayViewModel.addHoliday(newHolidayDate, newHolidayDesc)
                        newHolidayDesc = ""
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Event, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Set Holiday")
                }
            }

            item {
                Text("Holiday List (${holidays.size})", style = MaterialTheme.typography.titleSmall)
            }

            if (holidays.isEmpty()) {
                item { Text("No holidays set.", style = MaterialTheme.typography.bodySmall) }
            } else {
                items(holidays, key = { it.date }) { holiday ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(DateUtils.displayDate(holiday.date), fontWeight = FontWeight.Bold)
                                Text(holiday.description, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { holidayViewModel.removeHoliday(holiday.date) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Remove holiday")
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}
