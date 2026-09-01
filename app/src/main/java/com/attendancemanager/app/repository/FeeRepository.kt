package com.attendancemanager.app.repository

import com.attendancemanager.app.data.dao.FeeDao
import com.attendancemanager.app.data.dao.MemberDao
import com.attendancemanager.app.data.entity.Fee
import com.attendancemanager.app.data.entity.PaymentMethod
import com.attendancemanager.app.util.calculateFeeByClass
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class FeeRepository(
    private val feeDao: FeeDao,
    private val memberDao: MemberDao
) {
    fun getMonthlyFees(yearMonth: String): Flow<List<Fee>> {
        return feeDao.getMonthlyFees(yearMonth)
    }

    fun getMemberFeeHistory(memberId: String): Flow<List<Fee>> {
        return feeDao.getMemberFeeHistory(memberId)
    }

    fun getPendingFees(yearMonth: String): Flow<List<Fee>> {
        return feeDao.getPendingFees(yearMonth)
    }

    fun getPaidFees(yearMonth: String): Flow<List<Fee>> {
        return feeDao.getPaidFees(yearMonth)
    }

    fun getFeesWithPendingBalance(yearMonth: String): Flow<List<Fee>> {
        return feeDao.getFeesWithPendingBalance(yearMonth)
    }

    fun getFeesByPaymentMethod(yearMonth: String, method: PaymentMethod): Flow<List<Fee>> {
        return feeDao.getFeesByPaymentMethod(yearMonth, method.name)
    }

    /**
     * Auto-generate fees for all members for a given month if they don't already exist.
     * This ensures the Fee Details list is always populated with all members.
     */
    suspend fun ensureMonthlyFeesExist(yearMonth: String) {
        val members = memberDao.getAllMembersOnce()
        members.forEach { member ->
            val existingFee = feeDao.getFeeRecord(member.memberId, yearMonth)
            if (existingFee == null) {
                val totalAmount = calculateFeeByClass(member.classNumber)
                val newFee = Fee(
                    memberId = member.memberId,
                    yearMonth = yearMonth,
                    totalAmount = totalAmount,
                    paidAmount = 0,
                    pendingAmount = totalAmount,
                    isPaid = false,
                    paymentMethod = null,
                    lastPaidDate = null,
                    upiTransactionId = null,
                    remarks = null
                )
                feeDao.insertOrUpdateFee(newFee)
            }
        }
    }

    /**
     * Record a payment for a student's fee (partial or full) with payment method.
     * @param memberId Student ID
     * @param yearMonth Month in yyyy-MM format
     * @param paymentAmount Amount being paid now
     * @param paymentMethod CASH or UPI
     * @param upiTransactionId Transaction ID (only required for UPI payments)
     */
    suspend fun recordPayment(
        memberId: String,
        yearMonth: String,
        paymentAmount: Int,
        paymentMethod: PaymentMethod,
        upiTransactionId: String? = null
    ) {
        val currentFee = feeDao.getFeeRecord(memberId, yearMonth) ?: return

        val newPaidAmount = currentFee.paidAmount + paymentAmount
        val newPendingAmount = (currentFee.totalAmount - newPaidAmount).coerceAtLeast(0)
        val isNowFullyPaid = newPaidAmount >= currentFee.totalAmount

        feeDao.recordPayment(
            memberId = memberId,
            yearMonth = yearMonth,
            paidAmount = newPaidAmount,
            pendingAmount = newPendingAmount,
            isPaid = isNowFullyPaid,
            paymentMethod = paymentMethod.name,
            paidDate = LocalDate.now().toString(),
            upiTransactionId = if (paymentMethod == PaymentMethod.UPI) upiTransactionId else null
        )
    }

    /**
     * Get summary of all fees for a month.
     */
    suspend fun getMonthlyFeesSummary(yearMonth: String): com.attendancemanager.app.data.dao.FeeSummary {
        return feeDao.getMonthlyFeesSummary(yearMonth)
    }

    /**
     * Get payment method breakdown for a month.
     */
    suspend fun getPaymentMethodBreakdown(yearMonth: String): List<com.attendancemanager.app.data.dao.PaymentMethodBreakdown> {
        return feeDao.getPaymentMethodBreakdown(yearMonth)
    }

    /**
     * Get total pending balance for a member across all months.
     */
    suspend fun getMemberTotalPending(memberId: String): Int {
        return feeDao.getMemberTotalPending(memberId) ?: 0
    }

    /**
     * Generate or update fees for all active members for a given month.
     * Alias for ensureMonthlyFeesExist for backward compatibility.
     */
    suspend fun generateMonthlyFees(yearMonth: String) {
        ensureMonthlyFeesExist(yearMonth)
    }

    /**
     * Insert or update a fee record.
     */
    suspend fun insertOrUpdateFee(fee: Fee) {
        feeDao.insertOrUpdateFee(fee)
    }
}
