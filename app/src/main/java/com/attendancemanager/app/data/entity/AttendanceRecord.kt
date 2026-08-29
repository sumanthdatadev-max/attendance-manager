package com.attendancemanager.app.data.entity

import androidx.room.Entity

enum class AttendanceStatus {
    PRESENT, ABSENT, LEAVE
}

/**
 * One attendance entry, keyed by Member ID + Date as required.
 * Composite primary key guarantees exactly one status per member per date,
 * and re-saving the same date simply overwrites (edit) the previous entry.
 */
@Entity(tableName = "attendance_records", primaryKeys = ["memberId", "date"])
data class AttendanceRecord(
    val memberId: String,
    val date: String, // ISO yyyy-MM-dd
    val status: AttendanceStatus
)
