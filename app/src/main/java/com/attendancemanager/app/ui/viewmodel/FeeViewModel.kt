package com.attendancemanager.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendancemanager.app.repository.FeeRepository
import com.attendancemanager.app.data.entity.Fee
import com.attendancemanager.app.data.entity.PaymentMethod
import com.attendancemanager.app.data.dao.FeeSummary
import com.attendancemanager.app.data.dao.PaymentMethodBreakdown
import com.attendancemanager.app.util.calculateFeeByClass
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth

data class FeeUIState(
    val allFees: List<Fee> = emptyList(),
    val pendingFees: List<Fee> = emptyList(),
    val paidFees: List<Fee> = emptyList(),
    val feeSummary: FeeSummary? = null,
    val paymentMethodBreakdown: List<PaymentMethodBreakdown> = emptyList(),
    val selectedYearMonth: String = YearMonth.now().toString()
)

data class FeeWithMemberName(
    val fee: Fee,
    val memberName: String,
    val currentClassNumber: Int,           // Current class from Member entity
    val expectedFeeAmount: Int,            // What the fee should be based on current class
    val hasClassMismatch: Boolean = false  // True if expectedFeeAmount != fee.totalAmount
)

class FeeViewModel(private val feeRepository: FeeRepository) : ViewModel() {
    private val selectedYearMonth = MutableStateFlow(YearMonth.now().toString())

    val feeState: StateFlow<FeeUIState> = selectedYearMonth
        .flatMapLatest { yearMonth ->
            // Auto-generate fees when month changes
            viewModelScope.launch {
                feeRepository.ensureMonthlyFeesExist(yearMonth)
            }

            combine(
                feeRepository.getMonthlyFees(yearMonth),
                feeRepository.getPendingFees(yearMonth),
                feeRepository.getPaidFees(yearMonth)
            ) { all, pending, paid ->
                FeeUIState(
                    allFees = all,
                    pendingFees = pending,
                    paidFees = paid,
                    selectedYearMonth = yearMonth
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, FeeUIState())

    // Fee details with member names and class mismatch detection
    val feeDetailsWithNames: StateFlow<List<FeeWithMemberName>> = feeState
        .flatMapLatest { state ->
            if (state.allFees.isEmpty()) {
                flowOf(emptyList())
            } else {
                flow {
                    val feesWithNames = mutableListOf<FeeWithMemberName>()
                    state.allFees.forEach { fee ->
                        val member = feeRepository.getMemberById(fee.memberId)
                        val currentClass = member?.classNumber ?: 0
                        val expectedFee = calculateFeeByClass(currentClass)
                        val hasMismatch = expectedFee != fee.totalAmount && expectedFee != 0

                        feesWithNames.add(
                            FeeWithMemberName(
                                fee = fee,
                                memberName = member?.name ?: "Unknown",
                                currentClassNumber = currentClass,
                                expectedFeeAmount = expectedFee,
                                hasClassMismatch = hasMismatch
                            )
                        )
                    }
                    emit(feesWithNames.sortedBy { it.memberName })
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Quick stats
    val totalDue: StateFlow<Int> = feeState
        .map { it.pendingFees.sumOf { fee -> fee.pendingAmount } }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val totalCollected: StateFlow<Int> = feeState
        .map { state ->
            state.allFees.sumOf { it.paidAmount }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val fullyPaidCount: StateFlow<Int> = feeState
        .map { it.paidFees.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val pendingCount: StateFlow<Int> = feeState
        .map { it.pendingFees.size }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Payment method breakdown
    val cashCollected: StateFlow<Int> = feeState
        .map { state ->
            state.allFees
                .filter { it.paymentMethod == PaymentMethod.CASH }
                .sumOf { it.paidAmount }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val upiCollected: StateFlow<Int> = feeState
        .map { state ->
            state.allFees
                .filter { it.paymentMethod == PaymentMethod.UPI }
                .sumOf { it.paidAmount }
        }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    fun selectMonth(yearMonth: String) {
        selectedYearMonth.value = yearMonth
    }

    /**
     * Record a payment for a student (partial or full) with payment method.
     */
    fun recordPayment(
        memberId: String,
        paymentAmount: Int,
        paymentMethod: PaymentMethod,
        upiTransactionId: String? = null
    ) {
        viewModelScope.launch {
            feeRepository.recordPayment(
                memberId = memberId,
                yearMonth = selectedYearMonth.value,
                paymentAmount = paymentAmount,
                paymentMethod = paymentMethod,
                upiTransactionId = upiTransactionId
            )
        }
    }

    fun generateMonthlyFees() {
        viewModelScope.launch {
            feeRepository.generateMonthlyFees(selectedYearMonth.value)
        }
    }
}
