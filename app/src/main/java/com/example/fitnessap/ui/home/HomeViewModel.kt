package com.example.fitnessap.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitnessap.data.repository.LogRepository
import com.example.fitnessap.data.repository.UserProfileRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class HomeUiState(
    val caloriesBurned: Int = 0,
    val caloriesConsumed: Int = 0,
    val dailyCalorieGoal: Int = 0
) {
    val net: Int get() = caloriesConsumed - caloriesBurned
    val remaining: Int get() = dailyCalorieGoal - net
}

class HomeViewModel(
    logRepository: LogRepository,
    userProfileRepository: UserProfileRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        logRepository.getTodayCaloriesBurned(),
        logRepository.getTodayCaloriesConsumed(),
        userProfileRepository.userProfileFlow
    ) { burned, consumed, profile ->
        HomeUiState(
            caloriesBurned = burned,
            caloriesConsumed = consumed,
            dailyCalorieGoal = profile.dailyCalorieGoal
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    class Factory(
        private val logRepository: LogRepository,
        private val userProfileRepository: UserProfileRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(logRepository, userProfileRepository) as T
        }
    }
}
