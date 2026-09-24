package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.ScheduleEntity
import com.example.data.local.TaskEntity
import com.example.ui.model.SubjectCategoryManager
import com.example.ui.model.SubjectInfo
import com.example.ui.theme.CoralPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.Primary500
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    tasks: List<TaskEntity>,
    schedules: List<ScheduleEntity>,
    onAddTask: (String, String, String, Int, String, String, Long) -> Unit,
    onTaskToggle: (TaskEntity) -> Unit,
    onDeleteTask: (TaskEntity) -> Unit,
    onAddSchedule: (String, String, String, String, String) -> Unit,
    onPrioritizeWithAi: () -> Unit
) {
    var selectedFilter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var viewMode by remember { mutableStateOf(0) } // 0: Tasks, 1: Schedule Blocks
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showAddScheduleDialog by remember { mutableStateOf(false) }

    // Collect all unique subjects present in existing tasks + standard presets
    val allSubjectOptions = remember(tasks) {
        val taskSubjects = tasks.map { it.subject.trim() }.filter { it.isNotBlank() }
        val presetNames = SubjectCategoryManager.PRESET_SUBJECTS.map { it.name }
        (presetNames + taskSubjects).distinct()
    }

    val filteredTasks = tasks.filter { task ->
        val matchesFilter = when (selectedFilter) {
            "All" -> true
            "Pending" -> !task.isCompleted
            "Completed" -> task.isCompleted
            "High Priority" -> task.priority == "HIGH" && !task.isCompleted
            "Mathematics" -> task.subject.equals("Mathematics", ignoreCase = true) || task.subject.contains("Math", ignoreCase = true)
            "Science" -> task.subject.equals("Science", ignoreCase = true) || task.subject.contains("Physics", ignoreCase = true) || task.subject.contains("Chemistry", ignoreCase = true) || task.subject.contains("Bio", ignoreCase = true)
            "Literature" -> task.subject.equals("Literature", ignoreCase = true) || task.subject.contains("English", ignoreCase = true) || task.subject.contains("Reading", ignoreCase = true)
            else -> task.subject.equals(selectedFilter, ignoreCase = true)
        }
        val query = searchQuery.trim()
        val matchesSearch = if (query.isEmpty()) {
            true
        } else {
            task.title.contains(query, ignoreCase = true) ||
                    task.priority.contains(query, ignoreCase = true) ||
                    task.subject.contains(query, ignoreCase = true) ||
                    task.category.contains(query, ignoreCase = true) ||
                    task.notes.contains(query, ignoreCase = true)
        }

        matchesFilter && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // View Switcher Header (Tasks vs Schedule Blocks)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.weight(1f)) {
                    SegmentedButton(
                        selected = viewMode == 0,
                        onClick = { viewMode = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) {
                        Text("Tasks (${tasks.count { !it.isCompleted }})")
                    }
                    SegmentedButton(
                        selected = viewMode == 1,
                        onClick = { viewMode = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) {
                        Text("Schedule (${schedules.size})")
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (viewMode == 0) showAddTaskDialog = true else showAddScheduleDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (viewMode == 0) {
                // Real-time Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .testTag("task_search_bar"),
                    placeholder = { Text("Search by title, subject, or priority...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary500,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                )

                // Filter Chips by Status & Subject
                val filterChips = remember(tasks, allSubjectOptions) {
                    val baseFilters = listOf("All", "Pending", "High Priority", "Completed")
                    baseFilters + allSubjectOptions
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    items(filterChips) { filter ->
                        val isSubject = filter !in listOf("All", "Pending", "High Priority", "Completed")
                        val subjectInfo = if (isSubject) SubjectCategoryManager.getSubjectInfo(filter) else null

                        val taskCount = remember(tasks, filter) {
                            when (filter) {
                                "All" -> tasks.size
                                "Pending" -> tasks.count { !it.isCompleted }
                                "Completed" -> tasks.count { it.isCompleted }
                                "High Priority" -> tasks.count { it.priority == "HIGH" && !it.isCompleted }
                                "Mathematics" -> tasks.count { it.subject.equals("Mathematics", ignoreCase = true) || it.subject.contains("Math", ignoreCase = true) }
                                "Science" -> tasks.count { it.subject.equals("Science", ignoreCase = true) || it.subject.contains("Physics", ignoreCase = true) || it.subject.contains("Chemistry", ignoreCase = true) }
                                "Literature" -> tasks.count { it.subject.equals("Literature", ignoreCase = true) || it.subject.contains("English", ignoreCase = true) }
                                else -> tasks.count { it.subject.equals(filter, ignoreCase = true) }
                            }
                        }

                        val isSelected = selectedFilter == filter

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilter = filter },
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (subjectInfo != null) {
                                        Text(subjectInfo.emoji)
                                    }
                                    Text(filter)
                                    if (taskCount > 0) {
                                        Surface(
                                            shape = CircleShape,
                                            color = if (isSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.padding(start = 2.dp)
                                        ) {
                                            Text(
                                                text = "$taskCount",
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = subjectInfo?.color ?: Primary500,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // AI Prioritizer Banner
                Card(
                    onClick = onPrioritizeWithAi,
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = Primary500)
                            Column {
                                Text(
                                    text = "⚡ Auto-Prioritize Tasks with AI",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Avora AI analyzes subjects, deadlines & difficulty to plan your day.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Go", tint = Primary500)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tasks List
                if (filteredTasks.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(imageVector = Icons.Default.Task, contentDescription = "No tasks", tint = Color.Gray, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "No tasks matching '$searchQuery'" else "No tasks found under '$selectedFilter'",
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredTasks, key = { it.id }) { task ->
                            TaskDetailCard(
                                task = task,
                                onToggle = { onTaskToggle(task) },
                                onDelete = { onDeleteTask(task) }
                            )
                        }
                    }
                }
            } else {
                // Schedule Blocks List
                if (schedules.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No study schedule blocks set up yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(schedules) { schedule ->
                            val subjectInfo = SubjectCategoryManager.getSubjectInfo(schedule.subject)
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = subjectInfo.color.copy(alpha = 0.15f),
                                            modifier = Modifier.size(44.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = subjectInfo.icon,
                                                    contentDescription = schedule.subject,
                                                    tint = subjectInfo.color,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                        }

                                        Column {
                                            Text(
                                                text = schedule.dayOfWeek.uppercase(),
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = subjectInfo.color
                                            )
                                            Text(
                                                text = schedule.title,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "${subjectInfo.emoji} ${schedule.subject}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = CyanAccent.copy(alpha = 0.2f)
                                    ) {
                                        Text(
                                            text = "${schedule.startTime} - ${schedule.endTime}",
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = CyanAccent,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            AddTaskDialog(
                onDismiss = { showAddTaskDialog = false },
                onAdd = { title, subject, priority, estimated, category, notes ->
                    onAddTask(title, subject, priority, estimated, category, notes, System.currentTimeMillis() + 86400000)
                    showAddTaskDialog = false
                }
            )
        }

        // Add Schedule Dialog
        if (showAddScheduleDialog) {
            AddScheduleDialog(
                onDismiss = { showAddScheduleDialog = false },
                onAdd = { title, subject, startTime, endTime, day ->
                    onAddSchedule(title, subject, startTime, endTime, day)
                    showAddScheduleDialog = false
                }
            )
        }

        // Floating Action Button to add new task
        FloatingActionButton(
            onClick = {
                if (viewMode == 0) showAddTaskDialog = true else showAddScheduleDialog = true
            },
            containerColor = Primary500,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 16.dp)
                .testTag("add_task_fab")
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add New Task"
            )
        }
    }
}

@Composable
fun TaskDetailCard(
    task: TaskEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val priorityColor = when (task.priority) {
        "HIGH" -> CoralPriority
        "MEDIUM" -> Color(0xFFE67E22)
        else -> Color(0xFF00B894)
    }

    val subjectInfo = SubjectCategoryManager.getSubjectInfo(task.subject)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("task_item_card")
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Visual Subject / Priority Indicator Stripe
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(subjectInfo.color)
                    .testTag("task_priority_indicator")
            )

            Column(modifier = Modifier.padding(14.dp).weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Checkbox(
                            checked = task.isCompleted,
                            onCheckedChange = { onToggle() },
                            colors = CheckboxDefaults.colors(checkedColor = Primary500)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.Gray)
                    }
                }

                if (task.notes.isNotBlank()) {
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 44.dp, bottom = 6.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 44.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Subject Category Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = subjectInfo.color.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = subjectInfo.icon,
                                    contentDescription = subjectInfo.name,
                                    tint = subjectInfo.color,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = task.subject,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = subjectInfo.color
                                )
                            }
                        }

                        // Category Pill (e.g. Exam Prep, Assignment)
                        if (task.category.isNotBlank() && task.category != "Assignment") {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = task.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "⏱️ ${task.estimatedMinutes}m",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = priorityColor.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = task.priority,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = priorityColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, Int, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    var customSubjectName by remember { mutableStateOf("") }
    var isCustomSubject by remember { mutableStateOf(false) }

    var priority by remember { mutableStateOf("HIGH") }
    var estimatedMinutes by remember { mutableStateOf("30") }
    var category by remember { mutableStateOf("Assignment") }
    var notes by remember { mutableStateOf("") }

    val presetSubjects = SubjectCategoryManager.PRESET_SUBJECTS
    val categories = listOf("Assignment", "Exam Prep", "Homework", "Project", "Reading")
    val quickDurations = listOf("15", "30", "45", "60", "90")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Add New Study Task",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title *") },
                        placeholder = { Text("e.g. Calculus Problem Set #4") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Subject Categorization Section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Subject Category",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // Preset Subject Chips
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presetSubjects.forEach { subjectInfo ->
                                val isSelected = !isCustomSubject && selectedSubject.equals(subjectInfo.name, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) subjectInfo.color else subjectInfo.lightBgColor.copy(alpha = 0.5f),
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, subjectInfo.color.copy(alpha = 0.3f)),
                                    modifier = Modifier
                                        .clickable {
                                            isCustomSubject = false
                                            selectedSubject = subjectInfo.name
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(subjectInfo.emoji)
                                        Text(
                                            text = subjectInfo.name,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                            color = if (isSelected) Color.White else subjectInfo.color
                                        )
                                    }
                                }
                            }

                            // Custom subject option button
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isCustomSubject) Primary500 else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clickable {
                                        isCustomSubject = true
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("✏️")
                                    Text(
                                        text = "Custom...",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isCustomSubject) FontWeight.Bold else FontWeight.Medium),
                                        color = if (isCustomSubject) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        if (isCustomSubject) {
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = customSubjectName,
                                onValueChange = { customSubjectName = it },
                                label = { Text("Enter Custom Subject Name") },
                                placeholder = { Text("e.g. Biochemistry, Economics") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }

                // Priority Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Priority Level",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("HIGH", "MEDIUM", "LOW").forEach { p ->
                                val isSelected = priority == p
                                val pColor = when (p) {
                                    "HIGH" -> CoralPriority
                                    "MEDIUM" -> Color(0xFFE67E22)
                                    else -> Color(0xFF00B894)
                                }
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) pColor else pColor.copy(alpha = 0.1f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { priority = p }
                                ) {
                                    Text(
                                        text = p,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) Color.White else pColor,
                                        modifier = Modifier.padding(vertical = 10.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // Duration Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Estimated Duration (Minutes)",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            quickDurations.forEach { dur ->
                                val isSelected = estimatedMinutes == dur
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { estimatedMinutes = dur },
                                    label = { Text("${dur}m") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Category Selector
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Task Type / Category",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(categories) { cat ->
                                FilterChip(
                                    selected = category == cat,
                                    onClick = { category = cat },
                                    label = { Text(cat) }
                                )
                            }
                        }
                    }
                }

                // Notes / Description
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Description") },
                        placeholder = { Text("Key chapters, goals, or reference links") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )
                }

                // Action Buttons
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    val finalSubject = if (isCustomSubject && customSubjectName.isNotBlank()) {
                                        customSubjectName.trim()
                                    } else {
                                        selectedSubject
                                    }
                                    onAdd(
                                        title.trim(),
                                        finalSubject,
                                        priority,
                                        estimatedMinutes.toIntOrNull() ?: 30,
                                        category,
                                        notes.trim()
                                    )
                                }
                            },
                            enabled = title.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Add Task")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddScheduleDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String, String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf("Mathematics") }
    var startTime by remember { mutableStateOf("09:00") }
    var endTime by remember { mutableStateOf("10:30") }
    var dayOfWeek by remember { mutableStateOf("Monday") }

    val presetSubjects = SubjectCategoryManager.PRESET_SUBJECTS
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text(
                        text = "Add Study Block Schedule",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Block Title (e.g., Calculus Session)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                // Subject Picker
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Subject Category",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            presetSubjects.take(6).forEach { subjectInfo ->
                                val isSelected = selectedSubject.equals(subjectInfo.name, ignoreCase = true)
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) subjectInfo.color else subjectInfo.lightBgColor.copy(alpha = 0.5f),
                                    modifier = Modifier.clickable { selectedSubject = subjectInfo.name }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(subjectInfo.emoji)
                                        Text(
                                            text = subjectInfo.name,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium),
                                            color = if (isSelected) Color.White else subjectInfo.color
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Day of Week",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(days) { d ->
                                FilterChip(
                                    selected = dayOfWeek == d,
                                    onClick = { dayOfWeek = d },
                                    label = { Text(d.take(3)) }
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) { Text("Cancel") }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onAdd(title.trim(), selectedSubject, startTime, endTime, dayOfWeek)
                                }
                            },
                            enabled = title.isNotBlank(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Block")
                        }
                    }
                }
            }
        }
    }
}

