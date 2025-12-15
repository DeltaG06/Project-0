package com.gaurav.queon

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gaurav.queon.ui.MainViewModel
import com.gaurav.queon.ui.screens.ExamScreen
import com.gaurav.queon.ui.screens.HomeScreen
import com.gaurav.queon.ui.screens.ScannerScreen
import com.gaurav.queon.utils.KioskModeManager
import com.gaurav.queon.ui.theme.QueonStudentTheme
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

class MainActivity : ComponentActivity() {
    
    var isExamActive: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            QueonStudentTheme {
                QueonApp()
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (!hasFocus && isExamActive) {
            // WARNING: App lost focus (minimize, notification shade, etc.)
            // PHASE 3 TODO: Log this as a cheating flag
            Toast.makeText(this, "⚠️ WARNING: Do not leave the exam screen!", Toast.LENGTH_LONG).show()
        }
    }
}


@Composable
fun QueonApp(
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val activity = context as? Activity
    @Suppress("DEPRECATION")
    val lifecycleOwner = LocalLifecycleOwner.current

    // --- CHEATING DETECTION (Lifecycle) ---
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                // App is pausing (minimized, screen off, or another app took over)
                // If exam is active, this is a violation.
                if (uiState.activeExam != null) {
                    viewModel.reportFocusLoss()
                    Toast.makeText(context, "⚠️ WARNING: Exam focus lost! Incident reported.", Toast.LENGTH_LONG).show()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // --- SECURITY ENFORCEMENT ---
    // React to activeExam changes to enable/disable security mode
    DisposableEffect(uiState.activeExam) {
        val isActive = uiState.activeExam != null
        
        if (isActive && activity != null) {
            // Update Activity Flag
            (activity as? MainActivity)?.isExamActive = true

            // 1. Start Lock Task using KioskModeManager
            val kioskSuccess = KioskModeManager.startKioskMode(activity)
            val kioskStatus = KioskModeManager.getKioskModeStatus(activity)
            
            if (!kioskSuccess) {
                Toast.makeText(
                    activity,
                    "For full security, please enable Device Admin in Settings\nCurrent: $kioskStatus",
                    Toast.LENGTH_LONG
                ).show()
                // Report that we are running in insecure mode
                viewModel.reportKioskBypassAttempt("KIOSK_START_FAILED_OR_UNAVAILABLE")
            }

            // 2. Hide System Bars (Immersive Sticky Mode - Stronger)
            val window = activity.window
            val decorView = window.decorView
            
            // Also use WindowInsetsController for modern approach
            val insetsController = WindowCompat.getInsetsController(window, decorView)
            insetsController.systemBarsBehavior = 
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
            
            // 3. Keep Screen On
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            
            // 4. Prevent screenshots (optional security)
            window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)

        } else if (activity != null) {
            // Update Activity Flag
            (activity as? MainActivity)?.isExamActive = false

            // EXITING EXAM MODE
            KioskModeManager.stopKioskMode(activity)
            
            // Restore System Bars
            val window = activity.window
            val decorView = window.decorView
            
            val insetsController = WindowCompat.getInsetsController(window, decorView)
            insetsController.show(WindowInsetsCompat.Type.systemBars())
            
            // Clear flags
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
        }

        onDispose {
            // Cleanup on app close/dispose if needed
        }
    }

    // Show Error Toast if any
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    when {
        // 1) Active exam → show strict exam screen
        uiState.activeExam != null && uiState.scanMode == null -> {
            ExamScreen(
                exam = uiState.activeExam!!,
                statusMessage = uiState.statusMessage,
                onRequestExit = {
                    viewModel.startExitScan()
                }
            )
        }

        // 2) Scanner visible (ENTRY or EXIT)
        uiState.scanMode != null -> {
            ScannerScreen(
                mode = uiState.scanMode!!.lowercase(),
                onResult = { rawQr ->
                    viewModel.onQrScanned(rawQr)
                },
                onCancel = {
                    viewModel.cancelScan()
                }
            )
        }

        // 3) Home screen
        else -> {
            HomeScreen(
                status = uiState.statusMessage,
                onStartExam = { viewModel.startEntryScan() },
                onEndExam = { viewModel.startExitScan() },
                hasActiveExam = uiState.activeExam != null
            )
        }
    }
}
