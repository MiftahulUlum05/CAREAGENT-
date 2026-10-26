package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class NavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val isAiFeature: Boolean = false
) {
    DASHBOARD("dashboard", "Dashboard", Icons.Filled.Dashboard, Icons.Outlined.Dashboard),
    TASKS("tasks", "Manajemen Tugas", Icons.Filled.CheckCircle, Icons.Outlined.CheckCircle),
    CALENDAR("calendar", "Kalender Aktivitas", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    SMART_SCHEDULER("smart_scheduler", "Smart Scheduler", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, isAiFeature = true),
    AI_ASSISTANT("ai_assistant", "AI Assistant", Icons.Filled.Psychology, Icons.Outlined.Psychology, isAiFeature = true),
    REMINDERS("reminders", "Pengingat", Icons.Filled.Notifications, Icons.Outlined.Notifications),
    ANALYTICS("analytics", "Analitik", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    NOTES("notes", "Catatan & Ide", Icons.Filled.Lightbulb, Icons.Outlined.Lightbulb),
    SETTINGS("settings", "Pengaturan", Icons.Filled.Settings, Icons.Outlined.Settings)
}
