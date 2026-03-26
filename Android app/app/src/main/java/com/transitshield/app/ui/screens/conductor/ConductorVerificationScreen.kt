package com.transitshield.app.ui.screens.conductor

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.PassengerTripDto
import com.transitshield.app.data.network.dto.PassengerTripFlowStore
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.InfoRow
import com.transitshield.app.ui.components.PrimaryButton
import com.transitshield.app.ui.components.SecondaryButton
import com.transitshield.app.ui.components.SectionHeader
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.GreenSuccess
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun ConductorVerificationScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tripRef by remember { mutableStateOf(PassengerTripFlowStore.activeTrip?.tripRef ?: "") }
    var verifiedTrip by remember { mutableStateOf<PassengerTripDto?>(null) }
    var isVerifying by remember { mutableStateOf(false) }

    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val scannedValue = result.contents?.trim()
        if (!scannedValue.isNullOrBlank()) {
            tripRef = scannedValue
            scope.launch {
                isVerifying = true
                try {
                    val trip = RetrofitClient.apiService.verifyTicket(scannedValue)
                    verifiedTrip = trip
                    PassengerTripFlowStore.activeTrip = trip
                    Toast.makeText(context, "Ticket verified.", Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isVerifying = false
                }
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Ticket Verification", onBack = { navController.popBackStack() }) },
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
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeader("Verify Passenger Ticket")
                    OutlinedTextField(
                        value = tripRef,
                        onValueChange = { tripRef = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Ticket Code / Trip Reference") }
                    )
                    PrimaryButton(
                        text = if (isVerifying) "Verifying..." else "Verify Ticket",
                        onClick = {
                            if (tripRef.isBlank()) {
                                Toast.makeText(context, "Enter or scan a ticket code.", Toast.LENGTH_LONG).show()
                                return@PrimaryButton
                            }
                            scope.launch {
                                isVerifying = true
                                try {
                                    val trip = RetrofitClient.apiService.verifyTicket(tripRef.trim())
                                    verifiedTrip = trip
                                    PassengerTripFlowStore.activeTrip = trip
                                    Toast.makeText(context, "Ticket verified.", Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Verification failed: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isVerifying = false
                                }
                            }
                        }
                    )
                    SecondaryButton(
                        text = "Scan Ticket QR",
                        onClick = {
                            val options = ScanOptions().apply {
                                setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                setPrompt("Scan Passenger Ticket QR")
                                setBeepEnabled(true)
                                setOrientationLocked(false)
                            }
                            scannerLauncher.launch(options)
                        }
                    )
                }
            }

            verifiedTrip?.let { trip ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Ticket Verified", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        InfoRow("Trip Reference", trip.tripRef ?: "-")
                        InfoRow("Bus", trip.busDisplayName ?: trip.busId?.toString() ?: "-")
                        InfoRow("Route", trip.routeName ?: "-")
                        InfoRow("Boarding Stop", trip.boardingStopName ?: "-")
                        InfoRow("Destination", trip.selectedDestinationStopName ?: "-")
                        InfoRow("Fare Paid", trip.totalFareLkr?.let { "LKR ${"%.2f".format(it)}" } ?: "-")
                        InfoRow("Payment Status", trip.paymentStatus ?: "-")
                        InfoRow("Verified", if (trip.ticketVerified == true) "Yes" else "No")
                        InfoRow("Verified At", trip.ticketVerifiedAt ?: "-")
                    }
                }
            }

            if (verifiedTrip != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgCard),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Driver / Conductor Status", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("Passenger ticket is confirmed as paid and active.", color = TextSecondary)
                    }
                }
            }
        }
    }
}
