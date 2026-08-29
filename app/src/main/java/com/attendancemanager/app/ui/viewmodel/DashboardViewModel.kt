package com.attendancemanager.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class DashboardUiState(
    val date: String = DateUtils.today(),
    val totalActiveMembers: Int = 0,
    val presentCount: Int = 0,
    val absentCount: Int = 0,
    val leaveCount: Int = 0,
    val isHoliday: Boolean = false,
    val holidayDescription: String = "",
    val absentees: List<Member> = emptyList(),
    val totalAbsenteeCountAllTime: Int = 0,
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val memberRepository: MemberRepository,
    private val attendanceRepository: AttendanceRepository,
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh(date: String = DateUtils.today()) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, date = date)

            val holiday = holidayRepository.isHoliday(date)
            val eligibleMembers = memberRepository.getMembersEligibleOnDateOnce(date)
            val records = attendanceRepository.getAttendanceForDateOnce(date).associateBy { it.memberId }

            var present = 0
            var absent = 0
            var leave = 0
            val absentees = mutableListOf<Member>()

            if (!holiday) {
                eligibleMembers.forEach { member ->
                    when (records[member.memberId]?.status ?: AttendanceStatus.PRESENT) {
                        AttendanceStatus.PRESENT -> present++
                        AttendanceStatus.ABSENT -> {
                            absent++
                            absentees.add(member)
                        }
                        AttendanceStatus.LEAVE -> leave++
                    }
                }
            }

            // Lifetime absentee-record count (for the "Total absentee count" figure).
            val allRecords = attendanceRepository.getAllRecordsOnce()
            val totalAbsenteeAllTime = allRecords.count { it.status == AttendanceStatus.ABSENT }

            _uiState.value = DashboardUiState(
                date = date,
                totalActiveMembers = eligibleMembers.count { it.isActive },
                presentCount = present,
                absentCount = absent,
                leaveCount = leave,
                isHoliday = holiday,
                holidayDescription = if (holiday) "Holiday" else "",
                absentees = absentees,
                totalAbsenteeCountAllTime = totalAbsenteeAllTime,
                isLoading = false
            )
        }
    }
}
