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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import android.widget.Toast
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.UserDto
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DriverProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    var user by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<UserDto?>(null) }
    var isEditing by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var isLoading by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
    var errorMessage by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    
    var editName by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var editAge by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }
    var editPhone by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("") }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null
        try {
            val me = RetrofitClient.apiService.getMe()
            user = me
            editName = me.fullName ?: ""
            editAge = me.age?.toString() ?: ""
            editPhone = me.phoneNumber ?: ""
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load profile"
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
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
                        Text(user?.fullName?.take(2)?.uppercase() ?: "DR", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(user?.fullName ?: "Loading...", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(user?.email ?: "", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    if (isLoading) {
                        CircularProgressIndicator(color = BlueElectric, strokeWidth = 2.dp)
                        Spacer(Modifier.height(12.dp))
                    }
                    errorMessage?.let {
                        Text(it, color = RedError, fontSize = 12.sp)
                        Spacer(Modifier.height(12.dp))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadge(user?.role ?: "DRIVER")
                        if (isEditing) {
                            Button(onClick = {
                                coroutineScope.launch {
                                    try {
                                        val updated = RetrofitClient.apiService.updateMe(
                                            UserDto(
                                                id = user?.id,
                                                fullName = editName,
                                                age = editAge.toIntOrNull(),
                                                email = user?.email,
                                                phoneNumber = editPhone,
                                                role = user?.role,
                                                isActive = user?.isActive
                                            )
                                        )
                                        user = updated
                                        isEditing = false
                                    } catch (e: Exception) {
                                        val message = e.message ?: "Failed to update profile"
                                        errorMessage = message
                                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }) { Text("Save") }
                            Button(onClick = { isEditing = false }, colors = ButtonDefaults.buttonColors(containerColor = BgElevated)) { Text("Cancel", color = TextPrimary) }
                        } else {
                            Button(onClick = { isEditing = true }) { Text("Edit Profile") }
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
                        if (isEditing) {
                            OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = editAge, onValueChange = { editAge = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                        } else {
                            InfoRow("User ID", user?.id?.toString() ?: "-")
                            InfoRow("Age", user?.age?.toString() ?: "-")
                            InfoRow("Phone", user?.phoneNumber ?: "-")
                            InfoRow("Email", user?.email ?: "-")
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
                        InfoRow("Account Status", if (user?.isActive == true) "Active" else "Inactive")
                        InfoRow("Join Date", "-")
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
