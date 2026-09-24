package com.example.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Primary500

data class NavTabItem(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val tag: String,
    val tabIndex: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    var showMoreBottomSheet by remember { mutableStateOf(false) }

    val primaryTabs = listOf(
        NavTabItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home", 0),
        NavTabItem("AI Tutor", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_tutor", 1),
        NavTabItem("Python Lab", Icons.Filled.Terminal, Icons.Outlined.Terminal, "tab_code_lab", 2),
        NavTabItem("Projects", Icons.Filled.Architecture, Icons.Outlined.Architecture, "tab_projects", 3),
        NavTabItem("All Features", Icons.Filled.Apps, Icons.Outlined.Apps, "tab_all_features", -1)
    )

    val allFeaturesList = listOf(
        NavTabItem("Home", Icons.Filled.Home, Icons.Outlined.Home, "tab_home", 0),
        NavTabItem("AI Tech Tutor", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, "tab_tutor", 1),
        NavTabItem("Python Playground & Lab", Icons.Filled.Terminal, Icons.Outlined.Terminal, "tab_code_lab", 2),
        NavTabItem("Project Builder", Icons.Filled.Architecture, Icons.Outlined.Architecture, "tab_projects", 3),
        NavTabItem("Learning Roadmaps", Icons.Filled.Map, Icons.Outlined.Map, "tab_roadmaps", 4),
        NavTabItem("Problem Solver", Icons.Filled.EditNote, Icons.Outlined.EditNote, "tab_solver", 11),
        NavTabItem("AI Quizzes & Labs", Icons.Filled.Quiz, Icons.Outlined.Quiz, "tab_quizzes", 5),
        NavTabItem("Notes & Docs", Icons.Filled.Description, Icons.Outlined.Description, "tab_notes", 6),
        NavTabItem("Flashcards (SRS)", Icons.Filled.Style, Icons.Outlined.Style, "tab_flashcards", 12),
        NavTabItem("Study Planner", Icons.Filled.EventNote, Icons.Outlined.EventNote, "tab_planner", 7),
        NavTabItem("Focus Timer", Icons.Filled.Timer, Icons.Outlined.Timer, "tab_focus", 8),
        NavTabItem("Skill Mastery Matrix", Icons.Filled.BarChart, Icons.Outlined.BarChart, "tab_skills", 9),
        NavTabItem("Verified Portfolio", Icons.Filled.VerifiedUser, Icons.Outlined.VerifiedUser, "tab_portfolio", 10),
        NavTabItem("Analytics & Stats", Icons.Filled.Analytics, Icons.Outlined.Analytics, "tab_analytics", 13),
        NavTabItem("Career Hub & Guidance", Icons.Filled.Work, Icons.Outlined.Work, "tab_career", 14)
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        primaryTabs.forEach { tab ->
            val isSelected = if (tab.tabIndex == -1) {
                // If selectedTab is not among the first 4 (0, 1, 2, 3), highlight "All Features"
                selectedTab > 3
            } else {
                selectedTab == tab.tabIndex
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (tab.tabIndex == -1) {
                        showMoreBottomSheet = true
                    } else {
                        onTabSelected(tab.tabIndex)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.title
                    )
                },
                label = {
                    Text(
                        tab.title,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        maxLines = 1
                    )
                },
                modifier = Modifier.testTag(tab.tag)
            )
        }
    }

    if (showMoreBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showMoreBottomSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "⚡ All Avora Features",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Access all 14 AI & technology learning modules",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { showMoreBottomSheet = false }) {
                        Icon(Icons.Filled.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    items(allFeaturesList.size) { idx ->
                        val item = allFeaturesList[idx]
                        val isItemActive = selectedTab == item.tabIndex
                        Surface(
                            onClick = {
                                onTabSelected(item.tabIndex)
                                showMoreBottomSheet = false
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isItemActive) Primary500.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = if (isItemActive) androidx.compose.foundation.BorderStroke(1.5.dp, Primary500) else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            ) {
                                Icon(
                                    imageVector = item.selectedIcon,
                                    contentDescription = item.title,
                                    tint = if (isItemActive) Primary500 else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = item.title,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isItemActive) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    ),
                                    color = if (isItemActive) Primary500 else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

