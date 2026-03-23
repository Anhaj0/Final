package com.transitshield.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.transitshield.app.data.viewmodel.AuthViewModel
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.PrimaryButton
import com.transitshield.app.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.material.icons.filled.Settings
import com.transitshield.app.data.network.RetrofitClient

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("TSProps", Context.MODE_PRIVATE) }
    var showIpDialog by remember { mutableStateOf(false) }
    var ipInput by remember { mutableStateOf(prefs.getString("ip_address", "192.168.8.196") ?: "192.168.8.196") }

    if (showIpDialog) {
        AlertDialog(
            onDismissRequest = { showIpDialog = false },
            title = { Text("Server Config", fontWeight = FontWeight.Bold) },
            text = {
                OutlinedTextField(
                    value = ipInput,
                    onValueChange = { ipInput = it },
                    label = { Text("IP Address (e.g., 192.168.1.100)") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    prefs.edit().putString("ip_address", ipInput).apply()
                    showIpDialog = false
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showIpDialog = false }) { Text("Cancel") }
            }
        )
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.radialGradient(listOf(BlueElectric, BlueDark))),
                contentAlignment = Alignment.Center
            ) {
                Text("TS", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }

            Spacer(Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = { showIpDialog = true }, modifier = Modifier.align(Alignment.TopEnd)) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings", tint = TextMuted)
                }
                
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome Back",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Sign in to your TransitShield account",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(32.dp))

            // Email Field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = BlueElectric) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueElectric,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = BlueElectric,
                    focusedContainerColor = BgCard,
                    unfocusedContainerColor = BgCard,
                    focusedLabelColor = BlueElectric,
                    unfocusedLabelColor = TextMuted
                ),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // Password Field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password", color = TextMuted) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = BlueElectric) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = TextMuted
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BlueElectric,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = BlueElectric,
                    focusedContainerColor = BgCard,
                    unfocusedContainerColor = BgCard,
                    focusedLabelColor = BlueElectric,
                    unfocusedLabelColor = TextMuted
                ),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Text(
                    text = "Forgot password?",
                    color = BlueLight,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { }
                )
            }

            // Error message
            authViewModel.errorMessage?.let { error ->
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.12f))
                ) {
                    Text(
                        text = error,
                        color = RedError,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            PrimaryButton(
                text = if (authViewModel.isLoading) "Signing in..." else "Sign In",
                enabled = !authViewModel.isLoading && email.isNotBlank() && password.isNotBlank(),
                onClick = {
                    authViewModel.login(email, password) { response ->
                        when (response.role?.uppercase()) {
                            "PASSENGER" -> navController.navigate(Screen.PassengerHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                            "DRIVER" -> navController.navigate(Screen.DriverHome.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                            else -> { /* Admin shouldn't log in on mobile */ }
                        }
                    }
                }
            )

            Spacer(Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Don't have an account? ", color = TextSecondary, fontSize = 14.sp)
                Text(
                    text = "Register",
                    color = BlueElectric,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { navController.navigate(Screen.Register.route) }
                )
            }

            Spacer(Modifier.height(24.dp))

            // Backend connectivity hint
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Server Connection", color = TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("Backend: ${RetrofitClient.BASE_URL}", color = TextSecondary, fontSize = 11.sp)
                    Text("Real device target IP: 192.168.8.196", color = TextSecondary, fontSize = 11.sp)
                }
            }
        }
    }
}
