package com.transitshield.app.ui.screens.passenger

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.BusLocationDto
import com.transitshield.app.data.network.dto.PassengerTripFlowStore
import com.transitshield.app.data.network.dto.RouteVariantStopDto
import com.transitshield.app.data.network.dto.StopDto
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.InfoRow
import com.transitshield.app.ui.components.SectionHeader
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BgSurface
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.GreenSuccess
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary
import kotlin.math.atan2
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.delay
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun LiveTrackerScreen(navController: NavController) {
    val activeTrip = PassengerTripFlowStore.activeTrip
    val scanResponse = PassengerTripFlowStore.qrScanResponse
    val selectedDestination = PassengerTripFlowStore.selectedDestinationStop

    val trackedBusId = activeTrip?.busId ?: scanResponse?.busId
    val routeVariantId = activeTrip?.routeVariantId ?: scanResponse?.routeVariantId
    val targetStopId = activeTrip?.selectedDestinationStopId ?: selectedDestination?.id
    val fallbackStopName = activeTrip?.selectedDestinationStopName ?: selectedDestination?.stopName

    var liveBus by remember { mutableStateOf<BusLocationDto?>(null) }
    var targetStop by remember { mutableStateOf<TrackedStop?>(selectedDestination?.toTrackedStop()) }
    var statusMessage by remember { mutableStateOf("Loading live tracker...") }

    LaunchedEffect(trackedBusId, routeVariantId, targetStopId) {
        if (trackedBusId == null) {
            statusMessage = "No boarded or selected bus found."
            return@LaunchedEffect
        }

        while (true) {
            try {
                liveBus = RetrofitClient.apiService.getBusLocation(trackedBusId)

                if (routeVariantId != null) {
                    val routeStops = RetrofitClient.apiService.getRouteVariantStops()
                        .filter { it.routeVariantId == routeVariantId }
                        .sortedBy { it.stopOrder ?: Int.MAX_VALUE }

                    targetStop = routeStops.firstOrNull { it.stopId == targetStopId }?.toTrackedStop()
                        ?: targetStop
                }

                statusMessage = "Live bus position updated"
            } catch (e: Exception) {
                statusMessage = "Failed to load tracker: ${e.message ?: "Unknown error"}"
            }

            delay(15_000L)
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
            val context = androidx.compose.ui.platform.LocalContext.current
            LaunchedEffect(Unit) {
                Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
            }

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
                            controller.setCenter(GeoPoint(6.9271, 79.8612))
                        }
                    },
                    update = { mapView ->
                        mapView.overlays.clear()

                        var focusPoint: GeoPoint? = null

                        liveBus?.takeIf { it.latitude != null && it.longitude != null }?.let { bus ->
                            val busPoint = GeoPoint(bus.latitude!!, bus.longitude!!)
                            focusPoint = busPoint
                            val marker = Marker(mapView).apply {
                                position = busPoint
                                title = bus.busDisplayTitle()
                                snippet = "Speed: ${formatSpeed(bus.speedKmh)}"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mapView.overlays.add(marker)
                        }

                        targetStop?.takeIf { it.latitude != null && it.longitude != null }?.let { stop ->
                            val stopPoint = GeoPoint(stop.latitude!!, stop.longitude!!)
                            if (focusPoint == null) focusPoint = stopPoint
                            val marker = Marker(mapView).apply {
                                position = stopPoint
                                title = stop.name ?: "Destination Stop"
                                snippet = "Tracked stop"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            }
                            mapView.overlays.add(marker)
                        }

                        focusPoint?.let { mapView.controller.animateTo(it) }
                        mapView.invalidate()
                    },
                    modifier = Modifier.fillMaxSize()
                )

                liveBus?.let { bus ->
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BgSurface.copy(alpha = 0.95f))
                    ) {
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DirectionsBus, contentDescription = null, tint = BlueElectric)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Tracking: ${bus.busDisplayTitle()}",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Speed: ${formatSpeed(bus.speedKmh)} • ${bus.occupancyStatus ?: "N/A"}",
                                    color = GreenSuccess,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Live Tracking")
                        Text(statusMessage, color = TextSecondary, fontSize = 12.sp)
                        InfoRow("Bus", liveBus?.busDisplayTitle() ?: "-")
                        InfoRow("Destination", targetStop?.name ?: fallbackStopName ?: "-")
                        InfoRow("Last Update", liveBus?.recordedAt?.take(16)?.replace("T", " ") ?: "-")
                        InfoRow("Current Speed", formatSpeed(liveBus?.speedKmh))
                        InfoRow("ETA", calculateEtaLabel(liveBus, targetStop))
                        InfoRow("Distance to Stop", calculateDistanceLabel(liveBus, targetStop))
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        SectionHeader("Tracked Coordinates")
                        InfoRow("Bus Latitude", liveBus?.latitude?.let { "%.6f".format(it) } ?: "-")
                        InfoRow("Bus Longitude", liveBus?.longitude?.let { "%.6f".format(it) } ?: "-")
                        InfoRow("Stop Latitude", targetStop?.latitude?.let { "%.6f".format(it) } ?: "-")
                        InfoRow("Stop Longitude", targetStop?.longitude?.let { "%.6f".format(it) } ?: "-")
                    }
                }
            }
        }
    }
}

private data class TrackedStop(
    val id: Long?,
    val name: String?,
    val latitude: Double?,
    val longitude: Double?
)

private fun StopDto.toTrackedStop(): TrackedStop {
    return TrackedStop(
        id = id,
        name = stopName,
        latitude = latitude,
        longitude = longitude
    )
}

private fun RouteVariantStopDto.toTrackedStop(): TrackedStop {
    return TrackedStop(
        id = stopId,
        name = stopName,
        latitude = stopLatitude,
        longitude = stopLongitude
    )
}

private fun BusLocationDto.busDisplayTitle(): String {
    return "Bus ${busId ?: "-"}"
}

private fun formatSpeed(speedKmh: Double?): String {
    return if (speedKmh == null) "-" else "%.1f km/h".format(speedKmh)
}

private fun calculateDistanceLabel(bus: BusLocationDto?, stop: TrackedStop?): String {
    val busLat = bus?.latitude
    val busLon = bus?.longitude
    val stopLat = stop?.latitude
    val stopLon = stop?.longitude

    if (busLat == null || busLon == null || stopLat == null || stopLon == null) {
        return "-"
    }

    val distanceKm = haversineKm(busLat, busLon, stopLat, stopLon)
    return if (distanceKm < 0.15) "Arriving now" else "%.2f km".format(distanceKm)
}

private fun calculateEtaLabel(bus: BusLocationDto?, stop: TrackedStop?): String {
    val busLat = bus?.latitude
    val busLon = bus?.longitude
    val stopLat = stop?.latitude
    val stopLon = stop?.longitude

    if (busLat == null || busLon == null || stopLat == null || stopLon == null) {
        return "-"
    }

    val distanceKm = haversineKm(busLat, busLon, stopLat, stopLon)
    if (distanceKm < 0.15) {
        return "Arriving now"
    }

    val speedKmh = bus.speedKmh?.takeIf { it > 5.0 } ?: 25.0
    val etaMinutes = ceil((distanceKm / speedKmh) * 60.0).toInt().coerceAtLeast(1)
    return "$etaMinutes min"
}

private fun haversineKm(
    startLat: Double,
    startLon: Double,
    endLat: Double,
    endLon: Double
): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(endLat - startLat)
    val dLon = Math.toRadians(endLon - startLon)
    val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(startLat)) * cos(Math.toRadians(endLat)) * sin(dLon / 2).pow(2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}
