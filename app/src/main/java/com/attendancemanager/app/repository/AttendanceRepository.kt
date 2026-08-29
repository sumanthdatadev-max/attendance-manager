package com.attendancemanager.app.repository

import com.attendancemanager.app.data.dao.AttendanceDao
import com.attendancemanager.app.data.entity.AttendanceRecord
import kotlinx.coroutines.flow.Flow

class AttendanceRepository(private val attendanceDao: AttendanceDao) {

    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForDate(date)

    suspend fun getAttendanceForDateOnce(date: String): List<AttendanceRecord> =
        attendanceDao.getAttendanceForDateOnce(date)

    fun getAttendanceForMember(memberId: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForMember(memberId)

    fun getAttendanceForRange(startDate: String, endDate: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForRange(startDate, endDate)

    suspend fun getAttendanceForRangeOnce(startDate: String, endDate: String): List<AttendanceRecord> =
        attendanceDao.getAttendanceForRangeOnce(startDate, endDate)

    fun getAttendanceForMemberInRange(memberId: String, startDate: String, endDate: String): Flow<List<AttendanceRecord>> =
        attendanceDao.getAttendanceForMemberInRange(memberId, startDate, endDate)

    suspend fun saveRecord(record: AttendanceRecord) = attendanceDao.upsert(record)

    suspend fun saveRecords(records: List<AttendanceRecord>) = attendanceDao.upsertAll(records)

    suspend fun getAllRecordsOnce(): List<AttendanceRecord> = attendanceDao.getAllRecordsOnce()

    suspend fun restoreAll(records: List<AttendanceRecord>) {
        attendanceDao.deleteAll()
        attendanceDao.upsertAll(records)
    }
}
