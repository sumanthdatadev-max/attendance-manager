package com.attendancemanager.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val HOME = "home"
    const val ATTENDANCE = "attendance"
    const val MEMBERS = "members"
    const val REPORTS = "reports"
    const val MORE = "more"

    const val ADD_MEMBER = "add_member"
    const val EDIT_MEMBER = "edit_member/{memberId}"
    const val MEMBER_HISTORY = "member_history/{memberId}"

    fun editMember(memberId: String) = "edit_member/$memberId"
    fun memberHistory(memberId: String) = "member_history/$memberId"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Filled.Home),
    BottomNavItem(Routes.ATTENDANCE, "Attendance", Icons.Filled.CalendarMonth),
    BottomNavItem(Routes.MEMBERS, "Members", Icons.Filled.Groups),
    BottomNavItem(Routes.REPORTS, "Reports", Icons.Filled.Assessment),
    BottomNavItem(Routes.MORE, "More", Icons.Filled.MoreHoriz)
)
