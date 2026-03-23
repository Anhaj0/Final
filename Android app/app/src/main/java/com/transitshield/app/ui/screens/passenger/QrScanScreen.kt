package com.transitshield.app.ui.screens.passenger

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.AppTopBar
import com.transitshield.app.ui.components.PrimaryButton
import com.transitshield.app.ui.theme.BgCard
import com.transitshield.app.ui.theme.BgDeep
import com.transitshield.app.ui.theme.BlueElectric
import com.transitshield.app.ui.theme.BorderSubtle
import com.transitshield.app.ui.theme.OrangeWarning
import com.transitshield.app.ui.theme.TextPrimary
import com.transitshield.app.ui.theme.TextSecondary

@Composable
fun QrScanScreen(navController: NavController) {
    val infiniteTransition = rememberInfiniteTransition(label = "scan")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scanLine"
    )
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )

    var infoMessage by remember { mutableStateOf<String?>(null) }

    val scannerLauncher = rememberLauncherForActivityResult(
        contract = ScanContract()
    ) { result: ScanIntentResult ->
        val scannedValue = result.contents
        if (!scannedValue.isNullOrBlank()) {
            infoMessage = "QR scanned successfully"
            navController.navigate(Screen.TripDetails.route)
        } else {
            infoMessage = "No QR code detected. Please try again."
        }
    }

    Scaffold(
        topBar = { AppTopBar(title = "Scan QR Code", onBack = { navController.popBackStack() }) },
        containerColor = BgDeep
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Point camera at the QR code inside the bus",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(BgCard)
                    .border(2.dp, BlueElectric.copy(alpha = borderAlpha), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.TopCenter
            ) {
                ScannerCorners()

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, BlueElectric, Color.Transparent)
                            )
                        )
                        .offset(y = (260 * scanLineY).dp)
                )

                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = BlueElectric.copy(alpha = 0.12f),
                    modifier = Modifier
                        .size(160.dp)
                        .align(Alignment.Center)
                )
            }

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BgCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("The QR code inside the bus identifies:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    listOf("🚌 Bus ID & vehicle info", "🗺 Route & direction", "👤 Current driver", "📍 All route stops").forEach { item ->
                        Text(text = item, color = TextPrimary, fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
                    }
                }
            }

            infoMessage?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    color = OrangeWarning,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            PrimaryButton(
                text = "Scan Bus QR",
                onClick = {
                    val options = ScanOptions().apply {
                        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        setPrompt("Scan bus QR code")
                        setBeepEnabled(true)
                        setOrientationLocked(false)
                    }
                    scannerLauncher.launch(options)
                }
            )

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancel", color = TextSecondary)
            }
        }
    }
}

@Composable
private fun ScannerCorners() {
    val cornerSize = 24.dp
    val cornerThickness = 3.dp
    val cornerColor = BlueElectric

    Box(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Box(modifier = Modifier.size(cornerThickness, cornerSize).background(cornerColor))
            Box(modifier = Modifier.size(cornerSize - cornerThickness, cornerThickness).background(cornerColor))
        }

        Row(modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
            Box(modifier = Modifier.size(cornerSize - cornerThickness, cornerThickness).background(cornerColor))
            Box(modifier = Modifier.size(cornerThickness, cornerSize).background(cornerColor))
        }

        Column(modifier = Modifier.align(Alignment.BottomStart).padding(12.dp)) {
            Box(modifier = Modifier.size(cornerSize, cornerThickness).background(cornerColor))
            Box(modifier = Modifier.size(cornerThickness, cornerSize - cornerThickness).background(cornerColor))
        }

        Column(modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp), horizontalAlignment = Alignment.End) {
            Box(modifier = Modifier.size(cornerSize, cornerThickness).background(cornerColor))
            Box(modifier = Modifier.size(cornerThickness, cornerSize - cornerThickness).background(cornerColor))
        }
    }
}
