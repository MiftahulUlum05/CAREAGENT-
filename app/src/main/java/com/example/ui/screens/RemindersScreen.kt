package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.data.model.UserSettings
import com.example.ui.components.CategoryBadge
import com.example.ui.components.PriorityBadge
import com.example.ui.theme.*

@Composable
fun RemindersScreen(
    tasks: List<TaskEntity>,
    userSettings: UserSettings,
    onToggleCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val overdueTasks = tasks.filter { it.isOverdue() }
    val dueSoonTasks = tasks.filter {
        !it.isOverdue() && it.status != TaskStatus.SELESAI.displayName &&
                (it.dueDate - System.currentTimeMillis() <= 86400000L * 2)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrimaryBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Pengingat & Notifikasi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Status: ${if (userSettings.notificationsEnabled) "Notifikasi Aktif" else "Notifikasi Dinonaktifkan"}",
                        fontSize = 12.sp,
                        color = if (userSettings.notificationsEnabled) PriorityGreen else PriorityRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Overdue Section
        if (overdueTasks.isNotEmpty()) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = PriorityRed, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Tugas Terlambat (${overdueTasks.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PriorityRed
                    )
                }
            }

            items(overdueTasks, key = { "overdue_${it.id}" }) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { onToggleCompletion(task) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CategoryBadge(task.category)
                                PriorityBadge(task.priority)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF991B1B))
                            Text("Tenggat: ${task.formattedDueDate()}", fontSize = 11.sp, color = PriorityRed, fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { onEditTask(task) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                        }
                    }
                }
            }
        }

        // Due Soon Section
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = PriorityAmber, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mendekati Tenggat Waktu (${dueSoonTasks.size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (dueSoonTasks.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text("Tidak ada tugas mendesak dalam 24 jam ke depan.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        } else {
            items(dueSoonTasks, key = { "due_${it.id}" }) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = false,
                            onCheckedChange = { onToggleCompletion(task) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                CategoryBadge(task.category)
                                PriorityBadge(task.priority)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(task.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("Tenggat: ${task.formattedDueDate()}", fontSize = 11.sp, color = PriorityAmber, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { onEditTask(task) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue)
                        }
                    }
                }
            }
        }
    }
}
