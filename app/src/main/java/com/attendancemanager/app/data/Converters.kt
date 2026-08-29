package com.attendancemanager.app.data

import androidx.room.TypeConverter
import com.attendancemanager.app.data.entity.AttendanceStatus

class Converters {
    @TypeConverter
    fun fromStatus(status: AttendanceStatus): String = status.name

    @TypeConverter
    fun toStatus(value: String): AttendanceStatus = AttendanceStatus.valueOf(value)
}
