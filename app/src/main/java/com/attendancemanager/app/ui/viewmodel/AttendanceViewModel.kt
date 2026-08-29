package com.attendancemanager.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendancemanager.app.data.entity.AttendanceRecord
import com.attendancemanager.app.data.entity.AttendanceStatus
import com.attendancemanager.app.data.entity.Member
import com.attendancemanager.app.repository.AttendanceRepository
import com.attendancemanager.app.repository.HolidayRepository
import com.attendancemanager.app.repository.MemberRepository
import com.attendancemanager.app.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MemberAttendanceRow(
    val member: Member,
    val status: AttendanceStatus
)

data class AttendanceUiState(
    val date: String = DateUtils.today(),
    val isHoliday: Boolean = false,
    val holidayDescription: String = "",
    val rows: List<MemberAttendanceRow> = emptyList(),
    val isLoading: Boolean = true,
    val saveMessage: String? = null
)

class AttendanceViewModel(
    private val memberRepository: MemberRepository,
    private val attendanceRepository: AttendanceRepository,
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AttendanceUiState())
    val uiState: StateFlow<AttendanceUiState> = _uiState.asStateFlow()

    init {
        loadForDate(DateUtils.today())
    }

    fun loadForDate(date: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(date = date, isLoading = true, saveMessage = null)

            val holiday = holidayRepository.isHoliday(date)
            val eligibleMembers = memberRepository.getMembersEligibleOnDateOnce(date)
            val existingRecords = attendanceRepository.getAttendanceForDateOnce(date)
                .associateBy { it.memberId }

            val rows = eligibleMembers.map { member ->
                val status = existingRecords[member.memberId]?.status ?: AttendanceStatus.PRESENT
                MemberAttendanceRow(member, status)
            }

            _uiState.value = _uiState.value.copy(
                isHoliday = holiday,
                rows = rows,
                isLoading = false
            )
        }
    }

    fun setStatus(memberId: String, status: AttendanceStatus) {
        val current = _uiState.value
        val updated = current.rows.map {
            if (it.member.memberId == memberId) it.copy(status = status) else it
        }
        _uiState.value = current.copy(rows = updated)
    }

    fun markAllPresent() {
        val current = _uiState.value
        val updated = current.rows.map { it.copy(status = AttendanceStatus.PRESENT) }
        _uiState.value = current.copy(rows = updated)
    }

    fun saveAttendance() {
        viewModelScope.launch {
            val current = _uiState.value
            val records = current.rows.map {
                AttendanceRecord(it.member.memberId, current.date, it.status)
            }
            attendanceRepository.saveRecords(records)
            _uiState.value = current.copy(saveMessage = "Attendance saved for ${DateUtils.displayDate(current.date)}")
        }
    }

    fun clearSaveMessage() {
        _uiState.value = _uiState.value.copy(saveMessage = null)
    }
}
