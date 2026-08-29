package com.attendancemanager.app.util

import com.attendancemanager.app.data.entity.AttendanceRecord
import com.attendancemanager.app.data.entity.AttendanceStatus
import com.attendancemanager.app.data.entity.Fee
import com.attendancemanager.app.data.entity.Member
import com.attendancemanager.app.repository.AttendanceRepository
import com.attendancemanager.app.repository.FeeRepository
import com.attendancemanager.app.repository.MemberRepository
import java.time.LocalDate
import java.time.YearMonth

/**
 * Seeds 10 sample members (not 110 - per testing requirement, 110 is the real-world
 * scale the app is designed for, sample data is only for demonstration/testing) plus
 * a few days of sample attendance and sample fee records so the app is immediately explorable.
 */
object SampleData {

    suspend fun seedIfEmpty(
        memberRepository: MemberRepository,
        attendanceRepository: AttendanceRepository,
        feeRepository: FeeRepository? = null
    ) {
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
                classNumber = (index % 10) + 1,  // Classes 1-10 based on index
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

        // Seed sample fee data for current month
        if (feeRepository != null) {
            val currentYearMonth = YearMonth.now().toString()
            val fees = mutableListOf<Fee>()
            members.forEach { member ->
                val feeAmount = calculateFeeByClass(member.classNumber)
                
                // Create fee record - some paid, some pending, some partial
                val fee = when {
                    // First 3 members: fully paid
                    member.memberId in listOf("M001", "M002", "M003") -> {
                        Fee(
                            memberId = member.memberId,
                            yearMonth = currentYearMonth,
                            totalAmount = feeAmount,
                            paidAmount = feeAmount,
                            pendingAmount = 0,
                            isPaid = true,
                            paymentMethod = com.attendancemanager.app.data.entity.PaymentMethod.CASH,
                            lastPaidDate = DateUtils.toIso(LocalDate.now().minusDays(5))
                        )
                    }
                    // Next 3 members: partially paid
                    member.memberId in listOf("M004", "M005", "M006") -> {
                        val paidAmount = feeAmount / 2
                        Fee(
                            memberId = member.memberId,
                            yearMonth = currentYearMonth,
                            totalAmount = feeAmount,
                            paidAmount = paidAmount,
                            pendingAmount = feeAmount - paidAmount,
                            isPaid = false,
                            paymentMethod = com.attendancemanager.app.data.entity.PaymentMethod.UPI,
                            lastPaidDate = DateUtils.toIso(LocalDate.now().minusDays(3)),
                            upiTransactionId = "UPI${member.memberId}${LocalDate.now().year}"
                        )
                    }
                    // Remaining members: not paid
                    else -> {
                        Fee(
                            memberId = member.memberId,
                            yearMonth = currentYearMonth,
                            totalAmount = feeAmount,
                            paidAmount = 0,
                            pendingAmount = feeAmount,
                            isPaid = false,
                            paymentMethod = null,
                            lastPaidDate = null
                        )
                    }
                }
                fees.add(fee)
            }
            
            // Save all fee records to database
            fees.forEach { fee ->
                feeRepository.insertOrUpdateFee(fee)
            }
        }
    }

    private fun calculateFeeByClass(classNumber: Int): Int {
        return when (classNumber) {
            in 1..3 -> 250
            in 4..6 -> 300
            in 7..9 -> 400
            10 -> 500
            else -> 0
        }
    }
}
