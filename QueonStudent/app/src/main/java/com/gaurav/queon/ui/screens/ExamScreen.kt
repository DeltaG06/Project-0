package com.gaurav.queon.ui.screens

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale
import java.util.concurrent.TimeUnit
import com.gaurav.queon.data.model.ActiveExam
import com.gaurav.queon.ui.theme.*

@Composable
fun ExamScreen(
    exam: ActiveExam,
    statusMessage: String,
    onRequestExit: () -> Unit
) {
    // Timer State
    var timeRemainingSeconds by remember {
        mutableLongStateOf(exam.durationMinutes * 60L)
    }

    LaunchedEffect(key1 = exam) {
        // Calculate remaining relative to start time to be robust
        while (timeRemainingSeconds > 0) {
            val elapsed = (System.currentTimeMillis() - exam.startedAtMillis) / 1000
            val remaining = (exam.durationMinutes * 60) - elapsed
            timeRemainingSeconds = if (remaining > 0) remaining else 0
            delay(1000)
        }
    }

    val hours = TimeUnit.SECONDS.toHours(timeRemainingSeconds)
    val minutes = TimeUnit.SECONDS.toMinutes(timeRemainingSeconds) % 60
    val seconds = timeRemainingSeconds % 60
    val timeString = String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
    
    // Animation: Slow "Breathing" Border for Focus
    val infiniteTransition = rememberInfiniteTransition(label = "focus_breath")
    val breathColor by infiniteTransition.animateColor(
        initialValue = DeepSpaceBlue,
        targetValue = ElectricBlue.copy(alpha = 0.3f),
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath_color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceBlack)
            .border(4.dp, breathColor) // Breathing border
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Secure",
                    tint = FocusGreen,
                    modifier = Modifier.size(16.dp).padding(end = 8.dp)
                )
                Text(
                    text = "SECURE EXAM MODE",
                    style = MaterialTheme.typography.labelMedium,
                    color = FocusGreen,
                    letterSpacing = 2.sp
                )
            }

            // Main Content: Timer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = exam.examName.uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 32.dp)
                )

                // Timer Container
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(280.dp)
                        .background(SurfaceGlass.copy(alpha = 0.3f), CircleShape)
                        .border(2.dp, Brush.verticalGradient(listOf(ElectricBlue, ElectricPurple)), CircleShape)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 56.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "REMAINING",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextTertiary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }

            // Footer: Status & Exit
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status Pill
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceGlass),
                    shape = RoundedCornerShape(100.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }

                // Explicit Exit Button
                Button(
                    onClick = onRequestExit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DangerRed.copy(alpha = 0.1f),
                        contentColor = DangerRed
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("SUBMIT & EXIT EXAM", fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- UTILITIES TRAY ---
        var showTools by remember { mutableStateOf(false) }

        Box(modifier = Modifier.fillMaxSize()) {
            // Floating Action Button for Tools
            FloatingActionButton(
                onClick = { showTools = true },
                containerColor = ElectricBlue,
                contentColor = DeepSpaceBlack,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 100.dp, end = 24.dp) // Above exit button/footer
            ) {
                Icon(Icons.Default.Build, "Tools")
            }
        }

        if (showTools) {
            ExamToolsSheet(onDismiss = { showTools = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamToolsSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceGlass,
        contentColor = TextPrimary
    ) {
        var selectedTool by remember { mutableStateOf("CALC") } // CALC or NOTES

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp) // Fixed height for consistency
                .padding(16.dp)
        ) {
            // Tool Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ToolTab("Calculator", selectedTool == "CALC") { selectedTool = "CALC" }
                ToolTab("Scratchpad", selectedTool == "NOTES") { selectedTool = "NOTES" }
            }

            HorizontalDivider(color = TextTertiary.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(16.dp))

            when (selectedTool) {
                "CALC" -> BasicCalculator()
                "NOTES" -> Scratchpad()
            }
        }
    }
}

@Composable
fun ToolTab(text: String, selected: Boolean, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        colors = ButtonDefaults.textButtonColors(
            contentColor = if (selected) ElectricBlue else TextTertiary
        )
    ) {
        Text(
            text, 
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.titleMedium
        )
    }
}

@Composable
fun BasicCalculator() {
    var display by remember { mutableStateOf("") }
    val buttons = listOf(
        listOf("7", "8", "9", "/"),
        listOf("4", "5", "6", "*"),
        listOf("1", "2", "3", "-"),
        listOf("C", "0", "=", "+")
    )

    Column(
        modifier = Modifier.fillMaxWidth().background(DeepSpaceBlack.copy(alpha=0.5f), RoundedCornerShape(16.dp)).padding(16.dp),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = display.ifEmpty { "0" },
            style = MaterialTheme.typography.displaySmall,
            color = NeonCyan,
            modifier = Modifier.padding(bottom = 16.dp),
            maxLines = 1
        )
        
        buttons.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { label ->
                    Button(
                        onClick = {
                            when (label) {
                                "C" -> display = ""
                                "=" -> {
                                    display = try {
                                        // Shim for "interactive" feel
                                        "Not Implemented"
                                    } catch(_: Exception) {
                                        "Err"
                                    }
                                }
                                else -> display += label
                            }
                        },
                        modifier = Modifier.weight(1f).aspectRatio(1.5f),
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceGlass)
                    ) {
                        Text(label, fontSize = 20.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun Scratchpad() {
    var text by remember { mutableStateOf("") }
    // Using a side effect to "use" the text value if needed, but TextField uses it.
    // The warning "Assigned value is never read" usually refers to the result of `text = it`.
    // But `text = it` is a Unit.
    // Wait, the warning might be on `text = it` inside lambda?
    // "Assigned value is never read" usually means I assigned something to a variable but didn't use the result.
    // Here `text` is a var delegate.
    // Maybe the warning is because `text` is only read by `value = text`.
    // It is read.
    // Ah, maybe the warning is about the lambda result? No.
    // "Assigned value is never read" -> maybe `var text`?
    // If I change to `var text by remember { mutableStateOf("") }`, and I read it in `value = text`.
    // It should be fine.
    
    // Sometimes IDE flags `text = it` if `text` is local var and not used *after* assignment in that scope?
    // But this is a state delegate.
    
    // I will try to suppress it if it persists or ignore as false positive, but I'll check if I can fix it.
    // Actually, maybe I have a duplicate assignment somewhere? No.
    
    TextField(
        value = text,
        onValueChange = { newText -> text = newText }, // Explicit lambda param
        modifier = Modifier.fillMaxSize().background(DeepSpaceBlack, RoundedCornerShape(8.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = DeepSpaceBlack,
            unfocusedContainerColor = DeepSpaceBlack,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        placeholder = { Text("Type notes here...", color = TextTertiary) }
    )
}
