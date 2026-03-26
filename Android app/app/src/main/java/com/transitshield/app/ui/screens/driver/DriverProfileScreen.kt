package com.transitshield.app.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.UserDto
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DriverProfileScreen(navController: NavController) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val userState = remember { mutableStateOf<UserDto?>(null) }
    val isEditingState = remember { mutableStateOf(false) }
    val editNameState = remember { mutableStateOf("") }
    val editAgeState = remember { mutableStateOf("") }
    val editPhoneState = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.apiService.getMe()
            userState.value = me
            editNameState.value = me.fullName ?: ""
            editAgeState.value = me.age?.toString() ?: ""
            editPhoneState.value = me.phoneNumber ?: ""
        } catch (e: Exception) {
            // handle error
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Driver Profile", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.verticalGradient(listOf(BgCard, BgSurface)))
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(BlueElectric, BlueDark))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userState.value?.fullName?.take(2)?.uppercase() ?: "DR", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(userState.value?.fullName ?: "Loading...", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(userState.value?.email ?: "", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(userState.value?.role ?: "DRIVER")
                        if (isEditingState.value) {
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        val updated = RetrofitClient.apiService.updateMe(
                                            UserDto(
                                                id = userState.value?.id,
                                                fullName = editNameState.value,
                                                age = editAgeState.value.toIntOrNull(),
                                                email = userState.value?.email,
                                                phoneNumber = editPhoneState.value,
                                                role = userState.value?.role,
                                                isActive = userState.value?.isActive
                                            )
                                        )
                                        userState.value = updated
                                        isEditingState.value = false
                                    } catch (e: Exception) {
                                    }
                                }
                            }) { Text("Save") }
                            Button(onClick = { isEditingState.value = false }, colors = ButtonDefaults.buttonColors(containerColor = BgElevated)) { Text("Cancel", color = TextPrimary) }
                        } else {
                            Button(onClick = { isEditingState.value = true }) { Text("Edit Profile") }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("Driver Information")
                        Spacer(Modifier.height(4.dp))
                        if (isEditingState.value) {
                            OutlinedTextField(value = editNameState.value, onValueChange = { editNameState.value = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = editAgeState.value, onValueChange = { editAgeState.value = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = editPhoneState.value, onValueChange = { editPhoneState.value = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                        } else {
                            InfoRow("User ID", userState.value?.id?.toString() ?: "-")
                            InfoRow("Age", userState.value?.age?.toString() ?: "-")
                            InfoRow("Phone", userState.value?.phoneNumber ?: "-")
                            InfoRow("Email", userState.value?.email ?: "-")
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("Performance")
                        Spacer(Modifier.height(4.dp))
                        InfoRow("Account Status", if (userState.value?.isActive == true) "Active" else "Inactive")
                        InfoRow("Join Date", formatProfileDate(userState.value?.createdAt))
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RedError.copy(alpha = 0.15f), contentColor = RedError)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

private fun formatProfileDate(createdAt: String?): String {
    return if (createdAt.isNullOrBlank()) "-" else createdAt.substringBefore("T")
}
