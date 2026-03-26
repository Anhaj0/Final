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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.PassengerTripFlowStore
import com.transitshield.app.data.network.dto.TripStartRequest
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.InfoRow
import com.transitshield.app.ui.components.PrimaryButton
import com.transitshield.app.ui.components.SecondaryButton
import com.transitshield.app.ui.components.SectionHeader
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun PaymentScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scanResponse = PassengerTripFlowStore.qrScanResponse
    val selectedDestination = PassengerTripFlowStore.selectedDestinationStop
    val passengerId = PassengerTripFlowStore.passengerId
    val fare = PassengerTripFlowStore.farePreview

    var currentBalance by remember { mutableStateOf(PassengerTripFlowStore.passengerBalance?.walletBalance) }
    var isPaying by remember { mutableStateOf(false) }

    LaunchedEffect(passengerId) {
        if (passengerId == null) {
            Toast.makeText(context, "Passenger session missing.", Toast.LENGTH_LONG).show()
            navController.popBackStack()
            return@LaunchedEffect
        }

        try {
            val balanceDto = RetrofitClient.apiService.getPassengerBalance(passengerId)
            PassengerTripFlowStore.passengerBalance = balanceDto
            currentBalance = balanceDto.walletBalance
        } catch (_: Exception) {
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Payment", onBack = { navController.popBackStack() }) },
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
            if (scanResponse == null || selectedDestination == null || fare == null || passengerId == null) {
                Text("Trip details are incomplete. Please scan again.", color = TextPrimary)
                return@Column
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("Fare Breakdown")
                    InfoRow("Bus", scanResponse.busDisplayName ?: "-")
                    InfoRow("Boarding", scanResponse.orderedStops?.firstOrNull { it.id == scanResponse.nearestBoardingStopId }?.stopName ?: "-")
                    InfoRow("Destination", selectedDestination.stopName ?: "-")
                    InfoRow("Total Payable", "LKR ${"%.2f".format(fare)}")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    SectionHeader("Payment Method")
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = BlueElectric)
                    Text("TransitShield Wallet", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Text(
                        "Available Balance: LKR ${"%.2f".format(currentBalance ?: 0.0)}",
                        color = TextSecondary
                    )
                }
            }

            if (isPaying) {
                CircularProgressIndicator(color = BlueElectric)
            }

            PrimaryButton(
                text = "Confirm Payment - LKR ${"%.2f".format(fare)}",
                onClick = {
                    val busAssignmentId = scanResponse.busAssignmentId
                    val boardingStopId = scanResponse.nearestBoardingStopId
                    val destinationStopId = selectedDestination.id
                    val qrToken = PassengerTripFlowStore.qrToken

                    if (busAssignmentId == null || boardingStopId == null || destinationStopId == null || qrToken.isNullOrBlank()) {
                        Toast.makeText(context, "Payment data is incomplete.", Toast.LENGTH_LONG).show()
                        return@PrimaryButton
                    }

                    isPaying = true
                    scope.launch {
                        try {
                            val trip = RetrofitClient.apiService.startTrip(
                                TripStartRequest(
                                    passengerProfileId = passengerId,
                                    busAssignmentId = busAssignmentId,
                                    boardingStopId = boardingStopId,
                                    selectedDestinationStopId = destinationStopId,
                                    qrTokenUsed = qrToken
                                )
                            )
                            PassengerTripFlowStore.activeTrip = trip
                            PassengerTripFlowStore.passengerBalance = PassengerTripFlowStore.passengerBalance?.copy(
                                walletBalance = trip.walletBalanceAfterPayment
                            )
                            navController.navigate(Screen.DigitalReceipt.route)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Payment failed: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isPaying = false
                        }
                    }
                }
            )

            SecondaryButton(
                text = "Cancel",
                onClick = { navController.popBackStack() }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
