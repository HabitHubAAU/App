package com.example.habithub

import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habithub.HabitHubApplication
import com.example.habithub.data.model.Habit
import com.example.habithub.data.preferences.ThemeMode
import com.example.habithub.data.preferences.ThemePreference
import com.example.habithub.data.repository.HabitRepository
import com.example.habithub.sensor.ShakeDetector
import com.example.habithub.sensor.StepCounterSensor
import com.example.habithub.ui.navigation.Screen
import com.example.habithub.ui.screen.AddHabitScreenContent
import com.example.habithub.ui.screen.DetailScreen
import com.example.habithub.ui.screen.EditHabitScreen
import com.example.habithub.ui.screen.HomeScreen
import com.example.habithub.ui.screen.HomeScreenContent
import com.example.habithub.ui.screen.StatsScreen
import com.example.habithub.ui.theme.HabitHubTheme
import com.example.habithub.ui.viewmodel.HabitViewModel
import com.example.habithub.ui.viewmodel.HabitViewModelFactory
import com.example.habithub.ui.viewmodel.ThemeViewModel
import com.example.habithub.ui.viewmodel.ThemeViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: HabitViewModel by viewModels {
        val db = (application as HabitHubApplication).database
        HabitViewModelFactory(HabitRepository(db.habitDao(), db.completionDao()))
    }

    private val themeViewModel: ThemeViewModel by viewModels {
        ThemeViewModelFactory(ThemePreference(applicationContext))
    }

    private lateinit var sensorManager: SensorManager
    private lateinit var shakeDetector: ShakeDetector
    private lateinit var stepCounterSensor: StepCounterSensor

    private val snackbarHostState = SnackbarHostState()
    private var stepCount: Int? by mutableStateOf(null)

    private val motivationalQuotes = listOf(
        "You are one habit away from a different life.",
        "Small steps every day lead to big changes.",
        "Consistency is the key to achievement.",
        "Progress, not perfection. Keep going!",
        "Build habits, build yourself.",
        "Every day is a new opportunity to grow.",
        "Champions do ordinary things extraordinarily well.",
        "Your habits shape your destiny.",
        "One day at a time, one habit at a time.",
        "The secret of getting ahead is getting started."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        shakeDetector = ShakeDetector {
            val quote = motivationalQuotes.random()
            lifecycleScope.launch {
                snackbarHostState.currentSnackbarData?.dismiss()
                snackbarHostState.showSnackbar(message = quote, duration = SnackbarDuration.Short)
            }
        }

        stepCounterSensor = StepCounterSensor { steps ->
            stepCount = steps
        }

        setContent {
            val themeMode by themeViewModel.themeMode.collectAsState()
            val isDarkTheme = themeMode == ThemeMode.DARK
            HabitHubTheme(darkTheme = isDarkTheme, dynamicColor = false) {
                HabitHubApp(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    stepCount = stepCount,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { themeViewModel.toggleTheme() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ShakeDetector.register(sensorManager, shakeDetector)
        val hasStepSensor = StepCounterSensor.register(sensorManager, stepCounterSensor)
        if (!hasStepSensor) stepCount = null
    }

    override fun onPause() {
        super.onPause()
        ShakeDetector.unregister(sensorManager, shakeDetector)
        StepCounterSensor.unregister(sensorManager, stepCounterSensor)
    }
}


@Composable
fun HabitHubApp(
    viewModel: HabitViewModel,
    snackbarHostState: SnackbarHostState,
    stepCount: Int?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()

    HabitHubAppContent(
        viewModel = viewModel,
        snackbarHostState = snackbarHostState,
        navController = navController,
        stepCount = stepCount,
        isDarkTheme = isDarkTheme,
        onToggleTheme = onToggleTheme
    )
}

@Composable
fun HabitHubAppContent(
    viewModel: HabitViewModel,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    stepCount: Int?,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    val todayCompletions by viewModel.todayCompletions.collectAsState()
    val recentCompletions by viewModel.recentCompletions.collectAsState()

    val currentBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStack?.destination?.route
    val bottomNavScreens = listOf(Screen.Home, Screen.Add, Screen.Stats)
    val showBottomBar = currentRoute in bottomNavScreens.map { it.route }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onHabitClick = { habit ->
                        navController.navigate(Screen.Detail.route(habit.id))
                    },
                    onHabitLongClick = { habit ->
                        navController.navigate(Screen.Edit.route(habit.id))
                    },
                    stepCount = stepCount,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme
                )
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
                StatsScreen(viewModel = viewModel)
            }
            composable(
                route = Screen.Detail.route,
                arguments = listOf(navArgument("habitId") { type = NavType.IntType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getInt("habitId") ?: return@composable
                DetailScreen(
                    habitId = habitId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onEditHabit = { id ->
                        navController.navigate(Screen.Edit.route(id))
                    }
                )
            }
            composable(
                route = Screen.Edit.route,
                arguments = listOf(navArgument("habitId") { type = NavType.IntType })
            ) { backStackEntry ->
                val habitId = backStackEntry.arguments?.getInt("habitId") ?: return@composable
                EditHabitScreen(
                    habitId = habitId,
                    viewModel = viewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onDeleteHabit = { habit ->
                        viewModel.deleteHabit(habit)
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HabitHubAppPreview() {
    val habits = listOf(
        Habit(id = 1, name = "Reading", emoji = "📚", colorValue = 0xFF6750A4L),
        Habit(id = 2, name = "Workout", emoji = "💪", colorValue = 0xFF00897BL)
    )
    HabitHubTheme {
        HomeScreenContent(
            habits = habits,
            completedIds = setOf(1),
            onToggleCompletion = {},
            onDeleteHabit = {}
        )
    }
}