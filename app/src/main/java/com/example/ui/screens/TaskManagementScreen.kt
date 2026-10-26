package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.example.data.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

enum class TaskTab(val title: String) {
    SEMUA("Semua Tugas"),
    HARI_INI("Tugas Hari Ini"),
    MENDATANG("Tugas Mendatang"),
    TERLAMBAT("Tugas Terlambat"),
    SELESAI("Tugas Selesai"),
    KATEGORI("Kategori")
}

enum class TaskViewMode(val title: String) {
    LIST("Daftar"),
    TABLE("Tabel"),
    KANBAN("Kanban")
}

enum class TaskSortBy(val title: String) {
    DEADLINE("Tenggat Waktu"),
    PRIORITY("Prioritas"),
    STATUS("Status"),
    NAME("Nama (A-Z)")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskManagementScreen(
    tasks: List<TaskEntity>,
    searchQuery: String,
    onAddTaskClick: () -> Unit,
    onToggleTaskCompletion: (TaskEntity) -> Unit,
    onEditTask: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onToggleSubtask: (TaskEntity, String) -> Unit,
    parseSubtasks: (String) -> List<SubTask>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(TaskTab.SEMUA) }
    var selectedViewMode by remember { mutableStateOf(TaskViewMode.LIST) }
    var sortBy by remember { mutableStateOf(TaskSortBy.DEADLINE) }
    var selectedCategoryFilter by remember { mutableStateOf<String?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }

    val categories = listOf("Semua", "Akademik", "Pekerjaan", "Pribadi", "Kesehatan", "Proyek")

    // Filter tasks based on Tab, Category, and Search Query
    val filteredTasks = remember(tasks, selectedTab, selectedCategoryFilter, searchQuery, sortBy) {
        var result = tasks

        // Tab Filter
        result = when (selectedTab) {
            TaskTab.SEMUA -> result
            TaskTab.HARI_INI -> result.filter { it.isDueToday() }
            TaskTab.MENDATANG -> result.filter { !it.isDueToday() && !it.isOverdue() && it.status != TaskStatus.SELESAI.displayName }
            TaskTab.TERLAMBAT -> result.filter { it.isOverdue() }
            TaskTab.SELESAI -> result.filter { it.status == TaskStatus.SELESAI.displayName }
            TaskTab.KATEGORI -> if (selectedCategoryFilter != null && selectedCategoryFilter != "Semua") {
                result.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
            } else result
        }

        // Search Query Filter
        if (searchQuery.isNotBlank()) {
            val q = searchQuery.lowercase()
            result = result.filter {
                it.title.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.category.lowercase().contains(q) ||
                        it.notes.lowercase().contains(q)
            }
        }

        // Sorting
        when (sortBy) {
            TaskSortBy.DEADLINE -> result.sortedBy { it.dueDate }
            TaskSortBy.PRIORITY -> result.sortedByDescending { TaskPriority.fromString(it.priority).level }
            TaskSortBy.STATUS -> result.sortedBy { it.status }
            TaskSortBy.NAME -> result.sortedBy { it.title.lowercase() }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Manajemen Tugas",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${filteredTasks.size} tugas ditampilkan",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onAddTaskClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier.testTag("tasks_add_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Tugas Baru", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs Scrollable
        ScrollableTabRow(
            selectedTabIndex = selectedTab.ordinal,
            edgePadding = 0.dp,
            divider = {},
            containerColor = Color.Transparent
        ) {
            TaskTab.entries.forEach { tab ->
                val isSelected = selectedTab == tab
                val count = when (tab) {
                    TaskTab.SEMUA -> tasks.size
                    TaskTab.HARI_INI -> tasks.count { it.isDueToday() }
                    TaskTab.MENDATANG -> tasks.count { !it.isDueToday() && !it.isOverdue() && it.status != TaskStatus.SELESAI.displayName }
                    TaskTab.TERLAMBAT -> tasks.count { it.isOverdue() }
                    TaskTab.SELESAI -> tasks.count { it.status == TaskStatus.SELESAI.displayName }
                    TaskTab.KATEGORI -> categories.size - 1
                }

                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = tab },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = tab.title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        if (tab == TaskTab.TERLAMBAT && count > 0) PriorityRed
                                        else if (isSelected) PrimaryBlue
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = count.toString(),
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Category Subfilter if Tab is KATEGORI
        if (selectedTab == TaskTab.KATEGORI) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                items(categories) { cat ->
                    val selected = (selectedCategoryFilter ?: "Semua") == cat
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategoryFilter = if (cat == "Semua") null else cat },
                        label = { Text(cat, fontSize = 11.sp) }
                    )
                }
            }
        }

        // View Mode & Sort Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // View Mode Selector (List, Table, Kanban)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(2.dp)
            ) {
                TaskViewMode.entries.forEach { mode ->
                    val isSelected = selectedViewMode == mode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                            .clickable { selectedViewMode = mode }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mode.title,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Sort Dropdown
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Urutkan: ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                var expandedSort by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expandedSort = true }) {
                        Text(sortBy.title, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(
                        expanded = expandedSort,
                        onDismissRequest = { expandedSort = false }
                    ) {
                        TaskSortBy.entries.forEach { s ->
                            DropdownMenuItem(
                                text = { Text(s.title, fontSize = 12.sp) },
                                onClick = {
                                    sortBy = s
                                    expandedSort = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Main Content based on View Mode
        when (selectedViewMode) {
            TaskViewMode.LIST -> {
                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Checklist, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tidak ada tugas yang cocok dengan filter.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                subtasks = parseSubtasks(task.subtasksJson),
                                onToggleCompletion = { onToggleTaskCompletion(task) },
                                onEdit = { onEditTask(task) },
                                onDelete = { taskToDelete = task },
                                onToggleSubtask = { subId -> onToggleSubtask(task, subId) }
                            )
                        }
                    }
                }
            }
            TaskViewMode.TABLE -> {
                TaskTableView(
                    tasks = filteredTasks,
                    onToggleCompletion = onToggleTaskCompletion,
                    onEdit = onEditTask,
                    onDelete = { taskToDelete = it }
                )
            }
            TaskViewMode.KANBAN -> {
                TaskKanbanView(
                    tasks = filteredTasks,
                    onToggleCompletion = onToggleTaskCompletion,
                    onEdit = onEditTask,
                    onDelete = { taskToDelete = it }
                )
            }
        }
    }

    // Dialog Konfirmasi Hapus Tugas
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = PriorityRed) },
            title = { Text("Konfirmasi Hapus Tugas", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus tugas '${taskToDelete?.title}'? Tindakan ini tidak dapat dibatalkan.") },
            confirmButton = {
                Button(
                    onClick = {
                        taskToDelete?.let { onDeleteTask(it) }
                        taskToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PriorityRed)
                ) {
                    Text("Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { taskToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun TaskTableView(
    tasks: List<TaskEntity>,
    onToggleCompletion: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxSize(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
                Text("Judul Tugas", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
                Text("Kategori", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Prioritas", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(0.8f))
                Text("Tenggat", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.2f))
                Text("Aksi", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(60.dp))
            }

            Spacer(modifier = Modifier.height(6.dp))

            tasks.forEachIndexed { index, task ->
                val isDone = task.status == TaskStatus.SELESAI.displayName
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isDone,
                        onCheckedChange = { onToggleCompletion(task) },
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(36.dp))

                    Text(
                        text = task.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        modifier = Modifier.weight(1.5f)
                    )

                    Box(modifier = Modifier.weight(1f)) {
                        CategoryBadge(task.category)
                    }

                    Box(modifier = Modifier.weight(0.8f)) {
                        PriorityBadge(task.priority)
                    }

                    Text(
                        text = task.formattedDueDate(),
                        fontSize = 11.sp,
                        color = if (task.isOverdue()) PriorityRed else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1.2f)
                    )

                    Row(modifier = Modifier.width(60.dp)) {
                        IconButton(onClick = { onEdit(task) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                        }
                        IconButton(onClick = { onDelete(task) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = PriorityRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
                if (index < tasks.size - 1) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        }
    }
}

@Composable
fun TaskKanbanView(
    tasks: List<TaskEntity>,
    onToggleCompletion: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    val todoTasks = tasks.filter { it.status == TaskStatus.BELUM_DIMULAI.displayName }
    val inProgressTasks = tasks.filter { it.status == TaskStatus.SEDANG_DIKERJAKAN.displayName }
    val doneTasks = tasks.filter { it.status == TaskStatus.SELESAI.displayName }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KanbanColumn(
            title = "Belum Dimulai",
            badgeColor = Color(0xFF64748B),
            tasks = todoTasks,
            onToggleCompletion = onToggleCompletion,
            onEdit = onEdit,
            onDelete = onDelete
        )
        KanbanColumn(
            title = "Sedang Dikerjakan",
            badgeColor = PurpleAi,
            tasks = inProgressTasks,
            onToggleCompletion = onToggleCompletion,
            onEdit = onEdit,
            onDelete = onDelete
        )
        KanbanColumn(
            title = "Selesai",
            badgeColor = PriorityGreen,
            tasks = doneTasks,
            onToggleCompletion = onToggleCompletion,
            onEdit = onEdit,
            onDelete = onDelete
        )
    }
}

@Composable
fun KanbanColumn(
    title: String,
    badgeColor: Color,
    tasks: List<TaskEntity>,
    onToggleCompletion: (TaskEntity) -> Unit,
    onEdit: (TaskEntity) -> Unit,
    onDelete: (TaskEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .width(260.dp)
            .fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = tasks.size.toString(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasks.forEach { task ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                CategoryBadge(task.category)
                                PriorityBadge(task.priority)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = task.title, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Tenggat: ${task.formattedDueDate()}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                IconButton(onClick = { onEdit(task) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Edit, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                }
                                IconButton(onClick = { onDelete(task) }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = null, tint = PriorityRed, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
