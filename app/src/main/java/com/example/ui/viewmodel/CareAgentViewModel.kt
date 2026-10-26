package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.CareAgentRepository
import com.example.ui.navigation.NavItem
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class CareAgentViewModel(application: Application) : AndroidViewModel(application) {
    val repository = CareAgentRepository(application)

    private val _currentScreen = MutableStateFlow(NavItem.DASHBOARD)
    val currentScreen: StateFlow<NavItem> = _currentScreen.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings: StateFlow<UserSettings> = _userSettings.asStateFlow()

    val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSchedules: StateFlow<List<ScheduleItemEntity>> = repository.getAllSchedules()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteEntity>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _proposedSchedules = MutableStateFlow<List<ScheduleItemEntity>>(emptyList())
    val proposedSchedules: StateFlow<List<ScheduleItemEntity>> = _proposedSchedules.asStateFlow()

    private val _isAiGenerating = MutableStateFlow(false)
    val isAiGenerating: StateFlow<Boolean> = _isAiGenerating.asStateFlow()

    private val _aiChatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                id = "welcome",
                sender = "ai",
                text = "Halo! Saya **CAREAGENT AI**, asisten cerdas pengelola aktivitas dan tugas Anda. Bagaimana saya dapat membantu meningkatkan produktivitas Anda hari ini?",
                suggestedActionType = "menu"
            )
        )
    )
    val aiChatMessages: StateFlow<List<ChatMessage>> = _aiChatMessages.asStateFlow()

    private val _snackbarMessage = MutableStateFlow<String?>(null)
    val snackbarMessage: StateFlow<String?> = _snackbarMessage.asStateFlow()

    private val _editingTask = MutableStateFlow<TaskEntity?>(null)
    val editingTask: StateFlow<TaskEntity?> = _editingTask.asStateFlow()

    private val _showTaskDialog = MutableStateFlow(false)
    val showTaskDialog: StateFlow<Boolean> = _showTaskDialog.asStateFlow()

    init {
        viewModelScope.launch {
            repository.checkAndSeedInitialData()
        }
    }

    fun navigateTo(screen: NavItem) {
        _currentScreen.value = screen
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openAddTaskDialog() {
        _editingTask.value = null
        _showTaskDialog.value = true
    }

    fun openEditTaskDialog(task: TaskEntity) {
        _editingTask.value = task
        _showTaskDialog.value = true
    }

    fun closeTaskDialog() {
        _showTaskDialog.value = false
        _editingTask.value = null
    }

    fun saveTask(
        id: Long = 0,
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
    ) {
        viewModelScope.launch {
            val subtasksJson = repository.serializeSubtasks(subtasks)
            val now = System.currentTimeMillis()
            val task = TaskEntity(
                id = id,
                title = title.trim(),
                description = description.trim(),
                category = category,
                dueDate = dueDate,
                dueTimeStr = dueTimeStr,
                estimatedDurationMinutes = estimatedDurationMinutes,
                priority = priority,
                status = status,
                progress = progress,
                subtasksJson = subtasksJson,
                notes = notes.trim(),
                updatedAt = now,
                createdAt = if (id == 0L) now else now
            )
            if (id == 0L) {
                repository.insertTask(task)
                _snackbarMessage.value = "Tugas '${task.title}' berhasil ditambahkan!"
            } else {
                repository.updateTask(task)
                _snackbarMessage.value = "Tugas '${task.title}' berhasil diperbarui!"
            }
            closeTaskDialog()
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            _snackbarMessage.value = "Tugas '${task.title}' telah dihapus."
        }
    }

    fun toggleTaskCompletion(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = if (task.status == TaskStatus.SELESAI.displayName) {
                TaskStatus.BELUM_DIMULAI.displayName
            } else {
                TaskStatus.SELESAI.displayName
            }
            val newProgress = if (newStatus == TaskStatus.SELESAI.displayName) 100 else 0
            val updated = task.copy(status = newStatus, progress = newProgress, updatedAt = System.currentTimeMillis())
            repository.updateTask(updated)
            _snackbarMessage.value = if (newStatus == TaskStatus.SELESAI.displayName) {
                "Hebat! Tugas '${task.title}' selesai! 🎉"
            } else {
                "Status tugas '${task.title}' diperbarui."
            }
        }
    }

    fun toggleSubtask(task: TaskEntity, subtaskId: String) {
        viewModelScope.launch {
            val currentList = repository.parseSubtasks(task.subtasksJson)
            val updatedList = currentList.map {
                if (it.id == subtaskId) it.copy(isCompleted = !it.isCompleted) else it
            }
            val completedCount = updatedList.count { it.isCompleted }
            val newProgress = if (updatedList.isNotEmpty()) {
                ((completedCount.toFloat() / updatedList.size) * 100).toInt()
            } else {
                task.progress
            }
            val newStatus = when {
                newProgress == 100 -> TaskStatus.SELESAI.displayName
                newProgress > 0 -> TaskStatus.SEDANG_DIKERJAKAN.displayName
                else -> task.status
            }
            val updatedTask = task.copy(
                subtasksJson = repository.serializeSubtasks(updatedList),
                progress = newProgress,
                status = newStatus,
                updatedAt = System.currentTimeMillis()
            )
            repository.updateTask(updatedTask)
        }
    }

    fun saveSchedule(item: ScheduleItemEntity) {
        viewModelScope.launch {
            if (item.id == 0L) {
                repository.insertSchedule(item)
                _snackbarMessage.value = "Aktivitas '${item.title}' ditambahkan ke jadwal!"
            } else {
                repository.updateSchedule(item)
                _snackbarMessage.value = "Jadwal '${item.title}' diperbarui!"
            }
        }
    }

    fun deleteSchedule(item: ScheduleItemEntity) {
        viewModelScope.launch {
            repository.deleteSchedule(item)
            _snackbarMessage.value = "Aktivitas '${item.title}' dihapus dari jadwal."
        }
    }

    fun generateSmartScheduleWithAI(userPreferenceNote: String = "") {
        viewModelScope.launch {
            _isAiGenerating.value = true
            val tasks = allTasks.value.filter { it.status != TaskStatus.SELESAI.displayName }
            val existingSchedules = allSchedules.value

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val cal = Calendar.getInstance()
            val todayStr = sdf.format(cal.time)
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val tomorrowStr = sdf.format(cal.time)

            // Algoritma Smart Scheduler: Urutkan berdasarkan level prioritas & tenggat waktu
            val sortedTasks = tasks.sortedWith(
                compareByDescending<TaskEntity> { TaskPriority.fromString(it.priority).level }
                    .thenBy { it.dueDate }
            )

            val proposals = mutableListOf<ScheduleItemEntity>()
            val availableSlots = listOf(
                "08:30" to "10:00",
                "10:15" to "11:45",
                "13:30" to "15:00",
                "15:15" to "16:45",
                "19:00" to "20:30"
            )

            var slotIdx = 0
            var targetDate = todayStr

            for (task in sortedTasks.take(5)) {
                if (slotIdx >= availableSlots.size) {
                    slotIdx = 0
                    targetDate = tomorrowStr
                }
                val slot = availableSlots[slotIdx]

                // Deteksi bentrok dengan jadwal yang sudah ada
                val isOccupied = existingSchedules.any {
                    it.dateStr == targetDate && it.startTimeStr == slot.first
                }

                val finalSlot = if (isOccupied && slotIdx + 1 < availableSlots.size) {
                    slotIdx++
                    availableSlots[slotIdx]
                } else slot

                proposals.add(
                    ScheduleItemEntity(
                        taskId = task.id,
                        title = "Fokus: ${task.title}",
                        category = task.category,
                        dateStr = targetDate,
                        startTimeStr = finalSlot.first,
                        endTimeStr = finalSlot.second,
                        notes = "Usulan AI: Prioritas ${task.priority}, durasi est. ${task.estimatedDurationMinutes} mnt.",
                        isAIProposed = true,
                        isApproved = false
                    )
                )
                slotIdx++
            }

            _proposedSchedules.value = proposals
            _isAiGenerating.value = false
            _snackbarMessage.value = "Smart Scheduler AI menyusun ${proposals.size} usulan slot waktu!"
        }
    }

    fun approveProposedSchedule(item: ScheduleItemEntity) {
        viewModelScope.launch {
            val approved = item.copy(isApproved = true, isAIProposed = false)
            repository.insertSchedule(approved)
            _proposedSchedules.value = _proposedSchedules.value.filter { it != item }
            _snackbarMessage.value = "Jadwal '${item.title}' disetujui & disimpan!"
        }
    }

    fun approveAllProposedSchedules() {
        viewModelScope.launch {
            val items = _proposedSchedules.value.map { it.copy(isApproved = true, isAIProposed = false) }
            repository.insertSchedules(items)
            _proposedSchedules.value = emptyList()
            _snackbarMessage.value = "Semua usulan jadwal berhasil disetujui & ditambahkan!"
        }
    }

    fun rejectProposedSchedules() {
        _proposedSchedules.value = emptyList()
        _snackbarMessage.value = "Usulan jadwal telah ditolak."
    }

    fun sendAiChatMessage(userText: String) {
        if (userText.isBlank()) return
        val userMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            sender = "user",
            text = userText
        )
        _aiChatMessages.value = _aiChatMessages.value + userMsg

        viewModelScope.launch {
            _isAiGenerating.value = true
            val reply = repository.callGeminiAi(userText, _userSettings.value.customApiKey)
            val aiMsg = ChatMessage(
                id = UUID.randomUUID().toString(),
                sender = "ai",
                text = reply,
                isActionable = reply.contains("jadwal") || reply.contains("subtask"),
                suggestedActionType = if (reply.contains("subtask")) "breakdown" else if (reply.contains("jadwal")) "schedule" else null
            )
            _aiChatMessages.value = _aiChatMessages.value + aiMsg
            _isAiGenerating.value = false
        }
    }

    fun saveNote(title: String, content: String, category: String, colorHex: String = "#1E293B", id: Long = 0) {
        viewModelScope.launch {
            val note = NoteEntity(
                id = id,
                title = title.trim(),
                content = content.trim(),
                category = category,
                colorHex = colorHex,
                updatedAt = System.currentTimeMillis()
            )
            if (id == 0L) {
                repository.insertNote(note)
                _snackbarMessage.value = "Catatan '${note.title}' berhasil disimpan!"
            } else {
                repository.updateNote(note)
                _snackbarMessage.value = "Catatan '${note.title}' diperbarui!"
            }
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            repository.deleteNote(note)
            _snackbarMessage.value = "Catatan '${note.title}' dihapus."
        }
    }

    fun convertNoteToTask(note: NoteEntity) {
        viewModelScope.launch {
            val newTask = TaskEntity(
                title = note.title,
                description = note.content,
                category = if (note.category.isNotBlank()) note.category else "Ide",
                priority = TaskPriority.SEDANG.displayName,
                status = TaskStatus.BELUM_DIMULAI.displayName,
                progress = 0,
                notes = "Dibuat dari catatan ide."
            )
            repository.insertTask(newTask)
            _snackbarMessage.value = "Catatan '${note.title}' berhasil diubah menjadi tugas!"
        }
    }

    fun updateSettings(settings: UserSettings) {
        _userSettings.value = settings
        _snackbarMessage.value = "Pengaturan berhasil disimpan."
    }

    fun dismissSnackbar() {
        _snackbarMessage.value = null
    }
}
