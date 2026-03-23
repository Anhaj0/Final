package com.transitshield.app.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

@Composable
fun PassengerHomeScreen(navController: NavController) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<UserDto?>(null) }
    var walletBalance by remember { mutableStateOf(0.0) }
    var points by remember { mutableStateOf(0.0) }
    var selectedTab by remember { mutableStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.apiService.getMe()
            user = me
            walletBalance = me.walletBalance ?: 0.0
            val balanceMap = RetrofitClient.apiService.getMyBalance()
            points = balanceMap["totalPoints"] ?: 0.0
        } catch (e: Exception) {
            Toast.makeText(context, e.message ?: "Failed to load profile", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        containerColor = BgDeep,
        bottomBar = {
            PassengerBottomNav(selectedTab = selectedTab, onTabSelected = { selectedTab = it }, navController = navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .background(BgDeep)
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(listOf(BgSurface, BgDeep))
                    )
                    .padding(horizontal = 20.dp, vertical = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Good Morning,", color = TextSecondary, fontSize = 13.sp)
                        Text(
                            text = user?.fullName ?: "Loading...",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(BlueElectric),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = user?.fullName?.take(2)?.uppercase() ?: "PA", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Wallet Card
                WalletCard(
                    balance = String.format("%.2f", walletBalance),
                    points = String.format("%.1f", points)
                )

                Spacer(Modifier.height(20.dp))

                // Active Trip Card
                ActiveTripBanner(navController)

                Spacer(Modifier.height(20.dp))

                // Quick Actions
                SectionHeader(title = "Quick Actions")
                Spacer(Modifier.height(10.dp))

                data class ActionItem(val title: String, val icon: ImageVector, val color: Color, val route: String)
                val actions = listOf(
                    ActionItem("Scan QR\n/ Ticket", Icons.Default.QrCodeScanner, BlueElectric, Screen.QrScan.route),
                    ActionItem("Live\nTracker", Icons.Default.Map, GreenSuccess, Screen.LiveTracker.route),
                    ActionItem("Rewards", Icons.Default.Star, OrangeWarning, Screen.Rewards.route),
                    ActionItem("Tasks", Icons.Default.TaskAlt, PurpleInfo, Screen.PassengerTasks.route),
                    ActionItem("Lost Item", Icons.Default.FindInPage, PurpleInfo, Screen.LostItemReport.route),
                    ActionItem("Complaint", Icons.Default.Report, RedError, Screen.ComplaintSubmission.route),
                    ActionItem("My Trips", Icons.Default.History, BlueLight, Screen.RecentTrips.route)
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(actions) { action ->
                        QuickActionCard(
                            title = action.title,
                            icon = action.icon,
                            iconTint = action.color,
                            onClick = { navController.navigate(action.route) }
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Stats Row
                SectionHeader(title = "Your Overview")
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    DashboardStatCard(
                        label = "Trips This Month",
                        value = "14",
                        icon = Icons.Default.DirectionsBus,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardStatCard(
                        label = "Total Spent",
                        value = "LKR 680",
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = GreenSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Recent Trips
                SectionHeader(
                    title = "Recent Trips",
                    actionText = "See All",
                    onAction = { navController.navigate(Screen.RecentTrips.route) }
                )
                Spacer(Modifier.height(10.dp))

                Text("No recent trips found for your account.", color = TextMuted, fontSize = 13.sp)

                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ActiveTripBanner(navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BlueElectric.copy(alpha = 0.12f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, BlueElectric.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(BlueElectric.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = BlueElectric, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Active Trip", color = BlueLight, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                Text("Route 138 – Narahenpita → Nugegoda", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            }
            TextButton(onClick = { navController.navigate(Screen.ActiveTrip.route) }) {
                Text("View", color = BlueElectric, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PassengerBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavController
) {
    NavigationBar(
        containerColor = BgSurface,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple("Home", Icons.Default.Home, Screen.PassengerHome.route),
            Triple("Trips", Icons.Default.History, Screen.RecentTrips.route),
            Triple("Rewards", Icons.Default.Star, Screen.Rewards.route),
            Triple("Profile", Icons.Default.Person, Screen.PassengerProfile.route)
        )
        items.forEachIndexed { index, (label, icon, route) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = {
                    onTabSelected(index)
                    if (index != 0) navController.navigate(route)
                },
                icon = { Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp)) },
                label = { Text(label, fontSize = 11.sp) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BlueElectric,
                    selectedTextColor = BlueElectric,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted,
                    indicatorColor = BlueElectric.copy(alpha = 0.12f)
                )
            )
        }
    }
}
