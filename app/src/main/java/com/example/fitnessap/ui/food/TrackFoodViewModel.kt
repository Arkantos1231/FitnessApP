package com.example.fitnessap.ui.food

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnessap.data.local.entity.FoodLog
import com.example.fitnessap.data.repository.LogRepository
import com.example.fitnessap.data.repository.UserProfileRepository
import com.example.fitnessap.network.OpenAiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class TrackFoodUiState {
    object Idle : TrackFoodUiState()
    object Loading : TrackFoodUiState()
    data class Result(val calories: Int) : TrackFoodUiState()
    data class Error(val message: String) : TrackFoodUiState()
    object Logged : TrackFoodUiState()
}

class TrackFoodViewModel(
    private val logRepository: LogRepository,
    private val userProfileRepository: UserProfileRepository,
    private val openAiService: OpenAiService
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrackFoodUiState>(TrackFoodUiState.Idle)
    val uiState: StateFlow<TrackFoodUiState> = _uiState.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _manualName = MutableStateFlow("")
    val manualName: StateFlow<String> = _manualName.asStateFlow()

    private val _manualCalories = MutableStateFlow("")
    val manualCalories: StateFlow<String> = _manualCalories.asStateFlow()

    val logs: StateFlow<List<FoodLog>> = logRepository.getTodayFoodLogs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onDescriptionChange(value: String) { _description.value = value }
    fun onManualNameChange(value: String) { _manualName.value = value }
    fun onManualCaloriesChange(value: String) {
        if (value.all { it.isDigit() }) _manualCalories.value = value
    }

    fun deleteLog(log: FoodLog) {
        viewModelScope.launch { logRepository.deleteFoodLog(log) }
    }

    fun estimateCalories() {
        val desc = _description.value.trim()
        if (desc.isBlank()) return
        viewModelScope.launch {
            _uiState.value = TrackFoodUiState.Loading
            try {
                val apiKey = userProfileRepository.apiKeyFlow.first()
                if (apiKey.isBlank()) {
                    _uiState.value = TrackFoodUiState.Error("Please set your OpenAI API key in Profile.")
                    return@launch
                }
                val calories = openAiService.estimateCaloriesConsumed(apiKey, desc)
                _uiState.value = TrackFoodUiState.Result(calories)
            } catch (e: Exception) {
                _uiState.value = TrackFoodUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun logFood(calories: Int) {
        viewModelScope.launch {
            logRepository.insertFoodLog(
                FoodLog(
                    dateMillis = System.currentTimeMillis(),
                    description = _description.value,
                    caloriesConsumed = calories
                )
            )
            _description.value = ""
            _uiState.value = TrackFoodUiState.Logged
        }
    }

    fun logManual() {
        val name = _manualName.value.trim()
        val calories = _manualCalories.value.toIntOrNull() ?: return
        if (name.isBlank() || calories <= 0) return
        viewModelScope.launch {
            logRepository.insertFoodLog(
                FoodLog(
                    dateMillis = System.currentTimeMillis(),
                    description = name,
                    caloriesConsumed = calories
                )
            )
            _manualName.value = ""
            _manualCalories.value = ""
        }
    }

    fun reset() { _uiState.value = TrackFoodUiState.Idle }

    class Factory(
        private val logRepository: LogRepository,
        private val userProfileRepository: UserProfileRepository,
        private val openAiService: OpenAiService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TrackFoodViewModel(logRepository, userProfileRepository, openAiService) as T
        }
    }
}
