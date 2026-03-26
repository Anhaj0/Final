package com.transitshield.app.ui.screens.passenger

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import com.transitshield.app.data.network.dto.PassengerTripDto
import com.transitshield.app.data.network.dto.PassengerTripFlowStore
import com.transitshield.app.data.network.dto.TripEndRequest
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.InfoRow
import com.transitshield.app.ui.components.SectionHeader
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun ActiveTripScreen(navController: NavController) {
    val context = LocalContext.current
    var trip by remember { mutableStateOf<PassengerTripDto?>(PassengerTripFlowStore.activeTrip) }
    var completedTrip by remember { mutableStateOf<PassengerTripDto?>(PassengerTripFlowStore.lastCompletedTrip) }
    var balance by remember { mutableStateOf(PassengerTripFlowStore.passengerBalance) }
    var passengerId by remember { mutableStateOf(PassengerTripFlowStore.passengerId) }
    var loading by remember { mutableStateOf(trip == null) }
    var completing by remember { mutableStateOf(false) }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    LaunchedEffect(Unit) {
        try {
            val me = RetrofitClient.apiService.getMe()
            val resolvedPassengerId = me.id ?: PassengerTripFlowStore.passengerId
            passengerId = resolvedPassengerId
            PassengerTripFlowStore.passengerId = resolvedPassengerId
            if (resolvedPassengerId != null) {
                balance = RetrofitClient.apiService.getPassengerBalance(resolvedPassengerId)
                PassengerTripFlowStore.passengerBalance = balance
                if (trip == null) {
                    try {
                        trip = RetrofitClient.apiService.getActiveTrip(resolvedPassengerId)
                        PassengerTripFlowStore.activeTrip = trip
                        completedTrip = null
                        PassengerTripFlowStore.lastCompletedTrip = null
                    } catch (e: HttpException) {
                        if (e.code() != 404) throw e
                        trip = null
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to load active trip: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            loading = false
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Active Trip", onBack = { navController.popBackStack() }) },
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
            if (loading) {
                CircularProgressIndicator()
                return@Column
            }

            if (trip == null) {
                if (completedTrip != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = BgCard),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            SectionHeader("Trip Completed")
                            Text(
                                completedTrip?.routeName ?: "Completed Trip",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                            InfoRow("Trip Reference", completedTrip?.tripRef ?: "-")
                            InfoRow("Destination", completedTrip?.selectedDestinationStopName ?: "-")
                            InfoRow("Completed At", completedTrip?.endedAt ?: "-")
                            InfoRow(
                                "Remaining Wallet",
                                balance?.walletBalance?.let { "LKR ${"%.2f".format(it)}" } ?: "-"
                            )
                            InfoRow("Reward Points", balance?.totalPoints?.toString() ?: "-")
                            Button(
                                onClick = { navController.navigate(Screen.Rewards.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View Rewards")
                            }
                            OutlinedButton(
                                onClick = { navController.navigate(Screen.RecentTrips.route) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View Trip History")
                            }
                        }
                    }
                } else {
                    Text("No active trip found.", color = TextPrimary)
                }
                return@Column
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Trip Information")
                    Text(trip?.routeName ?: "Current Route", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    InfoRow("Trip Reference", trip?.tripRef ?: "-")
                    InfoRow("Bus", trip?.busDisplayName ?: trip?.busId?.toString() ?: "-")
                    InfoRow("Driver", trip?.driverName ?: "-")
                    InfoRow("Boarding Stop", trip?.boardingStopName ?: "-")
                    InfoRow("Destination", trip?.selectedDestinationStopName ?: "-")
                    InfoRow("Fare Paid", trip?.totalFareLkr?.let { "LKR ${"%.2f".format(it)}" } ?: "-")
                    InfoRow(
                        "Remaining Wallet",
                        balance?.walletBalance?.let { "LKR ${"%.2f".format(it)}" }
                            ?: trip?.walletBalanceAfterPayment?.let { "LKR ${"%.2f".format(it)}" }
                            ?: "-"
                    )
                    InfoRow("Reward Points", balance?.totalPoints?.toString() ?: "-")
                    InfoRow("Payment Status", trip?.paymentStatus ?: "-")
                    InfoRow("Trip Status", trip?.tripStatus ?: "-")
                    Button(
                        onClick = { navController.navigate(Screen.LiveTracker.route) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open Live Tracker")
                    }
                    Button(
                        onClick = {
                            val currentTrip = trip ?: return@Button
                            val currentPassengerId = passengerId
                                ?: PassengerTripFlowStore.passengerId
                                ?: currentTrip.passengerProfileId
                            coroutineScope.launch {
                                completing = true
                                try {
                                    val endedTrip = RetrofitClient.apiService.endTrip(
                                        TripEndRequest(
                                            tripId = currentTrip.id ?: return@launch,
                                            actualExitStopId = currentTrip.selectedDestinationStopId
                                        )
                                    )
                                    completedTrip = endedTrip
                                    trip = null

                                    val updatedBalance = currentPassengerId?.let {
                                        RetrofitClient.apiService.getPassengerBalance(it)
                                    }
                                    balance = updatedBalance
                                    PassengerTripFlowStore.markTripCompleted(endedTrip, updatedBalance)

                                    Toast.makeText(context, "Trip completed successfully", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Failed to complete trip: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    completing = false
                                }
                            }
                        },
                        enabled = !completing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (completing) "Completing Trip..." else "Complete Trip")
                    }
                }
            }
        }
    }
}
