package com.attendancemanager.app

import android.app.Application
import com.attendancemanager.app.backup.BackupManager
import com.attendancemanager.app.data.AppDatabase
import com.attendancemanager.app.repository.AttendanceRepository
import com.attendancemanager.app.repository.FeeRepository
import com.attendancemanager.app.repository.HolidayRepository
import com.attendancemanager.app.repository.MemberRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AttendanceApp : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: AppDatabase by lazy { AppDatabase.getInstance(this) }

    val memberRepository: MemberRepository by lazy { MemberRepository(database.memberDao()) }
    val attendanceRepository: AttendanceRepository by lazy { AttendanceRepository(database.attendanceDao()) }
    val holidayRepository: HolidayRepository by lazy { HolidayRepository(database.holidayDao()) }
    val feeRepository: FeeRepository by lazy { FeeRepository(database.feeDao(), database.memberDao()) }

    val backupManager: BackupManager by lazy {
        BackupManager(memberRepository, attendanceRepository, holidayRepository)
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            com.attendancemanager.app.util.SampleData.seedIfEmpty(memberRepository, attendanceRepository, feeRepository)
        }
    }
}
