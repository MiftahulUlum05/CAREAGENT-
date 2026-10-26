package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ScheduleItemEntity
import com.example.data.model.TaskEntity
import com.example.ui.components.CategoryBadge
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarViewMode(val title: String) {
    HARIAN("Harian"),
    MINGGUAN("Mingguan"),
    BULANAN("Bulanan")
}

@Composable
fun CalendarScreen(
    schedules: List<ScheduleItemEntity>,
    tasks: List<TaskEntity>,
    onSaveSchedule: (ScheduleItemEntity) -> Unit,
    onDeleteSchedule: (ScheduleItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var viewMode by remember { mutableStateOf(CalendarViewMode.HARIAN) }
    var selectedCalendar by remember { mutableStateOf(Calendar.getInstance()) }
    var showAddDialog by remember { mutableStateOf(false) }

    val sdfDisplay = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
    val sdfDateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val selectedDateStr = sdfDateKey.format(selectedCalendar.time)

    // Conflict detection
    val currentDaySchedules = schedules.filter { it.dateStr == selectedDateStr }
    val conflicts = remember(currentDaySchedules) {
        val detected = mutableListOf<Pair<ScheduleItemEntity, ScheduleItemEntity>>()
        for (i in currentDaySchedules.indices) {
            for (j in i + 1 until currentDaySchedules.size) {
                if (currentDaySchedules[i].conflictsWith(currentDaySchedules[j])) {
                    detected.add(currentDaySchedules[i] to currentDaySchedules[j])
                }
            }
        }
        detected
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Bar: Month Navigation & Mode Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Kalender Aktivitas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sdfDisplay.format(selectedCalendar.time),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Button(
                onClick = { showAddDialog = true },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.testTag("calendar_add_schedule_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Aktivitas", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // View Mode Switcher
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(2.dp)
        ) {
            CalendarViewMode.entries.forEach { mode ->
                val isSelected = viewMode == mode
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                        .clickable { viewMode = mode }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = mode.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Schedule Conflict Alert Banner
        if (conflicts.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Bentrok", tint = PriorityRed)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Deteksi Bentrokan Jadwal!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = PriorityRed
                        )
                        conflicts.forEach { (a, b) ->
                            Text(
                                text = "• '${a.title}' (${a.startTimeStr}-${a.endTimeStr}) bertabrakan dengan '${b.title}' (${b.startTimeStr}-${b.endTimeStr})",
                                fontSize = 11.sp,
                                color = Color(0xFF7F1D1D)
                            )
                        }
                    }
                }
            }
        }

        when (viewMode) {
            CalendarViewMode.HARIAN -> {
                // Day View
                DayViewContent(
                    selectedDateStr = selectedDateStr,
                    schedules = currentDaySchedules,
                    tasks = tasks.filter { it.isDueToday() },
                    onPrevDay = {
                        val c = selectedCalendar.clone() as Calendar
                        c.add(Calendar.DAY_OF_YEAR, -1)
                        selectedCalendar = c
                    },
                    onNextDay = {
                        val c = selectedCalendar.clone() as Calendar
                        c.add(Calendar.DAY_OF_YEAR, 1)
                        selectedCalendar = c
                    },
                    onDeleteSchedule = onDeleteSchedule
                )
            }
            CalendarViewMode.MINGGUAN -> {
                WeekViewContent(
                    calendar = selectedCalendar,
                    schedules = schedules,
                    onSelectDay = { dayCal ->
                        selectedCalendar = dayCal
                        viewMode = CalendarViewMode.HARIAN
                    }
                )
            }
            CalendarViewMode.BULANAN -> {
                MonthViewContent(
                    calendar = selectedCalendar,
                    schedules = schedules,
                    onSelectDay = { dayCal ->
                        selectedCalendar = dayCal
                        viewMode = CalendarViewMode.HARIAN
                    },
                    onPrevMonth = {
                        val c = selectedCalendar.clone() as Calendar
                        c.add(Calendar.MONTH, -1)
                        selectedCalendar = c
                    },
                    onNextMonth = {
                        val c = selectedCalendar.clone() as Calendar
                        c.add(Calendar.MONTH, 1)
                        selectedCalendar = c
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddScheduleDialog(
            defaultDateStr = selectedDateStr,
            onDismiss = { showAddDialog = false },
            onSave = { item ->
                onSaveSchedule(item)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun DayViewContent(
    selectedDateStr: String,
    schedules: List<ScheduleItemEntity>,
    tasks: List<TaskEntity>,
    onPrevDay: () -> Unit,
    onNextDay: () -> Unit,
    onDeleteSchedule: (ScheduleItemEntity) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevDay) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Hari Sebelumnya")
            }
            Text(
                text = selectedDateStr,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = onNextDay) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Hari Berikutnya")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (schedules.isEmpty() && tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Text(
                    text = "Tidak ada aktivitas atau tenggat waktu untuk tanggal ini.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (tasks.isNotEmpty()) {
                    item {
                        Text("Tenggat Tugas Hari Ini", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PriorityRed)
                    }
                    items(tasks) { t ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Flag, contentDescription = null, tint = PriorityRed, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(t.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PriorityRed)
                                    Text("Pukul ${t.dueTimeStr} • ${t.category}", fontSize = 11.sp, color = Color(0xFF991B1B))
                                }
                            }
                        }
                    }
                }

                if (schedules.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Aktivitas Terjadwal", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                    items(schedules) { sch ->
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
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(PrimaryBlue.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${sch.startTimeStr} - ${sch.endTimeStr}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PrimaryBlue
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sch.title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    if (sch.notes.isNotBlank()) {
                                        Text(sch.notes, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                CategoryBadge(sch.category)

                                IconButton(onClick = { onDeleteSchedule(sch) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeekViewContent(
    calendar: Calendar,
    schedules: List<ScheduleItemEntity>,
    onSelectDay: (Calendar) -> Unit
) {
    val daysOfWeek = listOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

    val weekDays = (0..6).map {
        val d = cal.clone() as Calendar
        cal.add(Calendar.DAY_OF_YEAR, 1)
        d
    }

    val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        weekDays.forEachIndexed { idx, dayCal ->
            val dateKey = sdfKey.format(dayCal.time)
            val daySchedules = schedules.filter { it.dateStr == dateKey }
            val dayNum = dayCal.get(Calendar.DAY_OF_MONTH)

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectDay(dayCal) },
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(PrimaryBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(daysOfWeek[idx], fontSize = 10.sp, color = PrimaryBlue)
                            Text(dayNum.toString(), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (daySchedules.isEmpty()) "Tidak ada aktivitas" else "${daySchedules.size} aktivitas terjadwal",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (daySchedules.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                        )
                        daySchedules.take(2).forEach { s ->
                            Text(
                                text = "• ${s.startTimeStr} ${s.title}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }

                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun MonthViewContent(
    calendar: Calendar,
    schedules: List<ScheduleItemEntity>,
    onSelectDay: (Calendar) -> Unit,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val cal = calendar.clone() as Calendar
    cal.set(Calendar.DAY_OF_MONTH, 1)
    val monthDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sunday, 2 = Monday

    val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Bulan Lalu")
            }
            val sdfMonthYear = SimpleDateFormat("MMMM yyyy", Locale("id", "ID"))
            Text(sdfMonthYear.format(calendar.time), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Bulan Depan")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf("Min", "Sen", "Sel", "Rab", "Kam", "Jum", "Sab").forEach {
                Text(it, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Empty offset cells
            val offset = firstDayOfWeek - 1
            items(offset) {
                Box(modifier = Modifier.size(40.dp))
            }

            items(monthDays) { dayIdx ->
                val day = dayIdx + 1
                val dayCal = calendar.clone() as Calendar
                dayCal.set(Calendar.DAY_OF_MONTH, day)
                val key = sdfKey.format(dayCal.time)
                val count = schedules.count { it.dateStr == key }

                Box(
                    modifier = Modifier
                        .height(50.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (count > 0) PrimaryBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                        .clickable { onSelectDay(dayCal) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(day.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (count > 0) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryBlue)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddScheduleDialog(
    defaultDateStr: String,
    onDismiss: () -> Unit,
    onSave: (ScheduleItemEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Aktivitas") }
    var dateStr by remember { mutableStateOf(defaultDateStr) }
    var startTimeStr by remember { mutableStateOf("09:00") }
    var endTimeStr by remember { mutableStateOf("10:30") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Aktivitas Baru", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (error != null) {
                    Text(error!!, color = PriorityRed, fontSize = 12.sp)
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Nama Aktivitas *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dateStr,
                    onValueChange = { dateStr = it },
                    label = { Text("Tanggal (yyyy-MM-dd)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTimeStr,
                        onValueChange = { startTimeStr = it },
                        label = { Text("Mulai") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTimeStr,
                        onValueChange = { endTimeStr = it },
                        label = { Text("Selesai") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        error = "Nama aktivitas tidak boleh kosong!"
                    } else {
                        onSave(
                            ScheduleItemEntity(
                                title = title.trim(),
                                category = category,
                                dateStr = dateStr,
                                startTimeStr = startTimeStr,
                                endTimeStr = endTimeStr,
                                notes = notes.trim(),
                                isApproved = true
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("Simpan", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
