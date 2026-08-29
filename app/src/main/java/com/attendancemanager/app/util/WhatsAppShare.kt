package com.attendancemanager.app.util

import android.content.Context
import android.content.Intent
import com.attendancemanager.app.data.entity.Member

object WhatsAppShare {

    /**
     * Builds the absentee message and launches ACTION_SEND so the user picks
     * the target app themselves (WhatsApp among them). Nothing is sent automatically,
     * and no WhatsApp Business API is used.
     */
    fun shareAbsentees(context: Context, dateDisplay: String, absentees: List<Member>) {
        val message = buildMessage(dateDisplay, absentees)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, message)
        }
        context.startActivity(Intent.createChooser(intent, "Share absentee list"))
    }

    fun buildMessage(dateDisplay: String, absentees: List<Member>): String {
        val sb = StringBuilder()
        sb.append("Attendance Update - ").append(dateDisplay).append("\n\n")
        if (absentees.isEmpty()) {
            sb.append("No absentees today. Everyone present!\n")
        } else {
            sb.append("Absentees:\n")
            absentees.forEachIndexed { index, member ->
                sb.append(index + 1).append(". ").append(member.memberId).append(" - ").append(member.name)
                if (!member.mobile.isNullOrBlank()) {
                    sb.append(" (").append(member.mobile).append(")")
                }
                sb.append("\n")
            }
        }
        sb.append("\nTotal Absentees: ").append(absentees.size)
        return sb.toString()
    }
}
