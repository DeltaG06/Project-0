package com.gaurav.queon.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import kotlinx.coroutines.delay
import com.gaurav.queon.ui.theme.*

@Composable
fun ScannerScreen(
    mode: String,               // "entry" or "exit"
    onResult: (String) -> Unit,
    onCancel: () -> Unit
) {
    // We handle the scanner launcher internally
    val scanQrCodeLauncher = rememberLauncherForActivityResult(ScanQRCode()) { result ->
        if (result is QRResult.QRSuccess) {
            val content = result.content.rawValue
            if (content != null) {
                onResult(content)
            }
        }
        // Handle other cases or ignore
    }

    // Auto-launch
    LaunchedEffect(Unit) {
        delay(800) 
        scanQrCodeLauncher.launch(null)
    }

    // Animation for laser
    val infiniteTransition = rememberInfiniteTransition(label = "laser_anim")
    val laserOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 300f, // roughly box size
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Instruction Card
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceGlass.copy(alpha=0.8f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(bottom = 32.dp)
            ) {
                Text(
                    text = if (mode == "entry") "SCAN TO START EXAM" else "SCAN TO SUBMIT & EXIT",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (mode == "entry") ElectricBlue else NeonCyan,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 24.dp),
                    letterSpacing = 1.sp
                )
            }

            // Simulated Scanner Box (Visual only, actual scanner opens on top)
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .border(2.dp, Brush.linearGradient(listOf(ElectricBlue, ElectricPurple)), RoundedCornerShape(16.dp))
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            ) {
                // Laser Line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = laserOffset.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color.Transparent, NeonCyan, Color.Transparent)
                            )
                        )
                )
                
                Text(
                    text = "Initializing Camera...",
                    color = TextTertiary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            
            Text(
                text = "Align QR code within the frame",
                color = TextSecondary,
                modifier = Modifier.padding(top = 32.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Cancel Button
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.1f),
                    contentColor = TextPrimary
                ),
                shape = RoundedCornerShape(50),
                border = androidx.compose.foundation.BorderStroke(1.dp, TextTertiary),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("CANCEL SCAN")
            }
        }
    }
}
