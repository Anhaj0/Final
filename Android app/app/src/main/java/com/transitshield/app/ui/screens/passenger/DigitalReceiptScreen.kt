package com.transitshield.app.ui.screens.passenger

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.PassengerTripFlowStore
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.InfoRow
import com.transitshield.app.ui.components.PrimaryButton
import com.transitshield.app.ui.components.SecondaryButton
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.GreenSuccess
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary

@Composable
fun DigitalReceiptScreen(navController: NavController) {
    var trip by remember { mutableStateOf(PassengerTripFlowStore.activeTrip) }

    LaunchedEffect(Unit) {
        val passengerId = PassengerTripFlowStore.passengerId ?: return@LaunchedEffect
        try {
            trip = RetrofitClient.apiService.getActiveTrip(passengerId)
            PassengerTripFlowStore.activeTrip = trip
        } catch (_: Exception) {
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Digital Receipt", onBack = { navController.popBackStack() }) },
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
            if (trip == null) {
                Text("No receipt data available.", color = TextPrimary)
                return@Column
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Payment Successful", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Your digital ticket is ready", color = TextSecondary)
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    InfoRow("Trip Reference", trip.tripRef ?: "-")
                    InfoRow("Bus", trip.busDisplayName ?: trip.busId?.toString() ?: "-")
                    InfoRow("Route", trip.routeName ?: "Route Variant ${trip.routeVariantId ?: "-"}")
                    InfoRow("Driver", trip.driverName ?: "-")
                    InfoRow("Boarding Stop", trip.boardingStopName ?: "-")
                    InfoRow("Destination", trip.selectedDestinationStopName ?: "-")
                    InfoRow("Fare Paid", trip.totalFareLkr?.let { "LKR ${"%.2f".format(it)}" } ?: "-")
                    InfoRow("Payment Status", trip.paymentStatus ?: "-")
                    InfoRow("Ticket Verified", if (trip.ticketVerified == true) "Yes" else "No")
                    InfoRow("Verified At", trip.ticketVerifiedAt ?: "-")
                    InfoRow("Issued At", trip.createdAt ?: "-")
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = BlueElectric)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(trip.tripRef ?: "-", color = BlueElectric, fontWeight = FontWeight.Bold)
                    Text("Ticket QR value for verification", color = TextSecondary, fontSize = 12.sp)
                    Text("Scan or enter this code on conductor screen", color = TextSecondary, fontSize = 12.sp)
                }
            }

            PrimaryButton(
                text = "View Active Trip",
                onClick = { navController.navigate(Screen.ActiveTrip.route) }
            )

            SecondaryButton(
                text = "Back to Home",
                onClick = {
                    navController.navigate(Screen.PassengerHome.route) {
                        popUpTo(Screen.PassengerHome.route) { inclusive = false }
                    }
                }
            )
        }
    }
}
