package com.attendancemanager.app.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendancemanager.app.backup.BackupManager
import com.attendancemanager.app.repository.HolidayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MoreViewModel(
    private val holidayRepository: HolidayRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    fun exportBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.exportTo(context, uri)
            _statusMessage.value = result.message
        }
    }

    fun importBackup(context: Context, uri: Uri) {
        viewModelScope.launch {
            val result = backupManager.importFrom(context, uri)
            _statusMessage.value = result.message
        }
    }

    fun clearMessage() {
        _statusMessage.value = null
    }
}
