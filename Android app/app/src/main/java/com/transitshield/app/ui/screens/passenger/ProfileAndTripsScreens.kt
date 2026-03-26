package com.transitshield.app.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
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
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Composable
fun RecentTripsScreen(navController: NavController) {
    val tripsState = remember { mutableStateOf<List<com.transitshield.app.data.network.dto.PassengerTripDto>>(emptyList()) }
    val loadingState = remember { mutableStateOf(true) }
    val errorState = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        loadingState.value = true
        errorState.value = null
        try {
            val me = RetrofitClient.apiService.getMe()
            val passengerId = me.id
            tripsState.value = if (passengerId != null) {
                RetrofitClient.apiService.getTripHistory(passengerId)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            errorState.value = e.message ?: "Failed to load trip history"
        } finally {
            loadingState.value = false
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "My Trips", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            when {
                loadingState.value -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = BlueElectric)
                }

                errorState.value != null -> {
                    EmptyStateCard(
                        title = "Trip History Unavailable",
                        message = errorState.value ?: "Unable to load trip history.",
                        icon = Icons.Default.ErrorOutline,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                tripsState.value.isEmpty() -> {
                    EmptyStateCard(
                        title = "No Recent Trips",
                        message = "Your completed and active trips will appear here.",
                        icon = Icons.Default.History,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 12.dp)
                    ) {
                        items(tripsState.value) { trip ->
                            TripCard(
                                routeNumber = trip.busDisplayName ?: "Bus",
                                routeName = trip.routeName ?: "Route unavailable",
                                from = trip.boardingStopName ?: "-",
                                to = trip.selectedDestinationStopName ?: trip.actualExitStopId?.toString() ?: "-",
                                date = formatTripDate(trip.createdAt),
                                fare = formatCurrency(trip.totalFareLkr),
                                status = trip.tripStatus ?: "UNKNOWN"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PassengerProfileScreen(navController: NavController) {
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val userState = remember { mutableStateOf<UserDto?>(null) }
    val isEditingState = remember { mutableStateOf(false) }
    val editNameState = remember { mutableStateOf("") }
    val editAgeState = remember { mutableStateOf("") }
    val editPhoneState = remember { mutableStateOf("") }
    val walletBalanceState = remember { mutableStateOf("--") }
    val rewardPointsState = remember { mutableStateOf("--") }

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.apiService.getMe()
            userState.value = me
            editNameState.value = me.fullName ?: ""
            editAgeState.value = me.age?.toString() ?: ""
            editPhoneState.value = me.phoneNumber ?: ""
            val passengerId = me.id
            if (passengerId != null) {
                val balance = RetrofitClient.apiService.getPassengerBalance(passengerId)
                walletBalanceState.value = formatCurrency(balance.walletBalance)
                rewardPointsState.value = formatPoints(balance.totalPoints)
            }
        } catch (e: Exception) {
            // handle error
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "My Profile", onBack = { navController.popBackStack() }) },
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
                // Profile Header
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
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Brush.radialGradient(listOf(BlueElectric, BlueDark))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(userState.value?.fullName?.take(2)?.uppercase() ?: "??", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(userState.value?.fullName ?: "Loading...", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(userState.value?.email ?: "", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatusBadge(userState.value?.role ?: "PASSENGER")
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
                                        // log error
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
                        SectionHeader("Account Details")
                        Spacer(Modifier.height(4.dp))
                        if (isEditingState.value) {
                            OutlinedTextField(value = editNameState.value, onValueChange = { editNameState.value = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = editAgeState.value, onValueChange = { editAgeState.value = it }, label = { Text("Age") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = editPhoneState.value, onValueChange = { editPhoneState.value = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth())
                        } else {
                            InfoRow("User ID", userState.value?.id?.toString() ?: "-")
                            InfoRow("Phone", userState.value?.phoneNumber ?: "-")
                            InfoRow("Age", userState.value?.age?.toString() ?: "-")
                            InfoRow("Email", userState.value?.email ?: "-")
                        }
                    }
                }
            }

            item {
                WalletCard(
                    balance = walletBalanceState.value,
                    points = rewardPointsState.value
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader("Preferences")
                        Spacer(Modifier.height(4.dp))
                        ProfileMenuRow("Notifications", Icons.Default.Notifications)
                        ProfileMenuRow("Saved Cards", Icons.Default.CreditCard)
                        ProfileMenuRow("Language", Icons.Default.Language)
                        ProfileMenuRow("Privacy Policy", Icons.Default.Shield)
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
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ProfileMenuRow(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = BlueElectric, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = TextPrimary, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp))
    }
    Divider(color = BorderSubtle.copy(alpha = 0.5f))
}

private fun formatCurrency(amount: Double?): String {
    val formatter = DecimalFormat("0.00")
    return formatter.format(amount ?: 0.0)
}

private fun formatPoints(points: Double?): String {
    return ((points ?: 0.0).toInt()).toString()
}

private fun formatTripDate(createdAt: String?): String {
    if (createdAt.isNullOrBlank()) {
        return "-"
    }

    return try {
        OffsetDateTime.parse(createdAt).format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
    } catch (_: DateTimeParseException) {
        try {
            LocalDateTime.parse(createdAt).format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
        } catch (_: DateTimeParseException) {
            createdAt
        }
    }
}
