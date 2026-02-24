package com.example.fitnessap.ui.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun TrackFoodScreen(viewModel: TrackFoodViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val description by viewModel.description.collectAsState()
    val manualName by viewModel.manualName.collectAsState()
    val manualCalories by viewModel.manualCalories.collectAsState()
    val logs by viewModel.logs.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is TrackFoodUiState.Logged) {
            viewModel.reset()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Track Food",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        OutlinedTextField(
            value = description,
            onValueChange = viewModel::onDescriptionChange,
            label = { Text("Describe what you ate") },
            placeholder = { Text("e.g., A bowl of oatmeal with banana and honey") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = viewModel::estimateCalories,
            enabled = description.isNotBlank() && uiState !is TrackFoodUiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Estimate Calories")
        }

        when (val state = uiState) {
            is TrackFoodUiState.Loading -> {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is TrackFoodUiState.Result -> {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Estimated Calories", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${state.calories} kcal",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.logFood(state.calories) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Log Food")
                        }
                    }
                }
            }
            is TrackFoodUiState.Error -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        text = state.message,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            else -> { /* Idle / Logged — nothing extra to show */ }
        }

        HorizontalDivider()

        Text(
            text = "Log Manually",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        OutlinedTextField(
            value = manualName,
            onValueChange = viewModel::onManualNameChange,
            label = { Text("Food name") },
            placeholder = { Text("e.g., Banana, Rice with chicken…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = manualCalories,
            onValueChange = viewModel::onManualCaloriesChange,
            label = { Text("Calories (kcal)") },
            placeholder = { Text("e.g., 350") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = viewModel::logManual,
            enabled = manualName.isNotBlank() && (manualCalories.toIntOrNull() ?: 0) > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Log Food")
        }

        if (logs.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Today's Food Log",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    logs.forEachIndexed { index, log ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = log.description,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = "${log.caloriesConsumed} kcal",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.tertiary
                                )
                            }
                            IconButton(onClick = { viewModel.deleteLog(log) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        if (index < logs.lastIndex) HorizontalDivider()
                    }
                }
            }
        }
    }
}
