package com.attendancemanager.app.util

import com.attendancemanager.app.data.entity.AttendanceRecord
import com.attendancemanager.app.data.entity.AttendanceStatus
import com.attendancemanager.app.data.entity.Member
import com.attendancemanager.app.repository.AttendanceRepository
import com.attendancemanager.app.repository.MemberRepository
import java.time.LocalDate

/**
 * Seeds 10 sample members (not 110 - per testing requirement, 110 is the real-world
 * scale the app is designed for, sample data is only for demonstration/testing) plus
 * a few days of sample attendance so the app is immediately explorable.
 */
object SampleData {

    suspend fun seedIfEmpty(memberRepository: MemberRepository, attendanceRepository: AttendanceRepository) {
        val existing = memberRepository.getAllMembersOnce()
        if (existing.isNotEmpty()) return

        val joinBase = LocalDate.now().minusMonths(3)
        val names = listOf(
            "Aarav Sharma", "Diya Patel", "Vihaan Reddy", "Ananya Iyer", "Kabir Singh",
            "Ishita Gupta", "Arjun Nair", "Meera Rao", "Rohan Verma", "Sanya Kapoor"
        )

        val members = names.mapIndexed { index, name ->
            Member(
                memberId = "M%03d".format(index + 1),
                name = name,
                mobile = if (index % 3 == 0) null else "98765%05d".format(10000 + index),
                joiningDate = DateUtils.toIso(joinBase.plusDays((index * 2).toLong())),
                leavingDate = null,
                isActive = true
            )
        }
        members.forEach { memberRepository.addMember(it) }

        // Seed the last 5 days of attendance (excluding today) as sample history.
        val records = mutableListOf<AttendanceRecord>()
        for (dayOffset in 1..5) {
            val date = DateUtils.toIso(LocalDate.now().minusDays(dayOffset.toLong()))
            members.forEachIndexed { index, member ->
                val status = when {
                    (index + dayOffset) % 7 == 0 -> AttendanceStatus.ABSENT
                    (index + dayOffset) % 5 == 0 -> AttendanceStatus.LEAVE
                    else -> AttendanceStatus.PRESENT
                }
                records.add(AttendanceRecord(member.memberId, date, status))
            }
        }
        attendanceRepository.saveRecords(records)
    }
}
