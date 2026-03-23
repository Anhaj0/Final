package com.transitshield.app.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.*
import com.transitshield.app.ui.theme.*

@Composable
fun PaymentScreen(navController: NavController) {
    var selectedMethod by remember { mutableStateOf("wallet") }
    val distanceStops = 2
    val baseFarePerStop = 22.5
    val serviceFee = 0.0
    val baseFare = baseFarePerStop * distanceStops
    val totalPayable = baseFare + serviceFee

    Scaffold(
        topBar = { AppTopBar(title = "Payment", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            // Fare Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionHeader("Fare Breakdown")
                    Spacer(Modifier.height(4.dp))
                    InfoRow("Route", "138 – Narahenpita → Nugegoda")
                    InfoRow("Distance", "$distanceStops stops")
                    InfoRow("Base Fare", "LKR %.2f".format(baseFare))
                    InfoRow("Service Fee", "LKR %.2f".format(serviceFee))
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BlueElectric.copy(alpha = 0.12f))
                            .padding(12.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total Payable", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("LKR %.2f".format(totalPayable), color = BlueElectric, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            SectionHeader("Payment Method")
            Spacer(Modifier.height(10.dp))

            // Wallet Option
            PaymentMethodCard(
                icon = Icons.Default.AccountBalanceWallet,
                title = "TransitShield Wallet",
                subtitle = "Balance: LKR 1,245.50",
                selected = selectedMethod == "wallet",
                onClick = { selectedMethod = "wallet" }
            )

            Spacer(Modifier.height(10.dp))

            PaymentMethodCard(
                icon = Icons.Default.CreditCard,
                title = "Debit / Credit Card",
                subtitle = "Visa, Mastercard, Amex",
                selected = selectedMethod == "card",
                onClick = { selectedMethod = "card" }
            )

            Spacer(Modifier.height(10.dp))

            PaymentMethodCard(
                icon = Icons.Default.PhoneAndroid,
                title = "Mobile Payment",
                subtitle = "Dialog Pay, FriMi, Sampath Vishwa",
                selected = selectedMethod == "mobile",
                onClick = { selectedMethod = "mobile" }
            )

            Spacer(Modifier.height(10.dp))

            // Top Up Notice
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeWarning.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangeWarning.copy(alpha = 0.3f))
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = OrangeWarning, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Need to top up? Visit Settings → Wallet → Top Up.", color = OrangeWarning, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                text = "Confirm Payment – LKR %.2f".format(totalPayable),
                onClick = { navController.navigate(Screen.DigitalReceipt.route) }
            )

            Spacer(Modifier.height(12.dp))

            SecondaryButton(
                text = "Cancel",
                onClick = { navController.popBackStack() }
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun PaymentMethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) BlueElectric.copy(alpha = 0.1f) else BgCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 1.5.dp else 1.dp,
            color = if (selected) BlueElectric else BorderSubtle
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) BlueElectric.copy(alpha = 0.2f) else BgElevated),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = if (selected) BlueElectric else TextMuted, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = TextSecondary, fontSize = 12.sp)
            }
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = BlueElectric, unselectedColor = TextMuted)
            )
        }
    }
}
