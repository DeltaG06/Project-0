package com.gaurav.queon.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gaurav.queon.ui.theme.*

@Composable
fun HomeScreen(
    status: String,
    onStartExam: () -> Unit,
    onEndExam: () -> Unit,
    hasActiveExam: Boolean
) {
    // Background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(modifier = Modifier.height(40.dp))
                // Logo placeholder
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(ElectricBlue, ElectricPurple)
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Q",
                        style = MaterialTheme.typography.displayMedium,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "QUEON",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    letterSpacing = 4.sp
                )
                Text(
                    text = "STUDENT PORTAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextTertiary,
                    letterSpacing = 2.sp
                )
            }

            // Action Buttons
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                // Start Exam Button
                HomeActionButton(
                    text = "START EXAM",
                    subtext = "Scan Entry QR",
                    // Use a standard icon available in default set
                    icon = Icons.Default.Add, 
                    gradient = Brush.horizontalGradient(listOf(ElectricBlue, ElectricPurple)),
                    onClick = onStartExam
                )

                // End Exam Button (only enabled if active)
                if (hasActiveExam) {
                    HomeActionButton(
                        text = "FINISH EXAM",
                        subtext = "Scan Exit QR",
                        icon = Icons.Default.CheckCircle,
                        gradient = Brush.horizontalGradient(listOf(DangerRed, Color(0xFFFF4444))),
                        onClick = onEndExam
                    )
                }
            }

            // Status Footer
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceGlass),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = status,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
fun HomeActionButton(
    text: String,
    subtext: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .background(gradient, RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(1.dp), // slight padding for border
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            Column {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = subtext,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextPrimary.copy(alpha = 0.7f)
                )
            }
            
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    .scale(glowAlpha), // Use standard scale modifier
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
