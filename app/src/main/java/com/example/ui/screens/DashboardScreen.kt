package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.navigation.NavItem
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    tasks: List<TaskEntity>,
    schedules: List<ScheduleItemEntity>,
    onAddTaskClick: () -> Unit,
    onGenerateAiScheduleClick: () -> Unit,
    onToggleTaskCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onToggleSubtask: (TaskEntity, String) -> Unit,
    onNavigate: (NavItem) -> Unit,
    parseSubtasks: (String) -> List<SubTask>,
    modifier: Modifier = Modifier
) {
    val totalTasks = tasks.size
    val todayTasks = tasks.filter { it.isDueToday() }
    val completedTasks = tasks.filter { it.status == TaskStatus.SELESAI.displayName }
    val highPriorityTasks = tasks.filter {
        (it.priority == TaskPriority.TINGGI.displayName || it.priority == TaskPriority.MENDESAK.displayName) &&
                it.status != TaskStatus.SELESAI.displayName
    }
    val productivityRate = if (totalTasks > 0) ((completedTasks.size.toFloat() / totalTasks) * 100).toInt() else 0

    val sdfDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = sdfDate.format(Date())
    val todaySchedules = schedules.filter { it.dateStr == todayStr }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onAddTaskClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_add_task_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Tugas", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }

                Button(
                    onClick = onGenerateAiScheduleClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("dashboard_ai_scheduler_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurpleAi)
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Buat Jadwal AI", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // 5 Kartu Statistik Utama
        item {
            Text(
                text = "Ringkasan Produktivitas",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsCard(
                        title = "Total Tugas",
                        value = totalTasks.toString(),
                        icon = Icons.Default.Assignment,
                        iconTint = PrimaryBlue,
                        iconBgColor = Color(0xFFE0F2FE),
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Tugas Hari Ini",
                        value = todayTasks.size.toString(),
                        icon = Icons.Default.Today,
                        iconTint = LightBlue,
                        iconBgColor = Color(0xFFF0F9FF),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsCard(
                        title = "Tugas Selesai",
                        value = completedTasks.size.toString(),
                        icon = Icons.Default.CheckCircle,
                        iconTint = PriorityGreen,
                        iconBgColor = Color(0xFFDCFCE7),
                        modifier = Modifier.weight(1f)
                    )
                    StatsCard(
                        title = "Prioritas Tinggi",
                        value = highPriorityTasks.size.toString(),
                        icon = Icons.Default.PriorityHigh,
                        iconTint = PriorityRed,
                        iconBgColor = Color(0xFFFEE2E2),
                        modifier = Modifier.weight(1f)
                    )
                }

                StatsCard(
                    title = "Persentase Produktivitas",
                    value = "$productivityRate%",
                    subtitle = "${completedTasks.size} dari $totalTasks tugas selesai",
                    icon = Icons.Default.TrendingUp,
                    iconTint = PurpleAi,
                    iconBgColor = Color(0xFFEDE9FE),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Rekomendasi AI Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(NavItem.AI_ASSISTANT) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(PurpleAi),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Rekomendasi CAREAGENT AI",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = PurpleAi
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PurpleAiLight)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("Cerdas", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = PurpleAiDark)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (highPriorityTasks.isNotEmpty()) {
                                "Fokus pada '${highPriorityTasks.first().title}' selama jam produktif pagi untuk hasil optimal."
                            } else {
                                "Semua tugas prioritas terkendali! Gunakan waktu luang untuk relaksasi atau belajar materi baru."
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Buka AI Assistant",
                        tint = PurpleAi
                    )
                }
            }
        }

        // Jadwal Aktivitas Hari Ini
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Jadwal Aktivitas Hari Ini",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onNavigate(NavItem.CALENDAR) }) {
                    Text("Lihat Kalender", fontSize = 12.sp)
                }
            }

            if (todaySchedules.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Outlined.EventAvailable, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Belum ada jadwal untuk hari ini.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        todaySchedules.forEachIndexed { index, schedule ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${schedule.startTimeStr} - ${schedule.endTimeStr}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = schedule.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (schedule.notes.isNotBlank()) {
                                        Text(
                                            text = schedule.notes,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }

                                CategoryBadge(schedule.category)
                            }
                            if (index < todaySchedules.size - 1) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }

        // Tugas Prioritas
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tugas Prioritas Utama",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                TextButton(onClick = { onNavigate(NavItem.TASKS) }) {
                    Text("Semua Tugas", fontSize = 12.sp)
                }
            }
        }

        val priorityList = highPriorityTasks.take(3)
        if (priorityList.isEmpty()) {
            item {
                Text(
                    text = "Tidak ada tugas prioritas tinggi yang tertunda.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(priorityList, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    subtasks = parseSubtasks(task.subtasksJson),
                    onToggleCompletion = { onToggleTaskCompletion(task) },
                    onEdit = { onEditTask(task) },
                    onDelete = { onDeleteTask(task) },
                    onToggleSubtask = { subId -> onToggleSubtask(task, subId) }
                )
            }
        }
    }
}
