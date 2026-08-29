package com.attendancemanager.app.data.dao

import androidx.room.*
import com.attendancemanager.app.data.entity.AttendanceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: AttendanceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(records: List<AttendanceRecord>)

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    fun getAttendanceForDate(date: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date = :date")
    suspend fun getAttendanceForDateOnce(date: String): List<AttendanceRecord>

    @Query("SELECT * FROM attendance_records WHERE memberId = :memberId ORDER BY date DESC")
    fun getAttendanceForMember(memberId: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date BETWEEN :startDate AND :endDate")
    fun getAttendanceForRange(startDate: String, endDate: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAttendanceForRangeOnce(startDate: String, endDate: String): List<AttendanceRecord>

    @Query(
        "SELECT * FROM attendance_records WHERE memberId = :memberId AND date BETWEEN :startDate AND :endDate ORDER BY date ASC"
    )
    fun getAttendanceForMemberInRange(memberId: String, startDate: String, endDate: String): Flow<List<AttendanceRecord>>

    @Query("SELECT * FROM attendance_records")
    suspend fun getAllRecordsOnce(): List<AttendanceRecord>

    @Query("DELETE FROM attendance_records")
    suspend fun deleteAll()
}
