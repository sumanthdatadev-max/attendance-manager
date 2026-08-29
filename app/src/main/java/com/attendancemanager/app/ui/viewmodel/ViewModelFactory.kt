package com.attendancemanager.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.attendancemanager.app.AttendanceApp
import com.attendancemanager.app.backup.BackupManager
import com.attendancemanager.app.repository.AttendanceRepository
import com.attendancemanager.app.repository.FeeRepository
import com.attendancemanager.app.repository.HolidayRepository
import com.attendancemanager.app.repository.MemberRepository

class ViewModelFactory(
    private val memberRepository: MemberRepository,
    private val attendanceRepository: AttendanceRepository,
    private val holidayRepository: HolidayRepository,
    private val feeRepository: FeeRepository,
    private val backupManager: BackupManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when {
            modelClass.isAssignableFrom(MemberViewModel::class.java) ->
                MemberViewModel(memberRepository, attendanceRepository) as T
            modelClass.isAssignableFrom(AttendanceViewModel::class.java) ->
                AttendanceViewModel(memberRepository, attendanceRepository, holidayRepository) as T
            modelClass.isAssignableFrom(HolidayViewModel::class.java) ->
                HolidayViewModel(holidayRepository) as T
            modelClass.isAssignableFrom(DashboardViewModel::class.java) ->
                DashboardViewModel(memberRepository, attendanceRepository, holidayRepository) as T
            modelClass.isAssignableFrom(ReportViewModel::class.java) ->
                ReportViewModel(memberRepository, attendanceRepository, holidayRepository) as T
            modelClass.isAssignableFrom(FeeViewModel::class.java) ->
                FeeViewModel(feeRepository) as T
            modelClass.isAssignableFrom(MoreViewModel::class.java) ->
                MoreViewModel(holidayRepository, backupManager) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }

    companion object {
        fun from(app: AttendanceApp): ViewModelFactory = ViewModelFactory(
            app.memberRepository,
            app.attendanceRepository,
            app.holidayRepository,
            app.feeRepository,
            app.backupManager
        )
    }
}
