package com.example.habithub

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habithub.data.repository.HabitRepository
import com.example.habithub.ui.navigation.Screen
import com.example.habithub.ui.screen.AddHabitScreen
import com.example.habithub.ui.screen.AddHabitScreenContent
import com.example.habithub.ui.theme.HabitHubTheme
import com.example.habithub.ui.viewmodel.HabitViewModel
import com.example.habithub.ui.viewmodel.HabitViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels {
        val db = (application as HabitHubApplication).database
        HabitViewModelFactory(HabitRepository(db.habitDao(), db.completionDao()))
    }

    private val snackbarHostState = SnackbarHostState()
    private var stepCount: Int? by mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HabitHubTheme {
                HabitHubApp(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    stepCount = stepCount
                )
            }
        }
    }
}

@Composable
fun HabitHubApp(viewModel: HabitViewModel, snackbarHostState: SnackbarHostState, stepCount: Int?)
{
    val navController = rememberNavController()

    HabitHubAppContent(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        navController = navController,
        stepCount = stepCount
    )
}

@Composable
fun HabitHubAppContent(
    viewModel: HabitViewModel,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    stepCount: Int?
) {
    val habits by viewModel.habits.collectAsState()
    val todayCompletions by viewModel.todayCompletions.collectAsState()
    val recentCompletions by viewModel.recentCompletions.collectAsState()

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route

    val bottomNavScreens = listOf(Screen.Home, Screen.Add, Screen.Stats)
    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavScreens.forEach { screen ->
                        NavigationBarItem(
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.label) },
                            label = { Text(screen.label) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        // Hier beginnt dein NavHost Setup für den Commit (mit Dummy-Screens)
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Home Screen Placeholder")
                }
            }

            composable(Screen.Add.route) {
                AddHabitScreenContent(
                    onAddHabit = { name, desc, emoji, color, days ->
                        viewModel.addHabit(name, desc, emoji, color, days)
                    },
                    onNavigateBack = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Add.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Stats.route) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Stats Screen Placeholder")
                }
            }

            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("habitId") { type = NavType.IntType })
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Detail Screen Placeholder")
                }
            }

            composable(
                route = Screen.Edit.route,
                arguments = listOf(navArgument("habitId") { type = NavType.IntType })
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Edit Habit Screen Placeholder")
                }
            }
        }
    }
}