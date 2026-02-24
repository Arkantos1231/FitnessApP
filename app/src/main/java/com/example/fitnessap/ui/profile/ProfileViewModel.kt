package com.example.fitnessap.ui.profile

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.fitnessap.data.model.UserProfile
import com.example.fitnessap.data.repository.UserProfileRepository
import com.example.fitnessap.notification.FoodReminderWorker
import com.example.fitnessap.sync.FirebaseSyncWorker
import com.example.fitnessap.util.millisUntilNextHourET
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class ProfileUiState(
    val name: String = "",
    val age: String = "",
    val weightKg: String = "",
    val heightCm: String = "",
    val gender: String = "",
    val activityLevel: String = "",
    val dailyCalorieGoal: String = "",
    val apiKey: String = "",
    val foodReminderEnabled: Boolean = false,
    val saved: Boolean = false,
    val isSignedIn: Boolean = false,
    val signedInEmail: String = "",
    val syncStatus: String = "Never synced"
)

class ProfileViewModel(
    private val userProfileRepository: UserProfileRepository,
    private val context: Context
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userProfileRepository.userProfileFlow.collect { profile ->
                _uiState.value = _uiState.value.copy(
                    name = profile.name,
                    age = if (profile.age == 0) "" else profile.age.toString(),
                    weightKg = if (profile.weightKg == 0f) "" else profile.weightKg.toString(),
                    heightCm = if (profile.heightCm == 0f) "" else profile.heightCm.toString(),
                    gender = profile.gender,
                    activityLevel = profile.activityLevel,
                    dailyCalorieGoal = if (profile.dailyCalorieGoal == 0) "" else profile.dailyCalorieGoal.toString()
                )
            }
        }
        viewModelScope.launch {
            userProfileRepository.apiKeyFlow.collect { key ->
                _uiState.value = _uiState.value.copy(apiKey = key)
            }
        }
        viewModelScope.launch {
            userProfileRepository.foodReminderEnabledFlow.collect { enabled ->
                _uiState.value = _uiState.value.copy(foodReminderEnabled = enabled)
            }
        }
        viewModelScope.launch {
            userProfileRepository.lastSyncMillisFlow.collect { millis ->
                val status = if (millis == 0L) "Never synced" else {
                    val fmt = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                    "Last sync: ${fmt.format(Date(millis))}"
                }
                _uiState.value = _uiState.value.copy(syncStatus = status)
            }
        }

        // Restore sign-in state
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _uiState.value = _uiState.value.copy(
                isSignedIn = true,
                signedInEmail = currentUser.email ?: ""
            )
        }
    }

    fun onNameChange(value: String) { _uiState.value = _uiState.value.copy(name = value) }
    fun onAgeChange(value: String) { _uiState.value = _uiState.value.copy(age = value) }
    fun onWeightChange(value: String) { _uiState.value = _uiState.value.copy(weightKg = value) }
    fun onHeightChange(value: String) { _uiState.value = _uiState.value.copy(heightCm = value) }
    fun onGenderChange(value: String) { _uiState.value = _uiState.value.copy(gender = value) }
    fun onActivityLevelChange(value: String) { _uiState.value = _uiState.value.copy(activityLevel = value) }
    fun onDailyCalorieGoalChange(value: String) { _uiState.value = _uiState.value.copy(dailyCalorieGoal = value) }
    fun onApiKeyChange(value: String) { _uiState.value = _uiState.value.copy(apiKey = value) }
    fun onSavedConsumed() { _uiState.value = _uiState.value.copy(saved = false) }

    fun setFoodReminderEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(foodReminderEnabled = enabled)
        viewModelScope.launch {
            userProfileRepository.saveFoodReminderEnabled(enabled)
            val workManager = WorkManager.getInstance(context)
            if (enabled) {
                val schedule = listOf(
                    7 to FoodReminderWorker.WORK_NAME_7,
                    13 to FoodReminderWorker.WORK_NAME_13,
                    20 to FoodReminderWorker.WORK_NAME_20
                )
                schedule.forEach { (hour, workName) ->
                    val delay = millisUntilNextHourET(hour)
                    val request = PeriodicWorkRequestBuilder<FoodReminderWorker>(24, TimeUnit.HOURS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build()
                    workManager.enqueueUniquePeriodicWork(
                        workName,
                        ExistingPeriodicWorkPolicy.REPLACE,
                        request
                    )
                }
            } else {
                FoodReminderWorker.ALL_WORK_NAMES.forEach { workManager.cancelUniqueWork(it) }
            }
        }
    }

    fun onSignInSuccess(account: GoogleSignInAccount) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential).await()
                val user = auth.currentUser
                _uiState.value = _uiState.value.copy(
                    isSignedIn = true,
                    signedInEmail = user?.email ?: ""
                )
                scheduleSyncWorker()
            } catch (e: Exception) {
                // Sign-in failed; remain signed out
            }
        }
    }

    fun signOut() {
        auth.signOut()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(com.example.fitnessap.R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso).signOut()
        cancelSyncWorker()
        _uiState.value = _uiState.value.copy(
            isSignedIn = false,
            signedInEmail = "",
            syncStatus = "Never synced"
        )
    }

    fun triggerManualSync() {
        _uiState.value = _uiState.value.copy(syncStatus = "Syncing…")
        val request = OneTimeWorkRequestBuilder<FirebaseSyncWorker>().build()
        WorkManager.getInstance(context).enqueue(request)
    }

    private fun scheduleSyncWorker() {
        val delay = millisUntilNextHourET(20)
        val request = PeriodicWorkRequestBuilder<FirebaseSyncWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            FirebaseSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            request
        )
    }

    private fun cancelSyncWorker() {
        WorkManager.getInstance(context).cancelUniqueWork(FirebaseSyncWorker.WORK_NAME)
    }

    fun testNotification() {
        val notification = NotificationCompat.Builder(context, FoodReminderWorker.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Food Reminder")
            .setContentText("Don't forget to log what you've eaten!")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1002, notification)
    }

    fun saveProfile() {
        val state = _uiState.value
        viewModelScope.launch {
            val profile = UserProfile(
                name = state.name,
                age = state.age.toIntOrNull() ?: 0,
                weightKg = state.weightKg.toFloatOrNull() ?: 0f,
                heightCm = state.heightCm.toFloatOrNull() ?: 0f,
                gender = state.gender,
                activityLevel = state.activityLevel,
                dailyCalorieGoal = state.dailyCalorieGoal.toIntOrNull() ?: 0
            )
            userProfileRepository.saveProfile(profile)
            userProfileRepository.saveApiKey(state.apiKey)
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    class Factory(
        private val userProfileRepository: UserProfileRepository,
        private val context: Context
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(userProfileRepository, context) as T
        }
    }
}
