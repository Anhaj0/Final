package com.transitshield.app.ui.screens.passenger

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.PassengerTaskCapacityRequest
import com.transitshield.app.data.network.dto.PassengerTaskLocationRequest
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun PassengerTasksScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { AppTopBar(title = "Passenger Tasks", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TaskCard(
                title = "Update Passenger Count (+2 Pts)",
                description = "Submit current headcount for your bus.",
                icon = { Icon(Icons.Default.Groups, contentDescription = null, tint = BlueElectric) },
                onClick = {
                    scope.launch {
                        runCatching {
                            RetrofitClient.apiService.submitTaskCapacity(
                                PassengerTaskCapacityRequest(
                                    busId = 1L,
                                    passengerCount = 34
                                )
                            )
                        }.onSuccess {
                            Toast.makeText(context, it.message ?: "Capacity submitted", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(context, it.message ?: "Failed to submit capacity", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )

            TaskCard(
                title = "Share Live Location (+5 Pts)",
                description = "Share your current bus location.",
                icon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = BlueElectric) },
                onClick = {
                    scope.launch {
                        runCatching {
                            RetrofitClient.apiService.submitTaskLocation(
                                PassengerTaskLocationRequest(
                                    busId = 1L,
                                    latitude = 6.9271,
                                    longitude = 79.8612
                                )
                            )
                        }.onSuccess {
                            Toast.makeText(context, it.message ?: "Location shared", Toast.LENGTH_SHORT).show()
                        }.onFailure {
                            Toast.makeText(context, it.message ?: "Failed to share location", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun TaskCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgCard),
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            icon()
            Spacer(Modifier.height(10.dp))
            Text(title, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Spacer(Modifier.height(4.dp))
            Text(description, color = TextSecondary, fontSize = 12.sp)
            Spacer(Modifier.height(8.dp))
            Text("Tap to submit", color = BlueElectric, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}
