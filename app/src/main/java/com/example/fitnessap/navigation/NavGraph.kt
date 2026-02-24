package com.example.fitnessap.navigation

import android.content.Context
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fitnessap.data.local.AppDatabase
import com.example.fitnessap.data.repository.LogRepository
import com.example.fitnessap.data.repository.UserProfileRepository
import com.example.fitnessap.network.OpenAiService
import com.example.fitnessap.ui.activity.TrackActivityScreen
import com.example.fitnessap.ui.activity.TrackActivityViewModel
import com.example.fitnessap.ui.food.TrackFoodScreen
import com.example.fitnessap.ui.food.TrackFoodViewModel
import com.example.fitnessap.ui.home.HomeScreen
import com.example.fitnessap.ui.home.HomeViewModel
import com.example.fitnessap.ui.profile.ProfileScreen
import com.example.fitnessap.ui.profile.ProfileViewModel

@Composable
fun AppNavigation(context: Context) {
    val navController = rememberNavController()
    val db = AppDatabase.getInstance(context)
    val logRepository = LogRepository(db)
    val userProfileRepository = UserProfileRepository(context)
    val openAiService = OpenAiService()

    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.TrackActivity,
        BottomNavItem.TrackFood,
        BottomNavItem.Profile
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == item.route } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None },
            popEnterTransition = { EnterTransition.None },
            popExitTransition = { ExitTransition.None }
        ) {
            composable(BottomNavItem.Home.route) {
                val viewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(logRepository, userProfileRepository)
                )
                HomeScreen(viewModel = viewModel)
            }
            composable(BottomNavItem.Profile.route) {
                val viewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModel.Factory(userProfileRepository, context)
                )
                ProfileScreen(viewModel = viewModel)
            }
            composable(BottomNavItem.TrackActivity.route) {
                val viewModel: TrackActivityViewModel = viewModel(
                    factory = TrackActivityViewModel.Factory(logRepository, userProfileRepository, openAiService, context)
                )
                TrackActivityScreen(viewModel = viewModel)
            }
            composable(BottomNavItem.TrackFood.route) {
                val viewModel: TrackFoodViewModel = viewModel(
                    factory = TrackFoodViewModel.Factory(logRepository, userProfileRepository, openAiService)
                )
                TrackFoodScreen(viewModel = viewModel)
            }
        }
    }
}
