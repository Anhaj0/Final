package com.transitshield.app.ui.screens.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.LostItemReportDto
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BgSurface
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.RedError
import com.transitshield.app.ui.theme.TextMuted
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary

@Composable
fun DriverHomeScreen(navController: NavController) {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var lostItems by remember { mutableStateOf<List<LostItemReportDto>>(emptyList()) }

    LaunchedEffect(Unit) {
        loading = true
        error = null
        runCatching { RetrofitClient.apiService.getDriverLostItems() }
            .onSuccess { lostItems = it }
            .onFailure { error = it.message ?: "Failed to load active alerts" }
        loading = false
    }

    Scaffold(
        topBar = { AppTopBar(title = "Driver Dashboard", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep,
        bottomBar = {
            DriverBottomNav(
                onProfileClick = { navController.navigate(Screen.DriverProfile.route) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(BgSurface, BgCard)))
                            .padding(16.dp)
                    ) {
                        Text("Demerit Status", color = TextSecondary, fontSize = 12.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("2 / 5", color = RedError, fontWeight = FontWeight.ExtraBold, fontSize = 28.sp)
                        Text("Keep your record clean to remain on duty.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Current Speed",
                        value = "34 km/h",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        title = "Adherence",
                        value = "92%",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text("Active Alerts", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            if (loading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = BlueElectric)
                    }
                }
            }

            error?.let { message ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.12f))
                    ) {
                        Text(message, color = RedError, modifier = Modifier.padding(12.dp), fontSize = 12.sp)
                    }
                }
            }

            if (!loading && error == null && lostItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Text(
                            "No active lost-item alerts for your assigned bus.",
                            color = TextSecondary,
                            modifier = Modifier.padding(14.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            items(lostItems) { item ->
                LostItemAlertCard(item)
            }
        }
    }
}

@Composable
private fun MetricCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun LostItemAlertCard(report: LostItemReportDto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(report.itemTitle ?: "Lost Item", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Text(report.description ?: "No description provided", color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text("Reporter: ${report.reporterName ?: "Unknown"}", color = BlueElectric, fontSize = 12.sp)
            Text("Phone: ${report.reporterPhoneNumber ?: "N/A"}", color = BlueElectric, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DriverBottomNav(onProfileClick: () -> Unit) {
    NavigationBar(containerColor = BgSurface, tonalElevation = 0.dp) {
        NavigationBarItem(selected = true, onClick = { }, icon = { Icon(Icons.Default.Dashboard, null) }, label = { Text("Dashboard") })
        NavigationBarItem(selected = false, onClick = { }, icon = { Icon(Icons.Default.NotificationsActive, null) }, label = { Text("Alerts") })
        NavigationBarItem(selected = false, onClick = onProfileClick, icon = { Icon(Icons.Default.Person, null) }, label = { Text("Profile") })
    }
}
