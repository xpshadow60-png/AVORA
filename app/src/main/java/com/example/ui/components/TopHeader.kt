package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.UserProfile
import com.example.focus.FocusTimerState
import com.example.ui.theme.CoralPriority
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.GreenSuccess
import com.example.ui.theme.Primary500
import com.example.ui.theme.ThemeMode

@Composable
fun TopHeader(
    timerState: FocusTimerState,
    themeMode: ThemeMode = ThemeMode.DARK,
    currentUser: UserProfile? = null,
    dailyUsageSeconds: Long = 0L,
    selectedTab: Int = 0,
    onSelectTab: (Int) -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onSetThemeMode: (ThemeMode) -> Unit = {},
    onOpenFreeInfo: () -> Unit = {},
    onPrioritizeClick: () -> Unit,
    onFocusTabClick: () -> Unit,
    onOpenCareerHub: () -> Unit = {},
    onOpenBackupExport: () -> Unit = {},
    onOpenPrivacyDisclosures: () -> Unit = {},
    onOpenAuth: () -> Unit = {},
    onResendVerification: () -> Unit = {},
    onChangePassword: (String) -> Unit = {},
    onLogout: () -> Unit = {}
) {
    var showThemeMenu by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPasswordInput by remember { mutableStateOf("") }
    var changePasswordError by remember { mutableStateOf<String?>(null) }
    var changePasswordSuccess by remember { mutableStateOf(false) }

    val studyMinutes = (dailyUsageSeconds / 60).toInt()
    val studyHours = studyMinutes / 60

    val topFeatures = remember {
        listOf(
            HeaderFeatureItem("Home", 0, Icons.Filled.Home, "🏠"),
            HeaderFeatureItem("AI Tech Tutor", 1, Icons.Filled.AutoAwesome, "🤖"),
            HeaderFeatureItem("Code Lab", 2, Icons.Filled.Terminal, "💻"),
            HeaderFeatureItem("Projects", 3, Icons.Filled.Architecture, "🚀"),
            HeaderFeatureItem("Roadmaps", 4, Icons.Filled.Map, "🗺️"),
            HeaderFeatureItem("Problem Solver", 11, Icons.Filled.EditNote, "✍️"),
            HeaderFeatureItem("AI Quizzes", 5, Icons.Filled.Quiz, "🎯"),
            HeaderFeatureItem("Notes & Docs", 6, Icons.Filled.Description, "📑"),
            HeaderFeatureItem("Flashcards", 12, Icons.Filled.Style, "🃏"),
            HeaderFeatureItem("Planner", 7, Icons.Filled.EventNote, "📅"),
            HeaderFeatureItem("Focus Timer", 8, Icons.Filled.Timer, "⏱️"),
            HeaderFeatureItem("Skills", 9, Icons.Filled.BarChart, "📊"),
            HeaderFeatureItem("Portfolio", 10, Icons.Filled.VerifiedUser, "🏆"),
            HeaderFeatureItem("Analytics", 13, Icons.Filled.Analytics, "📈")
        )
    }

    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f)),
        tonalElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 10.dp, bottom = 8.dp)
        ) {
            // Main Top Bar (Logo, Title, Action buttons)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Logo & Name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.clickable { onSelectTab(0) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0B1120)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_avora_app_logo_1787239854178),
                            contentDescription = "Avora Logo",
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column {
                        Text(
                            text = "Avora",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.5.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (currentUser != null) {
                                if (currentUser.name.equals("Future Leader", ignoreCase = true)) "Hi, Future Leader" else "Hi, ${currentUser.name}"
                            } else if (timerState.isActive) "Study Time Active" else "Ready to Build",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (timerState.isActive) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Action Buttons: Free Access Chip, Theme Toggle, Backup/Export, Active Alert, User Avatar
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Free Access Badge & Learning Info Chip
                    Surface(
                        onClick = onOpenFreeInfo,
                        shape = RoundedCornerShape(20.dp),
                        color = GreenSuccess.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            GreenSuccess.copy(alpha = 0.35f)
                        ),
                        modifier = Modifier.testTag("header_free_info_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                tint = GreenSuccess,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "100% Free",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = GreenSuccess
                            )
                        }
                    }

                    // Data Backup & Export Quick Button
                    IconButton(
                        onClick = onOpenBackupExport,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("header_backup_export_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SaveAlt,
                            contentDescription = "Data Backup & Export",
                            tint = CyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Dark / Light Theme Mode Toggle Button
                    Box {
                        IconButton(
                            onClick = { showThemeMenu = true },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .testTag("theme_mode_toggle_button")
                        ) {
                            Icon(
                                imageVector = when (themeMode) {
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.SYSTEM -> Icons.Default.SettingsBrightness
                                },
                                contentDescription = "Theme Mode",
                                tint = when (themeMode) {
                                    ThemeMode.DARK -> CyanAccent
                                    ThemeMode.LIGHT -> Primary500
                                    ThemeMode.SYSTEM -> MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Dark Theme 🌙") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.DarkMode,
                                        contentDescription = null,
                                        tint = if (themeMode == ThemeMode.DARK) CyanAccent else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onSetThemeMode(ThemeMode.DARK)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Light Theme ☀️") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LightMode,
                                        contentDescription = null,
                                        tint = if (themeMode == ThemeMode.LIGHT) Primary500 else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    onSetThemeMode(ThemeMode.LIGHT)
                                    showThemeMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("System Default ⚙️") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.SettingsBrightness,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    onSetThemeMode(ThemeMode.SYSTEM)
                                    showThemeMenu = false
                                }
                            )
                        }
                    }

                    if (timerState.isActive) {
                        Surface(
                            onClick = onFocusTabClick,
                            shape = RoundedCornerShape(20.dp),
                            color = CyanAccent.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyanAccent)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Focus Active",
                                    tint = CyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = formatSeconds(timerState.remainingSeconds),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = CyanAccent
                                )
                            }
                        }
                    }

                    // User Profile / Login Avatar Button
                    Surface(
                        onClick = {
                            if (currentUser != null) {
                                showProfileDialog = true
                            } else {
                                onOpenAuth()
                            }
                        },
                        shape = CircleShape,
                        color = if (currentUser != null) Primary500 else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("user_profile_avatar_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (currentUser != null) {
                                Text(
                                    text = currentUser.avatarInitials,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Sign In",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Visibly Displayed Top Features Bar (Scrollable horizontally)
            androidx.compose.foundation.lazy.LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("top_features_bar"),
                contentPadding = PaddingValues(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(topFeatures.size) { idx ->
                    val feature = topFeatures[idx]
                    val isSelected = selectedTab == feature.tabIndex
                    
                    Surface(
                        onClick = { onSelectTab(feature.tabIndex) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) Primary500 else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("header_feature_${feature.tabIndex}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Icon(
                                imageVector = feature.icon,
                                contentDescription = feature.title,
                                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = feature.title,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    fontSize = 12.sp
                                ),
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }

    // User Profile Details Dialog
    if (showProfileDialog && currentUser != null) {
        AlertDialog(
            onDismissRequest = { showProfileDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Primary500,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = currentUser.avatarInitials,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                    Column {
                        Text(text = currentUser.name, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (currentUser.isGuest) "Guest Account" else currentUser.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Major / Study Field:", style = MaterialTheme.typography.bodyMedium)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = CyanAccent.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = currentUser.majorOrField,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = CyanAccent,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Cloud User ID:", style = MaterialTheme.typography.bodyMedium)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (currentUser.id.length > 12) currentUser.id.take(10) + "..." else currentUser.id,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Email Status:", style = MaterialTheme.typography.bodyMedium)
                        if (currentUser.isEmailVerified) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = GreenSuccess.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Verified ✅",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = GreenSuccess,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else if (currentUser.isGuest) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CyanAccent.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "Guest Mode 🌐",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = CyanAccent,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        } else {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = CoralPriority.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "Unverified ⚠️",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = CoralPriority,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                                TextButton(
                                    onClick = { onResendVerification() },
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                                ) {
                                    Text("Resend Link", style = MaterialTheme.typography.labelSmall, color = CyanAccent)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Access Tier:", style = MaterialTheme.typography.bodyMedium)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GreenSuccess.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "100% Free For Everyone 🎓",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = GreenSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (!currentUser.isGuest) {
                        OutlinedButton(
                            onClick = {
                                newPasswordInput = ""
                                changePasswordError = null
                                changePasswordSuccess = false
                                showChangePasswordDialog = true
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp), tint = Primary500)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Account Password", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Button(
                        onClick = {
                            showProfileDialog = false
                            onOpenFreeInfo()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Primary500
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Avora is Free • Learn More",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedButton(
                        onClick = {
                            showProfileDialog = false
                            onOpenCareerHub()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Career Path & Roadmap", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    OutlinedButton(
                        onClick = {
                            showProfileDialog = false
                            onOpenBackupExport()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.SaveAlt, contentDescription = null, modifier = Modifier.size(16.dp), tint = CyanAccent)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Data Export & Backup (JSON/CSV)", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }

                    OutlinedButton(
                        onClick = {
                            showProfileDialog = false
                            onOpenPrivacyDisclosures()
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp), tint = GreenSuccess)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Privacy & Data Disclosures", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showProfileDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CoralPriority)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showProfileDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showChangePasswordDialog = false
                newPasswordInput = ""
                changePasswordError = null
                changePasswordSuccess = false
            },
            title = {
                Text("Change Account Password", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (changePasswordSuccess) {
                        Text(
                            text = "Password updated successfully!",
                            color = GreenSuccess,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            text = "Enter a new secure password (at least 6 characters):",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (changePasswordError != null) {
                            Text(
                                text = changePasswordError ?: "",
                                color = CoralPriority,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        OutlinedTextField(
                            value = newPasswordInput,
                            onValueChange = { newPasswordInput = it },
                            label = { Text("New Password") },
                            visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (changePasswordSuccess) {
                    Button(onClick = {
                        showChangePasswordDialog = false
                        newPasswordInput = ""
                        changePasswordError = null
                        changePasswordSuccess = false
                    }) {
                        Text("Done")
                    }
                } else {
                    Button(
                        onClick = {
                            if (newPasswordInput.length < 6) {
                                changePasswordError = "Password must be at least 6 characters."
                            } else {
                                onChangePassword(newPasswordInput)
                                changePasswordSuccess = true
                            }
                        },
                        enabled = newPasswordInput.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                    ) {
                        Text("Save Password")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showChangePasswordDialog = false
                    newPasswordInput = ""
                    changePasswordError = null
                    changePasswordSuccess = false
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun formatSeconds(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}

data class HeaderFeatureItem(
    val title: String,
    val tabIndex: Int,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val badge: String? = null
)

