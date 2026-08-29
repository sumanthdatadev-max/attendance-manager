package com.attendancemanager.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A club/organization member.
 * memberId is the user-facing unique ID (e.g. "M001") and is also the DB primary key,
 * as required: "Use Member ID as the database identifier."
 *
 * Members are never deleted - only marked inactive - so historical attendance
 * is always preserved.
 */
@Entity(tableName = "members")
data class Member(
    @PrimaryKey val memberId: String,
    val name: String,
    val mobile: String? = null,
    val classNumber: Int,                    // NEW: Student class (1-10) for fee calculation
    val joiningDate: String,                 // ISO yyyy-MM-dd
    val leavingDate: String? = null,         // ISO yyyy-MM-dd, null if still active/no leave date set
    val isActive: Boolean = true
)
