package com.attendancemanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A holiday applies to an entire date - on a holiday date, members are
 * never counted absent even if no attendance record exists for them.
 */
@Entity(tableName = "holidays")
data class Holiday(
    @PrimaryKey val date: String, // ISO yyyy-MM-dd
    val description: String = "Holiday"
)
