package com.transitshield.app.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*

@Composable
fun DigitalReceiptScreen(navController: NavController) {

    Scaffold(
        topBar = { AppTopBar(title = "Digital Receipt", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Payment Success Badge
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(GreenSuccess.copy(alpha = 0.15f))
                        .border(2.dp, GreenSuccess, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenSuccess, modifier = Modifier.size(40.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("Payment Successful", color = GreenSuccess, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("Your ticket has been issued", color = TextSecondary, fontSize = 13.sp)
            }

            Spacer(Modifier.height(20.dp))

            // Ticket Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column {
                    // Gradient Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(70.dp)
                            .background(Brush.horizontalGradient(listOf(WalletGrad1, WalletGrad2)))
                            .padding(horizontal = 20.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("TransitShield", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                Text("Route 138", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                            StatusBadge("PAID")
                        }
                    }

                    // Ticket Details
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow("Trip Reference", "REF-8A2F1C")
                        InfoRow("Bus ID", "NC-3421")
                        InfoRow("Route Name", "Pettah to Maharagama")
                        InfoRow("Driver", "Sunil Perera")
                        InfoRow("Boarding Stop", "Narahenpita")
                        InfoRow("Destination", "Nugegoda")
                        InfoRow("Fare Paid", "LKR 45.00")
                        InfoRow("Payment Status", "Success")
                        InfoRow("Date & Time", "Today, 08:32 AM")
                    }

                    // QR Placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgElevated),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCode2, contentDescription = null, tint = BlueElectric, modifier = Modifier.size(60.dp))
                            Spacer(Modifier.width(16.dp))
                            Column {
                                Text("Show to Conductor", color = TextSecondary, fontSize = 12.sp)
                                Text("REF-8A2F1C", color = BlueElectric, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            TextButton(
                onClick = {
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home", color = TextSecondary)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
