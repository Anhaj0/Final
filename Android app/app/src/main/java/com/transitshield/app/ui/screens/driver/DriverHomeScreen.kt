package com.transitshield.app.ui.screens.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.text.format.DateFormat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.DriverDashboardDto
import com.transitshield.app.data.network.dto.LocationUpdateRequest
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun DriverHomeScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) }
    var dashboard by remember { mutableStateOf<DriverDashboardDto?>(null) }
    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }
    var locationStatus by remember { mutableStateOf("Waiting for active trip") }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        locationStatus = if (hasLocationPermission) {
            "Location permission granted"
        } else {
            "Location permission required for live bus updates"
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        try {
            dashboard = RetrofitClient.apiService.getDriverDashboard()
        } catch (e: Exception) {
            // handle
        }
    }

    LaunchedEffect(dashboard?.activeAssignmentId, hasLocationPermission) {
        if (dashboard?.activeAssignmentId != null && !hasLocationPermission) {
            locationStatus = "Location permission required for live bus updates"
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    DisposableEffect(dashboard?.activeAssignmentId, dashboard?.activeBusId, dashboard?.driverProfileId, hasLocationPermission) {
        val activeBusId = dashboard?.activeBusId
        val driverProfileId = dashboard?.driverProfileId

        if (!hasLocationPermission || activeBusId == null || driverProfileId == null) {
            if (dashboard?.activeAssignmentId == null) {
                locationStatus = "No active trip for location sharing"
            }
            onDispose { }
        } else {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                locationStatus = "Location service unavailable"
                onDispose { }
            } else {
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        locationStatus = "Sending live location..."
                        coroutineScope.launch {
                            sendLocationUpdate(
                                location = location,
                                busId = activeBusId,
                                driverProfileId = driverProfileId,
                                onSuccess = {
                                    locationStatus = "Last updated ${formatLocationTime(context)}"
                                },
                                onError = {
                                    locationStatus = it
                                }
                            )
                        }
                    }

                    override fun onProviderDisabled(provider: String) {
                        locationStatus = "Enable device location to share bus position"
                    }

                    override fun onProviderEnabled(provider: String) {
                        locationStatus = "Location provider enabled"
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                }

                try {
                    val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
                        .filter(locationManager::isProviderEnabled)

                    if (providers.isEmpty()) {
                        locationStatus = "Enable GPS to share live bus location"
                    } else {
                        providers.forEach { provider ->
                            locationManager.requestLocationUpdates(
                                provider,
                                15_000L,
                                0f,
                                listener,
                                Looper.getMainLooper()
                            )
                            locationManager.getLastKnownLocation(provider)?.let { lastKnownLocation ->
                                coroutineScope.launch {
                                    sendLocationUpdate(
                                        location = lastKnownLocation,
                                        busId = activeBusId,
                                        driverProfileId = driverProfileId,
                                        onSuccess = {
                                            locationStatus = "Last updated ${formatLocationTime(context)}"
                                        },
                                        onError = {
                                            locationStatus = it
                                        }
                                    )
                                }
                            }
                        }
                    }
                } catch (_: SecurityException) {
                    locationStatus = "Location permission required for live bus updates"
                }

                onDispose {
                    locationManager.removeUpdates(listener)
                }
            }
        }
    }

    Scaffold(
        containerColor = BgDeep,
        bottomBar = {
            DriverBottomNav(
                selectedTab = selectedTab, 
                onTabSelected = { selectedTab = it }, 
                navController = navController,
                alertCount = dashboard?.alerts?.size ?: 0
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(BgDeep),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                // Driver Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(BgSurface, BgDeep)))
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Brush.radialGradient(listOf(BlueElectric, BlueDark))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(dashboard?.profileInitial ?: "DR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(dashboard?.name ?: "Loading...", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("ID: ${dashboard?.id ?: "-"} • ${dashboard?.depot ?: "-"}", color = TextSecondary, fontSize = 12.sp)
                        }
                        StatusBadge(if (dashboard?.isOnline == true) "ONLINE" else "OFFLINE")
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    // Demerit Card
                    val demerits = dashboard?.demerits ?: 0
                    val maxDemerits = 10
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (demerits < 5) BgCard else RedError.copy(alpha = 0.1f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (demerits < 5) BorderSubtle else RedError.copy(alpha = 0.4f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Warning, contentDescription = null, tint = OrangeWarning, modifier = Modifier.size(22.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Demerit Points", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                                }
                                Text("$demerits/$maxDemerits", color = OrangeWarning, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                            }
                            Spacer(Modifier.height(12.dp))
                            LinearProgressIndicator(
                                progress = { demerits.toFloat() / maxDemerits },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (demerits < 5) OrangeWarning else RedError,
                                trackColor = BgElevated
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = if (demerits < 5) "Good standing – keep it up!" else "Warning: High demerit count",
                                color = if (demerits < 5) GreenSuccess else RedError,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Current Route Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BlueElectric.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BlueElectric.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = BlueElectric, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Active Route / Schedule", color = BlueLight, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(dashboard?.currentRoute ?: "No Current Route", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { navController.navigate(Screen.ConductorVerification.route) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = BlueElectric)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Verify Passenger Ticket")
                            }
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = locationStatus,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            if (!hasLocationPermission && dashboard?.activeAssignmentId != null) {
                                Spacer(Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Enable Live Location")
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                                StatPill("Trips Today", dashboard?.tripsToday?.toString() ?: "0")
                                StatPill("On-Time", "${dashboard?.onTimePercentage ?: 0}%")
                                StatPill("Complaints", dashboard?.complaintsToday?.toString() ?: "0")
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats Row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        DashboardStatCard(
                            label = "Trips Today",
                            value = (dashboard?.tripsToday ?: 0).toString(),
                            icon = Icons.Default.Route,
                            modifier = Modifier.weight(1f)
                        )
                        DashboardStatCard(
                            label = "On-Time %",
                            value = "${dashboard?.onTimePercentage ?: 0}%",
                            icon = Icons.Default.Timer,
                            accentColor = GreenSuccess,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    SectionHeader(title = "Active Alerts")
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (dashboard?.alerts != null) {
                items(dashboard!!.alerts!!) { alert ->
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = when (alert.type) {
                                    "COMPLAINT" -> RedError.copy(alpha = 0.08f)
                                    "LOST_ITEM" -> PurpleInfo.copy(alpha = 0.08f)
                                    else -> BlueElectric.copy(alpha = 0.08f)
                                }
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                when (alert.type) {
                                    "COMPLAINT" -> RedError.copy(alpha = 0.3f)
                                    "LOST_ITEM" -> PurpleInfo.copy(alpha = 0.3f)
                                    else -> BlueElectric.copy(alpha = 0.3f)
                                }
                            )
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Icon(
                                    imageVector = when (alert.type) {
                                        "COMPLAINT" -> Icons.Default.Report
                                        "LOST_ITEM" -> Icons.Default.FindInPage
                                        else -> Icons.Default.Info
                                    },
                                    contentDescription = null,
                                    tint = when (alert.type) {
                                        "COMPLAINT" -> RedError
                                        "LOST_ITEM" -> PurpleInfo
                                        else -> BlueElectric
                                    },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(alert.title ?: "", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text(alert.message ?: "", color = TextSecondary, fontSize = 12.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(alert.timestamp ?: "", color = TextMuted, fontSize = 11.sp)
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Spacer(Modifier.height(6.dp))
                    SectionHeader(title = "Lost Item Alerts")
                    Spacer(Modifier.height(8.dp))
                }
            }

            if (dashboard?.lostItems != null) {
                items(dashboard!!.lostItems!!) { item ->
                    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = BgCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(PurpleInfo.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Inventory, contentDescription = null, tint = PurpleInfo, modifier = Modifier.size(22.dp))
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.item ?: "", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    Text("By: ${item.passengerName ?: "?"} • Route ${item.route ?: "?"}", color = TextSecondary, fontSize = 12.sp)
                                    Text(item.time ?: "", color = TextMuted, fontSize = 11.sp)
                                }
                                StatusBadge(item.status ?: "UNKNOWN")
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}

private suspend fun sendLocationUpdate(
    location: Location,
    busId: Long,
    driverProfileId: Long,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        RetrofitClient.apiService.updateLocation(
            LocationUpdateRequest(
                busId = busId,
                driverProfileId = driverProfileId,
                latitude = location.latitude,
                longitude = location.longitude,
                speedKmh = if (location.hasSpeed()) location.speed * 3.6 else null,
                heading = if (location.hasBearing()) location.bearing.toDouble() else null,
                occupancyStatus = null,
                sourceType = "DRIVER_APP"
            )
        )
        onSuccess()
    } catch (e: Exception) {
        onError("Location update failed: ${e.message ?: "Unknown error"}")
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

private fun formatLocationTime(context: Context): String {
    return DateFormat.format("hh:mm:ss a", System.currentTimeMillis()).toString()
}

@Composable
private fun StatPill(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(label, color = TextMuted, fontSize = 10.sp)
    }
}

@Composable
private fun DriverBottomNav(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    navController: NavController,
    alertCount: Int
) {
    NavigationBar(
        containerColor = BgSurface,
        tonalElevation = 0.dp
    ) {
        val items = listOf(
            Triple("Dashboard", Icons.Default.Dashboard, Screen.DriverHome.route),
            Triple("Schedule", Icons.Default.EventNote, Screen.DriverHome.route),
            Triple("Alerts", Icons.Default.NotificationsActive, Screen.DriverHome.route),
            Triple("Profile", Icons.Default.Person, Screen.DriverProfile.route)
        )
        items.forEachIndexed { index, (label, icon, route) ->
            NavigationBarItem(
                selected = selectedTab == index,
                onClick = {
                    onTabSelected(index)
                    if (index == 3) navController.navigate(route)
                },
                icon = {
                    BadgedBox(badge = {
                        if (index == 2 && alertCount > 0) {
                            Badge { Text(alertCount.toString()) }
                        }
                    }) {
                        Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp))
                    }
                },
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
