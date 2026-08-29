package com.attendancemanager.app.data.dao

import androidx.room.*
import com.attendancemanager.app.data.entity.Fee
import com.attendancemanager.app.data.entity.PaymentMethod
import kotlinx.coroutines.flow.Flow

@Dao
interface FeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFee(fee: Fee)

    @Query("SELECT * FROM fees WHERE memberId = :memberId AND yearMonth = :yearMonth")
    suspend fun getFeeRecord(memberId: String, yearMonth: String): Fee?

    @Query("SELECT * FROM fees WHERE yearMonth = :yearMonth ORDER BY memberId")
    fun getMonthlyFees(yearMonth: String): Flow<List<Fee>>

    @Query("SELECT * FROM fees WHERE memberId = :memberId ORDER BY yearMonth DESC")
    fun getMemberFeeHistory(memberId: String): Flow<List<Fee>>

    // Get all unpaid or partially paid fees
    @Query("SELECT * FROM fees WHERE yearMonth = :yearMonth AND isPaid = 0 ORDER BY memberId")
    fun getPendingFees(yearMonth: String): Flow<List<Fee>>

    // Get fully paid fees
    @Query("SELECT * FROM fees WHERE yearMonth = :yearMonth AND isPaid = 1 ORDER BY memberId")
    fun getPaidFees(yearMonth: String): Flow<List<Fee>>

    // Get fees with pending balance
    @Query("SELECT * FROM fees WHERE yearMonth = :yearMonth AND pendingAmount > 0 ORDER BY memberId")
    fun getFeesWithPendingBalance(yearMonth: String): Flow<List<Fee>>

    // Get fees by payment method
    @Query("SELECT * FROM fees WHERE yearMonth = :yearMonth AND paymentMethod = :method ORDER BY memberId")
    fun getFeesByPaymentMethod(yearMonth: String, method: String): Flow<List<Fee>>

    // Record a partial or full payment with payment method
    @Query("""
        UPDATE fees 
        SET paidAmount = :paidAmount, 
            pendingAmount = :pendingAmount, 
            isPaid = :isPaid, 
            paymentMethod = :paymentMethod,
            lastPaidDate = :paidDate,
            upiTransactionId = :upiTransactionId
        WHERE memberId = :memberId AND yearMonth = :yearMonth
    """)
    suspend fun recordPayment(
        memberId: String,
        yearMonth: String,
        paidAmount: Int,
        pendingAmount: Int,
        isPaid: Boolean,
        paymentMethod: String,
        paidDate: String,
        upiTransactionId: String? = null
    )

    // Get total fees, paid, and pending for a month with payment method breakdown
    @Query("""
        SELECT 
            SUM(totalAmount) as totalFees,
            SUM(paidAmount) as totalPaid,
            SUM(pendingAmount) as totalPending,
            SUM(CASE WHEN isPaid = 1 THEN 1 ELSE 0 END) as fullyPaidCount,
            COUNT(*) as totalMembers
        FROM fees 
        WHERE yearMonth = :yearMonth
    """)
    suspend fun getMonthlyFeesSummary(yearMonth: String): FeeSummary

    // Get payment method breakdown for a month
    @Query("""
        SELECT 
            paymentMethod,
            COUNT(*) as count,
            SUM(paidAmount) as totalAmount
        FROM fees 
        WHERE yearMonth = :yearMonth AND paidAmount > 0
        GROUP BY paymentMethod
    """)
    suspend fun getPaymentMethodBreakdown(yearMonth: String): List<PaymentMethodBreakdown>

    // Get member's pending balance across all months
    @Query("SELECT SUM(pendingAmount) FROM fees WHERE memberId = :memberId")
    suspend fun getMemberTotalPending(memberId: String): Int?
}

data class FeeSummary(
    val totalFees: Int,
    val totalPaid: Int,
    val totalPending: Int,
    val fullyPaidCount: Int,
    val totalMembers: Int
)

data class PaymentMethodBreakdown(
    val paymentMethod: String?,
    val count: Int,
    val totalAmount: Int
)
