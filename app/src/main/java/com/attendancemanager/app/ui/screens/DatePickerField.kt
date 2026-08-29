@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.attendancemanager.app.ui.screens
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.attendancemanager.app.util.DateUtils
import java.time.Instant
import java.time.ZoneId

/**
 * A text field that opens a Material date picker dialog when clicked.
 * [isoDate] and [onDateSelected] use ISO yyyy-MM-dd strings throughout the app.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    isoDate: String?,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    allowClear: Boolean = false,
    onClear: (() -> Unit)? = null
) {
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = isoDate?.let { DateUtils.displayDate(it) } ?: "",
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        trailingIcon = {
            IconButton(onClick = { showDialog = true }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Pick date")
            }
        }
    )

    if (allowClear && isoDate != null) {
        TextButton(onClick = { onClear?.invoke() }) {
            Text("Clear $label")
        }
    }

    if (showDialog) {
        val initialMillis = isoDate?.let {
            DateUtils.fromIso(it).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        } ?: System.currentTimeMillis()

        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                        onDateSelected(DateUtils.toIso(date))
                    }
                    showDialog = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
