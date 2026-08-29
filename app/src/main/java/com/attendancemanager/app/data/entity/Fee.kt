package com.attendancemanager.app.data.entity

import androidx.room.Entity

/**
 * Payment method enum
 */
enum class PaymentMethod {
    CASH, UPI
}

/**
 * Monthly fee record for each student with support for partial payments and payment methods.
 * Tracks fee payment status, amount paid, pending balance, and payment method for each member per month.
 */
@Entity(tableName = "fees", primaryKeys = ["memberId", "yearMonth"])
data class Fee(
    val memberId: String,           // Reference to Member.memberId
    val yearMonth: String,          // ISO yyyy-MM format (e.g., "2026-08")
    val totalAmount: Int,           // Total fee amount in rupees (250/300/400/500)
    val paidAmount: Int = 0,        // Amount already paid (supports partial payments)
    val pendingAmount: Int = 0,     // Remaining amount to be paid
    val isPaid: Boolean = false,    // true only when paidAmount == totalAmount
    val paymentMethod: PaymentMethod? = null,  // CASH or UPI
    val lastPaidDate: String? = null,   // ISO yyyy-MM-dd of last payment
    val upiTransactionId: String? = null,  // UPI transaction ID (only for UPI payments)
    val remarks: String? = null     // Optional notes
) {
    /**
     * Calculate pending amount based on total and paid amounts.
     */
    fun calculatePending(): Int = totalAmount - paidAmount
}
