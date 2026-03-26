package com.transitshield.app.ui.screens.passenger

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.FarePreviewRequest
import com.transitshield.app.data.network.dto.PassengerTripFlowStore
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.InfoRow
import com.transitshield.app.ui.components.PrimaryButton
import com.transitshield.app.ui.components.SectionHeader
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BgElevated
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.GreenSuccess
import com.transitshield.app.ui.theme.TextMuted
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary

@Composable
fun TripDetailsScreen(navController: NavController) {
    val context = LocalContext.current
    val scanResponse = PassengerTripFlowStore.qrScanResponse
    val orderedStops = scanResponse?.orderedStops.orEmpty()
    val boardingStopId = scanResponse?.nearestBoardingStopId
    val boardingIndex = orderedStops.indexOfFirst { it.id == boardingStopId }.let { if (it >= 0) it else 0 }
    val selectableStops = if (orderedStops.size > boardingIndex + 1) orderedStops.drop(boardingIndex + 1) else emptyList()

    var selectedDestination by remember {
        mutableStateOf(PassengerTripFlowStore.selectedDestinationStop ?: selectableStops.firstOrNull())
    }
    var fare by remember { mutableStateOf(PassengerTripFlowStore.farePreview) }
    var balance by remember { mutableStateOf(PassengerTripFlowStore.passengerBalance?.walletBalance) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var loadingFare by remember { mutableStateOf(false) }

    LaunchedEffect(scanResponse?.busAssignmentId) {
        if (scanResponse == null) {
            Toast.makeText(context, "Scan a bus QR first.", Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return@LaunchedEffect
        }

        val passengerId = PassengerTripFlowStore.passengerId ?: return@LaunchedEffect
        try {
            val balanceDto = RetrofitClient.apiService.getPassengerBalance(passengerId)
            PassengerTripFlowStore.passengerBalance = balanceDto
            balance = balanceDto.walletBalance
        } catch (_: Exception) {
        }
    }

    LaunchedEffect(selectedDestination?.id, scanResponse?.routeVariantId, boardingStopId) {
        val routeVariantId = scanResponse?.routeVariantId
        val destinationStopId = selectedDestination?.id
        if (routeVariantId == null || boardingStopId == null || destinationStopId == null) {
            fare = null
            return@LaunchedEffect
        }

        loadingFare = true
        try {
            fare = RetrofitClient.apiService.previewFare(
                FarePreviewRequest(
                    routeVariantId = routeVariantId,
                    boardingStopId = boardingStopId,
                    destinationStopId = destinationStopId
                )
            )
            PassengerTripFlowStore.selectedDestinationStop = selectedDestination
            PassengerTripFlowStore.farePreview = fare
        } catch (e: Exception) {
            fare = null
            Toast.makeText(context, "Failed to load fare: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            loadingFare = false
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Trip Details", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (scanResponse == null) {
                Text("No scan data found.", color = TextPrimary)
                return@Column
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(scanResponse.busDisplayName ?: "Assigned Bus", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Route Variant ID: ${scanResponse.routeVariantId ?: "-"}", color = TextSecondary, fontSize = 13.sp)
                    Text("Driver Profile ID: ${scanResponse.driverProfileId ?: "-"}", color = TextSecondary, fontSize = 13.sp)
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Your Journey")
                    InfoRow("Boarding Stop", orderedStops.getOrNull(boardingIndex)?.stopName ?: "-")
                    Divider(color = BorderSubtle)
                    Text("Destination Stop", color = TextMuted, fontSize = 12.sp)
                    OutlinedButton(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedDestination?.stopName ?: "Select destination")
                    }
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        selectableStops.forEach { stop ->
                            DropdownMenuItem(
                                text = { Text(stop.stopName ?: "Stop ${stop.id}") },
                                onClick = {
                                    selectedDestination = stop
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Fare Summary")
                    InfoRow("Wallet Balance", "LKR ${"%.2f".format(balance ?: 0.0)}")
                    InfoRow("Destination", selectedDestination?.stopName ?: "-")
                    if (loadingFare) {
                        CircularProgressIndicator(color = BlueElectric)
                    } else {
                        InfoRow("Total Fare", fare?.let { "LKR ${"%.2f".format(it)}" } ?: "-")
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, GreenSuccess.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SectionHeader("Route Stops")
                    orderedStops.forEachIndexed { index, stop ->
                        val label = when {
                            index == boardingIndex -> "Boarding"
                            selectedDestination?.id == stop.id -> "Destination"
                            else -> null
                        }
                        Text(
                            text = buildString {
                                append(stop.stopName ?: "Stop ${stop.id}")
                                if (label != null) append(" ($label)")
                            },
                            color = if (label != null) TextPrimary else TextSecondary,
                            fontWeight = if (label != null) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            PrimaryButton(
                text = "Confirm Journey & Pay",
                onClick = {
                    if (selectedDestination?.id == null || fare == null) {
                        Toast.makeText(context, "Select a destination first.", Toast.LENGTH_LONG).show()
                        return@PrimaryButton
                    }
                    PassengerTripFlowStore.selectedDestinationStop = selectedDestination
                    PassengerTripFlowStore.farePreview = fare
                    navController.navigate(Screen.Payment.route)
                }
            )
        }
    }
}
