package com.example.fitnessap.ui.activity

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnessap.data.local.entity.ActivityLog
import com.example.fitnessap.data.model.UserProfile
import com.example.fitnessap.data.repository.LogRepository
import com.example.fitnessap.data.repository.UserProfileRepository
import com.example.fitnessap.network.OpenAiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class TrackActivityUiState {
    object Idle : TrackActivityUiState()
    object Loading : TrackActivityUiState()
    data class Result(val calories: Int) : TrackActivityUiState()
    data class Error(val message: String) : TrackActivityUiState()
    object Logged : TrackActivityUiState()
}

class TrackActivityViewModel(
    private val logRepository: LogRepository,
    private val userProfileRepository: UserProfileRepository,
    private val openAiService: OpenAiService,
    private val context: Context
) : ViewModel() {

    // ── AI flow ─────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow<TrackActivityUiState>(TrackActivityUiState.Idle)
    val uiState: StateFlow<TrackActivityUiState> = _uiState.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    val logs: StateFlow<List<ActivityLog>> = logRepository.getTodayActivityLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDescriptionChange(value: String) { _description.value = value }

    fun deleteLog(log: ActivityLog) {
        viewModelScope.launch { logRepository.deleteActivityLog(log) }
    }

    fun estimateCalories() {
        val desc = _description.value.trim()
        if (desc.isBlank()) return
        viewModelScope.launch {
            _uiState.value = TrackActivityUiState.Loading
            try {
                val profile = userProfileRepository.userProfileFlow.first()
                val apiKey = userProfileRepository.apiKeyFlow.first()
                if (apiKey.isBlank()) {
                    _uiState.value = TrackActivityUiState.Error("Please set your OpenAI API key in Profile.")
                    return@launch
                }
                val calories = openAiService.estimateCaloriesBurned(apiKey, desc, profile)
                _uiState.value = TrackActivityUiState.Result(calories)
            } catch (e: Exception) {
                _uiState.value = TrackActivityUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logActivity(calories: Int) {
        viewModelScope.launch {
            logRepository.insertActivityLog(
                ActivityLog(
                    dateMillis = System.currentTimeMillis(),
                    description = _description.value,
                    caloriesBurned = calories
                )
            )
            _description.value = ""
            _uiState.value = TrackActivityUiState.Logged
        }
    }

    fun reset() { _uiState.value = TrackActivityUiState.Idle }

    // ── Step counter ─────────────────────────────────────────────────────────

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepDetectorSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

    val hasStepSensor: Boolean = stepDetectorSensor != null

    private val _isTracking = MutableStateFlow(false)
    val isTracking: StateFlow<Boolean> = _isTracking.asStateFlow()

    private val _stepCount = MutableStateFlow(0)
    val stepCount: StateFlow<Int> = _stepCount.asStateFlow()

    private val _profile = MutableStateFlow(UserProfile())

    // Live calorie estimate: steps × weightKg × 0.0005  (≈ 0.04 kcal/step at 80 kg)
    val liveStepCalories: StateFlow<Int> =
        combine(_stepCount, _profile) { steps, profile ->
            val w = if (profile.weightKg > 0f) profile.weightKg else 70f
            (steps * w * 0.0005f).toInt()
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val stepListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_STEP_DETECTOR) {
                _stepCount.value += 1
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        viewModelScope.launch {
            userProfileRepository.userProfileFlow.collect { _profile.value = it }
        }
    }

    fun startStepTracking() {
        if (stepDetectorSensor == null) return
        _stepCount.value = 0
        _isTracking.value = true
        sensorManager.registerListener(
            stepListener,
            stepDetectorSensor,
            SensorManager.SENSOR_DELAY_FASTEST
        )
    }

    fun stopAndLogSteps() {
        sensorManager.unregisterListener(stepListener)
        _isTracking.value = false
        val steps = _stepCount.value
        if (steps > 0) {
            viewModelScope.launch {
                val w = if (_profile.value.weightKg > 0f) _profile.value.weightKg else 70f
                val calories = (steps * w * 0.0005f).toInt().coerceAtLeast(1)
                logRepository.insertActivityLog(
                    ActivityLog(
                        dateMillis = System.currentTimeMillis(),
                        description = "$steps steps",
                        caloriesBurned = calories
                    )
                )
                _stepCount.value = 0
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.unregisterListener(stepListener)
    }

    // ── Factory ──────────────────────────────────────────────────────────────

    class Factory(
        private val logRepository: LogRepository,
        private val userProfileRepository: UserProfileRepository,
        private val openAiService: OpenAiService,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackActivityViewModel(logRepository, userProfileRepository, openAiService, context) as T
        }
    }
}
