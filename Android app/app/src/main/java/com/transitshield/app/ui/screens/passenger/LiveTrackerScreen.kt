package com.transitshield.app.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*

import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.BusLocationDto

import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.tileprovider.tilesource.TileSourceFactory

@Composable
fun LiveTrackerScreen(navController: NavController) {
    var buses by remember { mutableStateOf<List<BusLocationDto>>(emptyList()) }
    var selectedRoute by remember { mutableStateOf("All") }
    var loadError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            buses = RetrofitClient.apiService.getLiveLocations()
            loadError = null
        } catch (e: Exception) {
            loadError = e.message ?: "Failed to load live bus locations."
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Live Tracker", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val context = LocalContext.current
            LaunchedEffect(Unit) {
                Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            }
            
            // Real OSM Map
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
            ) {
                AndroidView(
                    factory = {
                        MapView(it).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(13.0)
                            // Default to Colombo center
                            controller.setCenter(GeoPoint(6.9271, 79.8612))
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.clear()
                        val filteredBuses = if (selectedRoute == "All") buses else buses.filter { it.routeVariantId?.toString() == selectedRoute }
                        
                        var firstGeoPoint: GeoPoint? = null
                        filteredBuses.forEach { bus ->
                            if (bus.latitude != null && bus.longitude != null) {
                                val geoPoint = GeoPoint(bus.latitude, bus.longitude)
                                if (firstGeoPoint == null) firstGeoPoint = geoPoint
                                
                                val marker = Marker(mapView)
                                marker.position = geoPoint
                                marker.title = "Bus #${bus.busId ?: "?"} (Route ${bus.routeVariantId ?: "?"})"
                                marker.snippet = "Speed: ${bus.speedKmh ?: 0} km/h"
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                mapView.overlays.add(marker)
                            }
                        }
                        
                        if (firstGeoPoint != null) {
                            mapView.controller.animateTo(firstGeoPoint)
                        }
                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Tracked bus banner
                if (buses.isNotEmpty()) {
                    val firstBus = buses.first()
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface.copy(alpha = 0.95f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = BlueElectric, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Tracking: Bus ${firstBus.busId}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Speed: ${firstBus.speedKmh ?: 0} km/h • ${firstBus.occupancyStatus ?: "N/A"}", color = GreenSuccess, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Route Filter Chips
            loadError?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.12f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, RedError.copy(alpha = 0.35f))
                ) {
                    Text(
                        text = error,
                        color = RedError,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            val availableRoutes = listOf("All") + buses.mapNotNull { it.routeVariantId?.toString() }.distinct()
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(availableRoutes) { route ->
                    FilterChip(
                        selected = selectedRoute == route,
                        onClick = { selectedRoute = route },
                        label = { Text(route, fontSize = 13.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BlueElectric,
                            selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                            containerColor = BgCard,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            SectionHeader(
                title = "Nearby Buses",
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(4.dp))

            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val filtered = if (selectedRoute == "All") buses else buses.filter { it.routeVariantId?.toString() == selectedRoute }
                items(filtered) { bus ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(BlueElectric.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(bus.routeVariantId?.toString() ?: "?", color = BlueElectric, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Bus #${bus.busId ?: "?"}", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text("Recorded: ${bus.recordedAt?.take(16)?.replace("T", " ") ?: "Now"}", color = GreenSuccess, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextMuted)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        color = TextPrimary,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        modifier = modifier
    )
}

@Composable
private fun BusPin(modifier: Modifier, routeNumber: String) {
    Box(
        modifier = modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(BlueElectric),
        contentAlignment = Alignment.Center
    ) {
        Text(routeNumber, color = androidx.compose.ui.graphics.Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
    }
}
