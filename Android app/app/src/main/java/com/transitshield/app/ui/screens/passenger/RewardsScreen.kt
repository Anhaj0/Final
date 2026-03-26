package com.transitshield.app.ui.screens.passenger

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.PassengerBalanceDto
import com.transitshield.app.data.network.dto.RewardTransactionDto
import com.transitshield.app.data.network.dto.TransferRequest
import com.transitshield.app.ui.theme.*
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.PrimaryButton
import kotlinx.coroutines.launch
import retrofit2.HttpException

@Composable
fun RewardsScreen(navController: NavController) {
    val coroutineScope = rememberCoroutineScope()
    
    var balance by remember { mutableStateOf<PassengerBalanceDto?>(null) }
    var publicId by remember { mutableStateOf("Loading...") }
    var transactions by remember { mutableStateOf<List<RewardTransactionDto>>(emptyList()) }
    
    var recipientId by remember { mutableStateOf("") }
    var transferAmount by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    fun refreshData() {
        coroutineScope.launch {
            try {
                val balanceRes = RetrofitClient.apiService.getMyBalanceSummary()
                balance = balanceRes
                publicId = balanceRes.publicUserId ?: "Unknown"
                transactions = RetrofitClient.apiService.getMyRewardHistory()
            } catch (e: Exception) {
                // Ignore for demo
            }
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
    }

    Scaffold(
        topBar = { AppTopBar(title = "Rewards & Transfers", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.horizontalGradient(listOf(WalletGrad1, WalletGrad2)))
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFCD34D), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Your Public ID: $publicId", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("${balance?.totalPoints ?: 0.0}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 40.sp)
                        Text("points available", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Remaining Wallet: LKR ${"%.2f".format(balance?.walletBalance ?: 0.0)}",
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BgCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Transfer Points", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = recipientId,
                            onValueChange = { recipientId = it },
                            label = { Text("Recipient Public ID") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BlueElectric,
                                focusedLabelColor = BlueElectric
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = transferAmount,
                            onValueChange = { transferAmount = it },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BlueElectric,
                                focusedLabelColor = BlueElectric
                            )
                        )
                        Spacer(Modifier.height(16.dp))
                        
                        if (message != null) {
                            Text(message!!, color = if (isError) RedError else GreenSuccess, fontSize = 14.sp)
                            Spacer(Modifier.height(8.dp))
                        }
                        
                        PrimaryButton(
                            text = "Transfer Now",
                            onClick = {
                                val amt = transferAmount.toDoubleOrNull() ?: 0.0
                                if (amt <= 0.0) {
                                    isError = true
                                    message = "Enter a valid amount"
                                    return@PrimaryButton
                                }
                                if (recipientId.isBlank()) {
                                    isError = true
                                    message = "Enter recipient ID"
                                    return@PrimaryButton
                                }
                                coroutineScope.launch {
                                    try {
                                        val result = RetrofitClient.apiService.transferPoints(TransferRequest(recipientId.trim(), amt))
                                        message = "Transfer Successful!"
                                        isError = false
                                        recipientId = ""
                                        transferAmount = ""
                                        if (balance != null) {
                                            balance = balance?.copy(totalPoints = (balance?.totalPoints ?: 0.0) - (result.points ?: 0.0))
                                        }
                                        refreshData()
                                    } catch (e: HttpException) {
                                        isError = true
                                        message = e.response()?.errorBody()?.string()?.ifBlank { null } ?: "Transfer failed. Check ID and balance."
                                    } catch (e: Exception) {
                                        isError = true
                                        message = "Transfer failed. Check ID and balance."
                                    }
                                }
                            }
                        )
                    }
                }
            }

            item {
                Text("Transaction History", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
            }

            if (transactions.isEmpty()) {
                item {
                    Text("No transactions yet.", color = TextSecondary, modifier = Modifier.padding(vertical = 20.dp))
                }
            } else {
                items(transactions) { tx ->
                    val isPos = tx.type == "EARNED" || tx.type == "TRANSFER_IN"
                    val sign = if (isPos) "+" else "-"
                    val color = if (isPos) GreenSuccess else RedError
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BgCard)
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(tx.type ?: "UNKNOWN", fontWeight = FontWeight.SemiBold, color = TextPrimary, fontSize = 14.sp)
                            Text(tx.description ?: "", color = TextSecondary, fontSize = 12.sp)
                            Text(tx.createdAt?.substringBefore("T") ?: "", color = TextMuted, fontSize = 11.sp)
                        }
                        Text("$sign${tx.points}", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}
