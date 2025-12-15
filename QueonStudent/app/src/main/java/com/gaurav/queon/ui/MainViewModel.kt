package com.gaurav.queon.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gaurav.queon.data.model.ActiveExam
import com.gaurav.queon.data.repository.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = false,
    val activeExam: ActiveExam? = null,
    val statusMessage: String = "Welcome to Queon",
    val error: String? = null,
    val scanMode: String? = null // "ENTRY" or "EXIT" or null
)

class MainViewModel : ViewModel() {
    // Ideally injected via Hilt/Koin
    private val repository = ExamRepository()

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    fun startEntryScan() {
        _uiState.update { it.copy(scanMode = "ENTRY", error = null) }
    }

    fun startExitScan() {
        _uiState.update { it.copy(scanMode = "EXIT", error = null) }
    }

    fun cancelScan() {
        _uiState.update { it.copy(scanMode = null) }
    }

    fun onQrScanned(rawQr: String) {
        val mode = _uiState.value.scanMode ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            if (mode == "ENTRY") {
                val result = repository.validateEntry(rawQr)
                result.fold(
                    onSuccess = { (msg, exam) ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                statusMessage = msg,
                                activeExam = exam ?: it.activeExam, // only update if not null (though logic says it's pair)
                                scanMode = null, // Close scanner on success
                                error = if(exam == null) "Entry Denied" else null
                            )
                        }
                    },
                    onFailure = { e ->
                         _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message,
                                scanMode = null // Close scanner on error? or keep open? 
                                // Let's close for now to show error on home/previous screen
                            )
                        }
                    }
                )
            } else { // EXIT
                val result = repository.validateExit(rawQr)
                result.fold(
                    onSuccess = { msg ->
                         val denied = msg.lowercase().contains("denied")
                         _uiState.update { 
                            it.copy(
                                isLoading = false,
                                statusMessage = msg,
                                activeExam = if (denied) it.activeExam else null,
                                scanMode = null,
                                error = if(denied) "Exit Denied" else null
                            )
                        }
                    },
                    onFailure = { e ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = e.message,
                                scanMode = null
                            )
                        }
                    }
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
    
    fun reportFocusLoss() {
        // Only report if exam is active
        val exam = _uiState.value.activeExam ?: return
        
        viewModelScope.launch {
            repository.reportIncident(
                examId = exam.examId,
                type = "FOCUS_LOST",
                details = "App lost focus (minimized or switched)"
            )
        }
    }
    
    fun reportKioskBypassAttempt(method: String) {
        val exam = _uiState.value.activeExam ?: return
        
        viewModelScope.launch {
            repository.reportIncident(
                examId = exam.examId,
                type = "KIOSK_BYPASS_ATTEMPT",
                details = "User attempted to bypass kiosk via: $method"
            )
        }
    }
}
