package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskStatus
import com.example.ui.components.CareAgentDrawer
import com.example.ui.components.CareAgentHeader
import com.example.ui.components.TaskDialog
import com.example.ui.navigation.NavItem
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.CareAgentViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: CareAgentViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val userSettings by viewModel.userSettings.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val schedules by viewModel.allSchedules.collectAsState()
    val notes by viewModel.allNotes.collectAsState()
    val proposedSchedules by viewModel.proposedSchedules.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val aiChatMessages by viewModel.aiChatMessages.collectAsState()
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val showTaskDialog by viewModel.showTaskDialog.collectAsState()
    val editingTask by viewModel.editingTask.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Overdue and upcoming reminder badge count
    val notificationCount = remember(tasks) {
        tasks.count { it.isOverdue() || (it.dueDate - System.currentTimeMillis() <= 86400000L && it.status != TaskStatus.SELESAI.displayName) }
    }

    // Handle BackHandler: return to Dashboard if on a subscreen
    BackHandler(enabled = currentScreen != NavItem.DASHBOARD || drawerState.isOpen) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else {
            viewModel.navigateTo(NavItem.DASHBOARD)
        }
    }

    // Snackbar effect
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                CareAgentDrawer(
                    currentScreen = currentScreen,
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                        coroutineScope.launch { drawerState.close() }
                    },
                    onAddTaskClick = {
                        coroutineScope.launch { drawerState.close() }
                        viewModel.openAddTaskDialog()
                    },
                    userSettings = userSettings,
                    onToggleTheme = {
                        viewModel.updateSettings(userSettings.copy(isDarkMode = !userSettings.isDarkMode))
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                CareAgentHeader(
                    userSettings = userSettings,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { viewModel.setSearchQuery(it) },
                    notificationCount = notificationCount,
                    onNotificationClick = { viewModel.navigateTo(NavItem.REMINDERS) },
                    onProfileClick = { viewModel.navigateTo(NavItem.SETTINGS) },
                    onMenuToggle = {
                        coroutineScope.launch {
                            if (drawerState.isOpen) drawerState.close() else drawerState.open()
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp
                ) {
                    val bottomItems = listOf(
                        NavItem.DASHBOARD,
                        NavItem.TASKS,
                        NavItem.CALENDAR,
                        NavItem.SMART_SCHEDULER,
                        NavItem.AI_ASSISTANT
                    )
                    bottomItems.forEach { item ->
                        val selected = currentScreen == item
                        NavigationBarItem(
                            selected = selected,
                            onClick = { viewModel.navigateTo(item) },
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title,
                                    tint = if (selected) {
                                        if (item.isAiFeature) PurpleAi else MaterialTheme.colorScheme.primary
                                    } else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            label = {
                                Text(
                                    text = item.title,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    color = if (selected) {
                                        if (item.isAiFeature) PurpleAi else MaterialTheme.colorScheme.primary
                                    } else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            modifier = Modifier.testTag("bottom_nav_${item.route}")
                        )
                    }
                }
            },
            floatingActionButton = {
                if (currentScreen == NavItem.TASKS || currentScreen == NavItem.DASHBOARD) {
                    FloatingActionButton(
                        onClick = { viewModel.openAddTaskDialog() },
                        containerColor = PrimaryBlue,
                        contentColor = Color.White,
                        modifier = Modifier.testTag("main_fab_add_task")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Tambah Tugas")
                    }
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (currentScreen) {
                    NavItem.DASHBOARD -> DashboardScreen(
                        tasks = tasks,
                        schedules = schedules,
                        onAddTaskClick = { viewModel.openAddTaskDialog() },
                        onGenerateAiScheduleClick = {
                            viewModel.navigateTo(NavItem.SMART_SCHEDULER)
                            viewModel.generateSmartScheduleWithAI()
                        },
                        onToggleTaskCompletion = { viewModel.toggleTaskCompletion(it) },
                        onEditTask = { viewModel.openEditTaskDialog(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onToggleSubtask = { task, subId -> viewModel.toggleSubtask(task, subId) },
                        onNavigate = { viewModel.navigateTo(it) },
                        parseSubtasks = { viewModel.repository.parseSubtasks(it) }
                    )
                    NavItem.TASKS -> TaskManagementScreen(
                        tasks = tasks,
                        searchQuery = searchQuery,
                        onAddTaskClick = { viewModel.openAddTaskDialog() },
                        onToggleTaskCompletion = { viewModel.toggleTaskCompletion(it) },
                        onEditTask = { viewModel.openEditTaskDialog(it) },
                        onDeleteTask = { viewModel.deleteTask(it) },
                        onToggleSubtask = { task, subId -> viewModel.toggleSubtask(task, subId) },
                        parseSubtasks = { viewModel.repository.parseSubtasks(it) }
                    )
                    NavItem.CALENDAR -> CalendarScreen(
                        schedules = schedules,
                        tasks = tasks,
                        onSaveSchedule = { viewModel.saveSchedule(it) },
                        onDeleteSchedule = { viewModel.deleteSchedule(it) }
                    )
                    NavItem.SMART_SCHEDULER -> SmartSchedulerScreen(
                        tasks = tasks,
                        proposedSchedules = proposedSchedules,
                        userSettings = userSettings,
                        isGenerating = isAiGenerating,
                        onGenerate = { viewModel.generateSmartScheduleWithAI() },
                        onApproveItem = { viewModel.approveProposedSchedule(it) },
                        onApproveAll = { viewModel.approveAllProposedSchedules() },
                        onRejectAll = { viewModel.rejectProposedSchedules() }
                    )
                    NavItem.AI_ASSISTANT -> AiAssistantScreen(
                        messages = aiChatMessages,
                        isThinking = isAiGenerating,
                        onSendMessage = { viewModel.sendAiChatMessage(it) }
                    )
                    NavItem.REMINDERS -> RemindersScreen(
                        tasks = tasks,
                        userSettings = userSettings,
                        onToggleCompletion = { viewModel.toggleTaskCompletion(it) },
                        onEditTask = { viewModel.openEditTaskDialog(it) }
                    )
                    NavItem.ANALYTICS -> AnalyticsScreen(tasks = tasks)
                    NavItem.NOTES -> NotesScreen(
                        notes = notes,
                        searchQuery = searchQuery,
                        onSaveNote = { title, content, cat, col, id ->
                            viewModel.saveNote(title, content, cat, col, id)
                        },
                        onDeleteNote = { viewModel.deleteNote(it) },
                        onConvertNoteToTask = { viewModel.convertNoteToTask(it) }
                    )
                    NavItem.SETTINGS -> SettingsScreen(
                        userSettings = userSettings,
                        onSaveSettings = { viewModel.updateSettings(it) }
                    )
                }
            }
        }
    }

    // Modal Task Dialog
    if (showTaskDialog) {
        val initialSubs = remember(editingTask) {
            editingTask?.let { viewModel.repository.parseSubtasks(it.subtasksJson) } ?: emptyList()
        }
        TaskDialog(
            initialTask = editingTask,
            initialSubtasks = initialSubs,
            onDismiss = { viewModel.closeTaskDialog() },
            onSave = { id, title, desc, cat, dueD, dueT, est, prio, stat, prog, subs, notes ->
                viewModel.saveTask(id, title, desc, cat, dueD, dueT, est, prio, stat, prog, subs, notes)
            }
        )
    }
}
