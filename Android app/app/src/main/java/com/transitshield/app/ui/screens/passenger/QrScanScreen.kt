package com.transitshield.app.ui.screens.passenger

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.PassengerTripFlowStore
import com.transitshield.app.data.network.dto.QrScanRequest
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.theme.*
import kotlinx.coroutines.launch

@Composable
fun QrScanScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }

    val scannerLauncher = rememberLauncherForActivityResult(ScanContract()) { result ->
        val scannedToken = result.contents
        if (scannedToken.isNullOrBlank()) {
            Toast.makeText(context, "Scan cancelled", Toast.LENGTH_SHORT).show()
            isProcessing = false
            return@rememberLauncherForActivityResult
        }

        scope.launch {
            try {
                // 1. Fetch current user ID (Fallback to 1 if backend profile route acts up)
                val me = try {
                    RetrofitClient.apiService.getMe()
                } catch (e: Exception) {
                    null
                }
                val passengerId = me?.id ?: 1L // Ensure we send a valid Long

                // 2. Build exact payload backend expects, trimming hidden whitespace
                val request = QrScanRequest(
                    passengerId = passengerId,
                    qrToken = scannedToken.trim(),
                    latitude = null,
                    longitude = null
                )

                android.util.Log.e("TRANSIT_DEBUG", "🚀 Sending QR Scan for Passenger $passengerId with token: ${scannedToken.trim()}")

                // 3. Fire the POST request
                val response = RetrofitClient.apiService.scanQr(request)
                val orderedStops = response.orderedStops.orEmpty()
                val selectedBoardingStop = orderedStops.firstOrNull { it.id == response.nearestBoardingStopId }
                    ?: orderedStops.firstOrNull()

                android.util.Log.d(
                    "TRANSIT_DEBUG",
                    "QR handoff -> orderedStops=${orderedStops.size}, nearestBoardingStopId=${response.nearestBoardingStopId}, selectedBoardingStopId=${selectedBoardingStop?.id}"
                )

                if (orderedStops.isEmpty()) {
                    Toast.makeText(
                        context,
                        response.message ?: "No boarding stops are configured for this QR.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@launch
                }

                Toast.makeText(
                    context,
                    "Stops: ${orderedStops.size}, suggested boarding: ${response.nearestBoardingStopId ?: "first stop fallback"}",
                    Toast.LENGTH_SHORT
                ).show()
                PassengerTripFlowStore.resetForNewScan()
                PassengerTripFlowStore.passengerId = passengerId
                PassengerTripFlowStore.qrToken = scannedToken.trim()
                PassengerTripFlowStore.qrScanResponse = response
                PassengerTripFlowStore.selectedBoardingStop = selectedBoardingStop
                navController.navigate(Screen.TripDetails.route) {
                    popUpTo(Screen.PassengerHome.route)
                }

            } catch (e: Exception) {
                android.util.Log.e("TRANSIT_DEBUG", "❌ QR Scan Failed: ${e.message}")
                Toast.makeText(context, "Backend Error: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isProcessing = false
            }
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Scan to Board", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = BlueElectric.copy(alpha = 0.1f)),
                modifier = Modifier.size(200.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scanner",
                        modifier = Modifier.size(100.dp),
                        tint = BlueElectric
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                "Point your camera at the Driver's dashboard to board the bus.",
                color = TextSecondary,
                fontSize = 16.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    isProcessing = true
                    val options = ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt("Scan Bus QR Code")
                        setBeepEnabled(true)
                        setOrientationLocked(false)
                    }
                    scannerLauncher.launch(options)
                },
                enabled = !isProcessing,
                colors = ButtonDefaults.buttonColors(containerColor = BlueElectric),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isProcessing) "Verifying..." else "Open Camera",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
