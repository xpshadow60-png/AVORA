package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.launch
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.R
import com.example.ui.components.PrivacyDataDisclosureDialog
import com.example.ui.components.TechAtmosphereBackground
import com.example.ui.theme.*

@Composable
fun LoginScreen(
    onSignInWithPassword: suspend (email: String, password: String) -> String?,
    onSignUpWithPassword: suspend (name: String, email: String, password: String, major: String) -> String?,
    onResetPassword: suspend (email: String) -> String?,
    onGoogleSignIn: () -> Unit,
    onContinueAsGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isSignUpMode by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var selectedMajor by remember { mutableStateOf("Computer Science") }
    var selectedClassLevel by remember { mutableStateOf("Undergraduate") }
    var rememberMe by remember { mutableStateOf(true) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    var forgotPasswordSent by remember { mutableStateOf(false) }
    var forgotPasswordError by remember { mutableStateOf<String?>(null) }
    var isResettingPassword by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    val majors = listOf(
        "AI & Machine Learning", "Computer Science", "Software Engineering", "Python & Data Science",
        "Full-Stack Web Dev", "Android & Mobile Dev", "Cybersecurity", "Cloud & DevOps", "Robotics & IoT", "Aspiring Programmer"
    )

    val classLevels = listOf("High School", "Undergraduate", "Graduate / Master's", "Bootcamp / Self-Taught", "Professional")

    fun validateAndSubmit() {
        if (isLoading) return
        errorMessage = null
        successMessage = null
        val cleanEmail = email.trim()

        if (cleanEmail.isBlank()) {
            errorMessage = "Please enter your email address."
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(cleanEmail).matches()) {
            errorMessage = "Please enter a valid email address."
            return
        }

        if (password.length < 6) {
            errorMessage = "Password must be at least 6 characters."
            return
        }

        if (isSignUpMode) {
            if (name.trim().isBlank()) {
                errorMessage = "Please enter your full name."
                return
            }
            if (password != confirmPassword) {
                errorMessage = "Passwords do not match."
                return
            }

            coroutineScope.launch {
                isLoading = true
                val error = onSignUpWithPassword(name.trim(), cleanEmail, password, selectedMajor)
                isLoading = false
                if (error != null) {
                    errorMessage = error
                }
            }
        } else {
            coroutineScope.launch {
                isLoading = true
                val error = onSignInWithPassword(cleanEmail, password)
                isLoading = false
                if (error != null) {
                    errorMessage = error
                }
            }
        }
    }

    TechAtmosphereBackground(
        modifier = modifier.fillMaxSize(),
        isDark = isSystemInDarkTheme()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Brand Logo & Name
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Primary500, CyanAccent)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_avora_app_logo_1787239854178),
                    contentDescription = "Avora Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Avora",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = if (isSignUpMode) "Create your verified cloud account to sync learning across devices" else "Learn Technology. Build Technology. Become Future-Ready.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Mode Selector Tabs (Sign In / Sign Up)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Sign In Tab
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (!isSignUpMode) Primary500 else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                isSignUpMode = false
                                errorMessage = null
                                successMessage = null
                            }
                            .testTag("tab_sign_in")
                    ) {
                        Text(
                            text = "Sign In",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (!isSignUpMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }

                    // Create Account Tab
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSignUpMode) Primary500 else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                isSignUpMode = true
                                errorMessage = null
                                successMessage = null
                            }
                            .testTag("tab_sign_up")
                    ) {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = if (isSignUpMode) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Error Banner if present
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CoralPriority.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CoralPriority.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = "Error",
                                tint = CoralPriority,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = CoralPriority
                            )
                        }
                    }
                }
            }

            // Success Banner if present
            AnimatedVisibility(visible = successMessage != null) {
                successMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = GreenSuccess.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Success",
                                tint = GreenSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = GreenSuccess
                            )
                        }
                    }
                }
            }

            // Input Fields Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Full Name (Only on Sign Up)
                    AnimatedVisibility(visible = isSignUpMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            placeholder = { Text("e.g. Alex Johnson") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = "Name", tint = Primary500)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input")
                        )
                    }

                    // Email Address Field
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        placeholder = { Text("your.email@gmail.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = "Email", tint = Primary500)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input")
                    )

                    // Major / Subject Selector (Only on Sign Up)
                    AnimatedVisibility(visible = isSignUpMode) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Major / Primary Study Area",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(majors) { major ->
                                    FilterChip(
                                        selected = selectedMajor == major,
                                        onClick = { selectedMajor = major },
                                        label = { Text(major) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Primary500,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Academic / Experience Level",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(classLevels) { level ->
                                    FilterChip(
                                        selected = selectedClassLevel == level,
                                        onClick = { selectedClassLevel = level },
                                        label = { Text(level) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = CyanAccent,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Password Field
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = "Password", tint = Primary500)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = if (isSignUpMode) ImeAction.Next else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) },
                            onDone = {
                                focusManager.clearFocus()
                                validateAndSubmit()
                            }
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input")
                    )

                    // Confirm Password (Only on Sign Up)
                    AnimatedVisibility(visible = isSignUpMode) {
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            label = { Text("Confirm Password") },
                            placeholder = { Text("••••••••") },
                            leadingIcon = {
                                Icon(Icons.Default.LockReset, contentDescription = "Confirm Password", tint = Primary500)
                            },
                            trailingIcon = {
                                IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                    Icon(
                                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = if (confirmPasswordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    validateAndSubmit()
                                }
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("confirm_password_input")
                        )
                    }

                    // Remember Me & Forgot Password
                    if (!isSignUpMode) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { rememberMe = !rememberMe }
                            ) {
                                Checkbox(
                                    checked = rememberMe,
                                    onCheckedChange = { rememberMe = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Primary500)
                                )
                                Text(
                                    text = "Stay signed in",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            TextButton(onClick = {
                                forgotPasswordEmail = email
                                showForgotPasswordDialog = true
                            }) {
                                Text(
                                    text = "Forgot password?",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = CyanAccent
                                )
                            }
                        }
                    }

                    // Primary Action Button (Sign In / Create Account)
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            validateAndSubmit()
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("submit_auth_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (isSignUpMode) "Create Account (Free)" else "Sign In",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Or Continue With Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text(
                    text = "  OR  ",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Divider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Google Sign In Button
            OutlinedButton(
                onClick = onGoogleSignIn,
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("google_sign_in_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google",
                    tint = CyanAccent,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Continue with Google",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Continue as Guest Option
            TextButton(
                onClick = onContinueAsGuest,
                enabled = !isLoading,
                modifier = Modifier.testTag("guest_explore_button")
            ) {
                Text(
                    text = "Explore as Guest / Offline Mode ➔",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Privacy & Data Handling Disclosures Link
            Row(
                modifier = Modifier.clickable { showPrivacyDialog = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = CyanAccent,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Security & Data Privacy Disclosures",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Privacy & Data Handling Disclosures Dialog
    if (showPrivacyDialog) {
        PrivacyDataDisclosureDialog(
            onDismiss = { showPrivacyDialog = false }
        )
    }

    // Forgot Password Dialog (Sends real Firebase Auth Password Reset Email)
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                showForgotPasswordDialog = false
                forgotPasswordSent = false
                forgotPasswordError = null
            },
            title = {
                Text(text = "Reset Account Password", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (forgotPasswordSent) {
                        Text(
                            text = "A password reset link has been sent to $forgotPasswordEmail. Please check your inbox (and spam folder) to reset your password.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GreenSuccess
                        )
                    } else {
                        Text(
                            text = "Enter your registered email address. We will send an official password reset link directly to your inbox.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        if (forgotPasswordError != null) {
                            Text(
                                text = forgotPasswordError ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = CoralPriority
                            )
                        }

                        OutlinedTextField(
                            value = forgotPasswordEmail,
                            onValueChange = { forgotPasswordEmail = it },
                            label = { Text("Registered Email") },
                            placeholder = { Text("e.g. your.email@gmail.com") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                if (forgotPasswordSent) {
                    Button(onClick = {
                        showForgotPasswordDialog = false
                        forgotPasswordSent = false
                        forgotPasswordError = null
                    }) {
                        Text("Done")
                    }
                } else {
                    Button(
                        onClick = {
                            forgotPasswordError = null
                            val clean = forgotPasswordEmail.trim()
                            if (clean.isBlank()) {
                                forgotPasswordError = "Please enter your email."
                            } else {
                                coroutineScope.launch {
                                    isResettingPassword = true
                                    val error = onResetPassword(clean)
                                    isResettingPassword = false
                                    if (error == null) {
                                        forgotPasswordSent = true
                                    } else {
                                        forgotPasswordError = error
                                    }
                                }
                            }
                        },
                        enabled = forgotPasswordEmail.isNotBlank() && !isResettingPassword,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary500)
                    ) {
                        if (isResettingPassword) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Send Reset Link")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showForgotPasswordDialog = false
                    forgotPasswordSent = false
                    forgotPasswordError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}
