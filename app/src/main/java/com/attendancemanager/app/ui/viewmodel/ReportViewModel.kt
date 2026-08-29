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
import java.time.YearMonth

data class MemberMonthlyStat(
    val member: Member,
    val present: Int,
    val absent: Int,
    val leave: Int,
    val eligibleDays: Int,
    val percentage: Double
)

data class MonthlyReportUiState(
    val yearMonth: YearMonth = DateUtils.currentYearMonth(),
    val totalPresent: Int = 0,
    val totalAbsent: Int = 0,
    val totalLeave: Int = 0,
    val totalHolidays: Int = 0,
    val overallAttendancePercentage: Double = 0.0,
    val memberStats: List<MemberMonthlyStat> = emptyList(),
    val isLoading: Boolean = true
)

class ReportViewModel(
    private val memberRepository: MemberRepository,
    private val attendanceRepository: AttendanceRepository,
    private val holidayRepository: HolidayRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MonthlyReportUiState())
    val uiState: StateFlow<MonthlyReportUiState> = _uiState.asStateFlow()

    init {
        loadMonth(DateUtils.currentYearMonth())
    }

    fun loadMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, yearMonth = yearMonth)

            val startDate = DateUtils.monthStart(yearMonth)
            val endDate = DateUtils.monthEnd(yearMonth)

            val holidays = holidayRepository.getHolidaysInRangeOnce(startDate, endDate)
            val holidayDates = holidays.map { it.date }.toSet()

            val allMembers = memberRepository.getAllMembersOnce()
            val allRecordsInRange = attendanceRepository.getAttendanceForRangeOnce(startDate, endDate)
                .filter { it.date !in holidayDates } // holidays never count as absence
                .groupBy { it.memberId }

            var totalPresent = 0
            var totalAbsent = 0
            var totalLeave = 0

            val memberStats = allMembers
                .filter { member ->
                    // Include members who had any presence in this window: joined on/before
                    // month end, and (no leaving date OR left on/after month start).
                    member.joiningDate <= endDate && (member.leavingDate == null || member.leavingDate >= startDate)
                }
                .map { member ->
                    val records = allRecordsInRange[member.memberId].orEmpty()
                    val present = records.count { it.status == AttendanceStatus.PRESENT }
                    val absent = records.count { it.status == AttendanceStatus.ABSENT }
                    val leave = records.count { it.status == AttendanceStatus.LEAVE }
                    val eligibleDays = present + absent + leave
                    val percentage = if (eligibleDays > 0) (present.toDouble() / eligibleDays) * 100.0 else 0.0

                    totalPresent += present
                    totalAbsent += absent
                    totalLeave += leave

                    MemberMonthlyStat(member, present, absent, leave, eligibleDays, percentage)
                }
                .sortedBy { it.member.name }

            val overallEligible = totalPresent + totalAbsent + totalLeave
            val overallPercentage = if (overallEligible > 0) (totalPresent.toDouble() / overallEligible) * 100.0 else 0.0

            _uiState.value = MonthlyReportUiState(
                yearMonth = yearMonth,
                totalPresent = totalPresent,
                totalAbsent = totalAbsent,
                totalLeave = totalLeave,
                totalHolidays = holidays.size,
                overallAttendancePercentage = overallPercentage,
                memberStats = memberStats,
                isLoading = false
            )
        }
    }
}
