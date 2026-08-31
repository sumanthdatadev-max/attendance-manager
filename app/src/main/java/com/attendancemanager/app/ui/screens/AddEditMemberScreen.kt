@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.attendancemanager.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.attendancemanager.app.AttendanceApp
import com.attendancemanager.app.data.entity.Member
import com.attendancemanager.app.ui.viewmodel.MemberViewModel
import com.attendancemanager.app.ui.viewmodel.ViewModelFactory
import com.attendancemanager.app.util.DateUtils
import com.attendancemanager.app.util.calculateFeeByClass
import kotlinx.coroutines.launch

@Composable
fun AddEditMemberScreen(navController: NavController, memberId: String?) {
    val context = LocalContext.current
    val app = context.applicationContext as AttendanceApp
    val viewModel: MemberViewModel = viewModel(factory = ViewModelFactory.from(app))
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isEdit = memberId != null

    var loadedMember by remember { mutableStateOf<Member?>(null) }
    var idField by remember { mutableStateOf("") }
    var nameField by remember { mutableStateOf("") }
    var mobileField by remember { mutableStateOf("") }
    var classNumber by remember { mutableStateOf(1) }
    var joiningDate by remember { mutableStateOf(DateUtils.today()) }
    var leavingDate by remember { mutableStateOf<String?>(null) }
    var isActive by remember { mutableStateOf(true) }
    var loading by remember { mutableStateOf(isEdit) }
    var classMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(memberId) {
        if (memberId != null) {
            val member = viewModel.getMember(memberId)
            loadedMember = member

            member?.let {
                idField = it.memberId
                nameField = it.name
                mobileField = it.mobile ?: ""
                classNumber = it.classNumber
                joiningDate = it.joiningDate
                leavingDate = it.leavingDate
                isActive = it.isActive
            }

            loading = false
        }
    }

    val monthlyFee = calculateFeeByClass(classNumber)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (isEdit) "Edit Member" else "Add Member")
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() }
                    ) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { padding ->

        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            OutlinedTextField(
                value = idField,
                onValueChange = {
                    if (!isEdit) idField = it
                },
                label = {
                    Text("Member ID (unique)")
                },
                enabled = !isEdit,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nameField,
                onValueChange = {
                    nameField = it
                },
                label = {
                    Text("Name")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            /*
             * Class selector
             */
            ExposedDropdownMenuBox(
                expanded = classMenuExpanded,
                onExpandedChange = {
                    classMenuExpanded = !classMenuExpanded
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = "Class $classNumber",
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text("Class")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            expanded = classMenuExpanded
                        )
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = classMenuExpanded,
                    onDismissRequest = {
                        classMenuExpanded = false
                    }
                ) {
                    (1..10).forEach { selectedClass ->
                        DropdownMenuItem(
                            text = {
                                Text("Class $selectedClass")
                            },
                            onClick = {
                                classNumber = selectedClass
                                classMenuExpanded = false
                            }
                        )
                    }
                }
            }

            /*
             * Automatically calculated monthly fee
             */
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text(
                        text = "Monthly Fee",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "₹$monthlyFee",
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            }

            OutlinedTextField(
                value = mobileField,
                onValueChange = {
                    mobileField = it
                },
                label = {
                    Text("Mobile number (optional)")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            DatePickerField(
                label = "Joining Date",
                isoDate = joiningDate,
                onDateSelected = {
                    joiningDate = it
                }
            )

            if (isEdit) {

                DatePickerField(
                    label = "Leaving Date (optional)",
                    isoDate = leavingDate,
                    onDateSelected = {
                        leavingDate = it
                    }
                )

                if (leavingDate != null) {
                    TextButton(
                        onClick = {
                            leavingDate = null
                        }
                    ) {
                        Text("Clear leaving date")
                    }
                }

                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Switch(
                        checked = isActive,
                        onCheckedChange = {
                            isActive = it
                        }
                    )

                    Text(
                        if (isActive) "Active" else "Inactive"
                    )
                }

                Text(
                    "Note: members are never permanently deleted, only marked inactive, " +
                            "so attendance history is always preserved.",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Button(
                onClick = {

                    if (isEdit) {

                        loadedMember?.let { member ->

                            viewModel.updateMember(
                                member = member,
                                name = nameField,
                                mobile = mobileField,
                                classNumber = classNumber,
                                joiningDate = joiningDate,
                                leavingDate = leavingDate,
                                isActive = isActive
                            ) { success, message ->

                                scope.launch {
                                    snackbarHostState.showSnackbar(message)

                                    if (success) {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }

                    } else {

                        viewModel.addMember(
                            memberId = idField,
                            name = nameField,
                            mobile = mobileField,
                            classNumber = classNumber,
                            joiningDate = joiningDate
                        ) { success, message ->

                            scope.launch {
                                snackbarHostState.showSnackbar(message)

                                if (success) {
                                    navController.popBackStack()
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (isEdit) "Save Changes" else "Add Member"
                )
            }
        }
    }
}
