package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.SubTask
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDialog(
    initialTask: TaskEntity?,
    initialSubtasks: List<SubTask>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        description: String,
        category: String,
        dueDate: Long,
        dueTimeStr: String,
        estimatedDurationMinutes: Int,
        priority: String,
        status: String,
        progress: Int,
        subtasks: List<SubTask>,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf(initialTask?.title ?: "") }
    var description by remember { mutableStateOf(initialTask?.description ?: "") }
    var category by remember { mutableStateOf(initialTask?.category ?: "Akademik") }
    var dueDate by remember { mutableLongStateOf(initialTask?.dueDate ?: (System.currentTimeMillis() + 86400000L)) }
    var dueTimeStr by remember { mutableStateOf(initialTask?.dueTimeStr ?: "17:00") }
    var estimatedDuration by remember { mutableIntStateOf(initialTask?.estimatedDurationMinutes ?: 60) }
    var priority by remember { mutableStateOf(initialTask?.priority ?: TaskPriority.SEDANG.displayName) }
    var status by remember { mutableStateOf(initialTask?.status ?: TaskStatus.BELUM_DIMULAI.displayName) }
    var progress by remember { mutableIntStateOf(initialTask?.progress ?: 0) }
    var notes by remember { mutableStateOf(initialTask?.notes ?: "") }

    var subtasks by remember { mutableStateOf(initialSubtasks) }
    var newSubtaskTitle by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Akademik", "Pekerjaan", "Pribadi", "Kesehatan", "Proyek", "Umum")
    val priorities = listOf(
        TaskPriority.RENDAH.displayName,
        TaskPriority.SEDANG.displayName,
        TaskPriority.TINGGI.displayName,
        TaskPriority.MENDESAK.displayName
    )
    val statuses = listOf(
        TaskStatus.BELUM_DIMULAI.displayName,
        TaskStatus.SEDANG_DIKERJAKAN.displayName,
        TaskStatus.SELESAI.displayName
    )

    val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialTask == null) "Tambah Tugas Baru" else "Edit Tugas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Error Banner
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = PriorityRed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(errorMessage!!, color = PriorityRed, fontSize = 12.sp)
                        }
                    }
                }

                // Scrollable Form Fields
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Judul Tugas
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (it.isNotBlank()) errorMessage = null
                        },
                        label = { Text("Judul Tugas *") },
                        placeholder = { Text("Contoh: Menyelesaikan Laporan Akhir...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task_form_title_input"),
                        singleLine = true
                    )

                    // Deskripsi
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Deskripsi") },
                        placeholder = { Text("Rincian tugas atau tujuan yang ingin dicapai...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .testTag("task_form_desc_input"),
                        maxLines = 4
                    )

                    // Kategori Chips
                    Column {
                        Text("Kategori", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.take(4).forEach { cat ->
                                val selected = category == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { category = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Prioritas & Status Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Prioritas", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            priorities.forEach { prio ->
                                val selected = priority == prio
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { priority = prio }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(selected = selected, onClick = { priority = prio })
                                    PriorityBadge(prio)
                                }
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text("Status", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            statuses.forEach { st ->
                                val selected = status == st
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            status = st
                                            if (st == TaskStatus.SELESAI.displayName) progress = 100
                                            else if (st == TaskStatus.BELUM_DIMULAI.displayName && progress == 100) progress = 0
                                        }
                                        .padding(vertical = 2.dp)
                                ) {
                                    RadioButton(selected = selected, onClick = {
                                        status = st
                                        if (st == TaskStatus.SELESAI.displayName) progress = 100
                                        else if (st == TaskStatus.BELUM_DIMULAI.displayName && progress == 100) progress = 0
                                    })
                                    Text(st, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    // Tenggat Waktu & Estimasi Durasi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = dueTimeStr,
                            onValueChange = { dueTimeStr = it },
                            label = { Text("Jam Tenggat (HH:mm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = estimatedDuration.toString(),
                            onValueChange = { estimatedDuration = it.toIntOrNull() ?: 60 },
                            label = { Text("Durasi (menit)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Progres Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Progres Penyelesaian", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("$progress%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }
                        Slider(
                            value = progress.toFloat(),
                            onValueChange = {
                                progress = it.toInt()
                                if (progress == 100) status = TaskStatus.SELESAI.displayName
                                else if (status == TaskStatus.SELESAI.displayName && progress < 100) status = TaskStatus.SEDANG_DIKERJAKAN.displayName
                            },
                            valueRange = 0f..100f
                        )
                    }

                    // Subtasks
                    Column {
                        Text("Daftar Subtask", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))

                        subtasks.forEachIndexed { index, sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = sub.isCompleted,
                                    onCheckedChange = { checked ->
                                        subtasks = subtasks.mapIndexed { i, s ->
                                            if (i == index) s.copy(isCompleted = checked) else s
                                        }
                                    }
                                )
                                Text(
                                    text = sub.title,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    subtasks = subtasks.filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newSubtaskTitle,
                                onValueChange = { newSubtaskTitle = it },
                                placeholder = { Text("Tambah subtask baru...", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilledTonalButton(
                                onClick = {
                                    if (newSubtaskTitle.isNotBlank()) {
                                        subtasks = subtasks + SubTask(UUID.randomUUID().toString(), newSubtaskTitle.trim(), false)
                                        newSubtaskTitle = ""
                                    }
                                }
                            ) {
                                Text("Tambah")
                            }
                        }
                    }

                    // Catatan Tambahan
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan") },
                        placeholder = { Text("Tautan, referensi, atau instruksi khusus...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                // Footer Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Batal")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                errorMessage = "Judul tugas tidak boleh kosong!"
                            } else {
                                onSave(
                                    initialTask?.id ?: 0L,
                                    title,
                                    description,
                                    category,
                                    dueDate,
                                    dueTimeStr,
                                    estimatedDuration,
                                    priority,
                                    status,
                                    progress,
                                    subtasks,
                                    notes
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.testTag("task_form_save_button")
                    ) {
                        Text("Simpan Tugas", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
