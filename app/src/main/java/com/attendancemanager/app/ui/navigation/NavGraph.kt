package com.attendancemanager.app.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.attendancemanager.app.ui.screens.AddEditMemberScreen
import com.attendancemanager.app.ui.screens.AttendanceScreen
import com.attendancemanager.app.ui.screens.HomeScreen
import com.attendancemanager.app.ui.screens.MemberHistoryScreen
import com.attendancemanager.app.ui.screens.MembersScreen
import com.attendancemanager.app.ui.screens.MoreScreen
import com.attendancemanager.app.ui.screens.ReportsScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { AppBottomBar(navController) }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.HOME) { HomeScreen() }
            composable(Routes.ATTENDANCE) { AttendanceScreen() }
            composable(Routes.MEMBERS) { MembersScreen(navController) }
            composable(Routes.REPORTS) { ReportsScreen(navController) }
            composable(Routes.MORE) { MoreScreen() }

            composable(Routes.ADD_MEMBER) { AddEditMemberScreen(navController, memberId = null) }

            composable(
                route = Routes.EDIT_MEMBER,
                arguments = listOf(navArgument("memberId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val memberId = backStackEntry.arguments?.getString("memberId") ?: return@composable
                AddEditMemberScreen(navController, memberId = memberId)
            }

            composable(
                route = Routes.MEMBER_HISTORY,
                arguments = listOf(navArgument("memberId") { type = androidx.navigation.NavType.StringType })
            ) { backStackEntry ->
                val memberId = backStackEntry.arguments?.getString("memberId") ?: return@composable
                MemberHistoryScreen(navController, memberId = memberId)
            }
        }
    }
}

@Composable
private fun AppBottomBar(navController: androidx.navigation.NavController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Only show the bottom bar on the five top-level tabs, not on Add/Edit/History screens.
    val topLevelRoutes = bottomNavItems.map { it.route }.toSet()
    val isTopLevel = currentDestination?.route in topLevelRoutes

    if (!isTopLevel) return

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
