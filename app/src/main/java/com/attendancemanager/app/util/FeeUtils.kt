package com.attendancemanager.app.util

/**
 * Calculates monthly fee based on student class.
 * Classes 1-3: ₹250
 * Classes 4-6: ₹300
 * Classes 7-9: ₹400
 * Class 10: ₹500
 */
fun calculateFeeByClass(classNumber: Int): Int {
    return when (classNumber) {
        in 1..3 -> 250
        in 4..6 -> 300
        in 7..9 -> 400
        10 -> 500
        else -> 0  // Invalid class
    }
}
